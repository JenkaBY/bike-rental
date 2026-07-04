# System Design: FR-05 — Customer Signs the Rental Agreement

## 1. Architectural Overview

Completes the signing flow inside the `agreement` module. A single transactional application
service orchestrates: rental snapshot (via `RentalSigningFacade`, FR-02) → template & duplicate &
fencing checks → customer lookup (`CustomerFacade`) → PDF rendering (`AgreementPdfRenderer`,
FR-04) → signature persistence → synchronous `completeSigning` on rental → event publication
(delivered to listeners after commit via Spring Modulith).

The signing REST controller physically lives in the agreement module although its URL is under
`/api/rentals/{rentalId}/signatures` (agreed API contract).

```
POST /api/rentals/{id}/signatures → SignAgreementService (@Transactional)
  ├─ RentalSigningFacade.getSigningSnapshot(id)         [rental module API]
  ├─ template checks (active, matches request)          [agreement domain]
  ├─ duplicate check (repository.existsByRentalId)
  ├─ fencing check (request.rentalVersion == snapshot.version)
  ├─ CustomerFacade.findById(snapshot.customerId)       [customer module API]
  ├─ AgreementPdfRenderer.render(...)                   [FR-04 port]
  ├─ save AgreementSignature (PDF, PNG, snapshot JSONB, sha256s, ip, ua, operator)
  ├─ RentalSigningFacade.completeSigning(id, version, signedAt)
  └─ EventPublisher.publish(AgreementSignedEvent)       [after-commit delivery]
```

---

## 2. Impacted Components

### Persistence

* **Liquibase** *(new)* `v1/agreement_signatures.create-table.xml` (id = file name,
  `author="claude"`, `<preConditions onFail="MARK_RAN">`):

  | column | type | constraints |
  |---|---|---|
  | id | BIGSERIAL | PK |
  | template_id | BIGINT | NOT NULL, FK → agreement_templates(id) `fk_agreement_signatures_agreement_templates` |
  | rental_id | BIGINT | NOT NULL, UNIQUE `uq_agreement_signatures_rental_id` |
  | customer_id | UUID | NOT NULL |
  | operator_id | VARCHAR(100) | NOT NULL |
  | signing_snapshot | JSONB | NOT NULL |
  | pdf_document | BYTEA | NOT NULL |
  | pdf_sha256 | CHAR(64) | NOT NULL |
  | template_content_sha256 | CHAR(64) | NOT NULL |
  | signature_image | BYTEA | NOT NULL |
  | signed_at | TIMESTAMP WITH TIME ZONE | NOT NULL |
  | ip_address | VARCHAR(45) | nullable |
  | user_agent | TEXT | nullable |

  Indexes: `idx_agreement_signatures_customer_id`, `idx_agreement_signatures_template_id`.
  NO FK to rentals (cross-module; integrity via the synchronous check). Registered at the bottom
  of the master changelog.

* **`AgreementSignatureJpaEntity`** *(new)*: `byte[] pdfDocument`, `byte[] signatureImage`;
  `signing_snapshot` mapped as
  `@JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private SigningSnapshot signingSnapshot;`
  (pattern: `RentalEquipmentJpaEntity.finalCostBreakdown`); `signed_at` as `Instant`.

* **`AgreementSignatureJpaRepository`**: `boolean existsByRentalId(Long)`,
  interface projection `SignatureSummaryView(Long id, Long templateId, Instant signedAt)` via
  JPQL selecting only those columns + join to template for `versionNumber`
  (`select s.id as id, s.templateId as templateId, t.versionNumber as templateVersionNumber, s.signedAt as signedAt ...`),
  and a dedicated `@Query` fetching only `pdf_document` for the download
  (`select s.pdfDocument from ... where s.rentalId = :rentalId`) — BYTEA never loaded in lists
  by construction.

### Domain (`agreement/domain/`)

* **`AgreementSignature`** aggregate *(new)* — immutable record-of-fact; created once via
  `AgreementSignature.create(...)`, no mutating behavior. Fields mirror the table;
  `SigningSnapshot` *(new record, `domain/model/`)*: customer (first/last name, phone), rental
  (id, version — informational, plannedDuration, startedAt, equipment lines with uid/name/cost,
  estimated total), template (id, versionNumber, contentSha256).
* **`AgreementSignatureRepository`** *(port)*: `save`, `existsByRentalId`,
  `findSummariesByRentalId` (0..1), `findPdfByRentalId` (`Optional<byte[]>`).
* **Domain exceptions** *(new, `agreement/domain/exception/`)*, codes per agreed contract:
    * `AgreementAlreadySignedException` — `agreement.signing.already_signed`
    * `AgreementTemplateNotActiveException` — `agreement.template.not_active`
    * `SigningVersionMismatchException` — `agreement.signing.rental_version_mismatch`
    * (reuse FR-03's `ActiveAgreementTemplateNotFoundException` — `agreement.template.no_active`)
* **`SignatureImageDecoder`** (application-level helper, see below) — invalid base64 → 400
  validation-style error `agreement.signing.invalid_signature_image`.

### Application (`agreement/application/`)

* **`SignAgreementUseCase`** *(interface)*:
  `SignAgreementResult execute(SignAgreementCommand(Long rentalId, String signaturePngBase64, Long rentalVersion, Long templateId, String operatorId, String ipAddress, String userAgent))`;
  `SignAgreementResult(Long signatureId, Instant signedAt)`.
* **`SignAgreementService`** *(new, ONE `@Transactional` method)*, ordered steps:
  1. `snapshot = rentalSigningFacade.getSigningSnapshot(rentalId)` — missing rental → global 404;
     wrong status → `RentalNotAwaitingSignatureException` (rental public API) → 409 by advice.
  2. `template = templateRepository.findActive().orElseThrow(ActiveAgreementTemplateNotFoundException::new)`;
     `if (!template.getId().equals(command.templateId())) throw new AgreementTemplateNotActiveException(...)`.
  3. `if (signatureRepository.existsByRentalId(rentalId)) throw new AgreementAlreadySignedException(rentalId)`.
  4. `if (!snapshot.version().equals(command.rentalVersion())) throw new SigningVersionMismatchException(...)`.
  5. `customer = customerFacade.findById(snapshot.customerId()).orElseThrow(...)` (shared `ResourceNotFoundException`).
  6. `signedAt = clock.instant()`; `startedAt = LocalDateTime.ofInstant(signedAt, ZoneId.systemDefault())`.
  7. decode PNG (`Base64.getDecoder()`; strip optional `data:image/png;base64,` prefix) →
     `AgreementPdfData` (real customer + snapshot equipment lines + startedAt) →
     `pdf = renderer.render(data)`; `pdfSha256 = ContentHasher.sha256(pdf)` (reuse FR-03 hasher).
  8. build `SigningSnapshot` + `AgreementSignature.create(...)` → `signatureRepository.save(...)`.
  9. `rentalSigningFacade.completeSigning(rentalId, command.rentalVersion(), signedAt)` —
     may throw `RentalSigningVersionMismatchException` / `ObjectOptimisticLockingFailureException`
     → transaction rollback (signature row not committed).
  10. `eventPublisher.publish("agreement-events", new AgreementSignedEvent(rentalId, signature.getId()))`
      via shared `EventPublisher` (requires an active transaction; Modulith delivers to
      `@ApplicationModuleListener`s after commit).
* **Query use cases**: `FindRentalSignaturesUseCase` (list of 0..1 summaries),
  `GetSignaturePdfUseCase` (`byte[]`, absent → shared `ResourceNotFoundException` → 404).

### Module API (`agreement/` root package)

* **`AgreementSignedEvent(Long rentalId, Long signatureId)`** *(new public record)* implements
  `shared.domain.event.BikeRentalEvent`. Lives in the agreement root (module API) — NOT in shared.

### Web (`agreement/web/`)

* **`RentalSignatureCommandController`** *(new, `web/command/`)*, path `/api/rentals/{rentalId}/signatures`:
  ```java
  @PostMapping → 201 SignatureCreatedResponse(Long signatureId, Instant signedAt)
  ```
  Request record `SignAgreementRequest(@NotBlank String signaturePng, @NotNull @PositiveOrZero Long rentalVersion,
  @NotNull @Positive Long templateId, @NotBlank String operatorId)`.
  `ip` resolved: first non-blank entry of `X-Forwarded-For` split by comma, else
  `request.getRemoteAddr()`; `userAgent` from the `User-Agent` header. Both passed into the command.
* **`RentalSignatureQueryController`** *(new, `web/query/`)*, same path, content negotiation:
  ```java
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)  → 200 List<SignatureSummaryResponse>
  @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)   → 200 byte[] + Content-Disposition: attachment; filename="rental-{id}-agreement.pdf" (404 when unsigned)
  ```
  `SignatureSummaryResponse(Long signatureId, Long templateId, Integer templateVersionNumber, Instant signedAt)`.
* **`AgreementRestControllerAdvice`** (FR-03) gains handlers:
    * `AgreementAlreadySignedException`, `AgreementTemplateNotActiveException`,
      `SigningVersionMismatchException` → **409** (exception's own errorCode)
    * `RentalNotAwaitingSignatureException` (rental module API) → **409** `agreement.signing.rental_not_awaiting_signature`
    * `RentalSigningVersionMismatchException` (rental module API) → **409** `agreement.signing.rental_version_mismatch`
    * invalid base64 → **400** `agreement.signing.invalid_signature_image`
    * `ObjectOptimisticLockingFailureException` — NOT handled here; global advice returns 409
      `shared.resource.optimistic_lock`.

---

## 3. Abstract Data Schema Changes

New table `agreement_signatures` (above). No changes to existing tables.

---

## 4. Component Contracts & Payloads

* **`POST /api/rentals/{rentalId}/signatures`**
    * Request: `{"signaturePng": "<base64>", "rentalVersion": 3, "templateId": 2, "operatorId": "op-1"}`
    * `201` → `{"signatureId": 10, "signedAt": "2026-07-04T12:34:56Z"}`
    * `404` — rental does not exist; `409` — see error codes above; `400` — validation/base64.
* **`GET /api/rentals/{rentalId}/signatures`** — JSON: `[]` or one summary; PDF: attachment bytes.
* **Cross-module calls:** `RentalSigningFacade` (rental), `CustomerFacade` (customer) — module
  API packages only; `ApplicationModules.verify()` must stay green.

---

## 5. Updated Interaction Sequence

### Happy path

1. Frontend (holding `version = V` from prepare-signing) POSTs signature.
2. Steps 1–8 of `SignAgreementService` succeed; PDF rendered with the single `signedAt` instant.
3. `completeSigning` flips rental to ACTIVE (`startedAt = signedAt` local time), publishes `RentalStarted`.
4. Transaction commits: signature row + rental update atomically; then `AgreementSignedEvent`
   reaches its (future) listeners.
5. `201 {signatureId, signedAt}` — a positive response by itself means the rental is ACTIVE.

### Race: cancel-signing vs signature

1. Operator cancels signing concurrently with the customer submitting.
2. If cancel commits first: `completeSigning`'s save hits the stale version →
   `ObjectOptimisticLockingFailureException` → rollback (no signature row) → 409
   `shared.resource.optimistic_lock`.
3. If signing commits first: cancel's save loses the same way; rental stays ACTIVE, signature intact.

### Stale tab (fencing)

1. Rental re-prepared after edits → current version W > V.
2. Old tab POSTs with `rentalVersion = V` → step 4 fails → 409
   `agreement.signing.rental_version_mismatch`; nothing persisted.

---

## 6. Non-Functional Architecture Decisions

* **Atomicity:** signature insert and rental activation share one DB transaction; the event is
  published inside it but delivered after commit (Spring Modulith event publication registry).
* **BYTEA hygiene:** list/summary reads use interface projections; the PDF is fetched by a
  dedicated single-column query; no JPA relation from signature summaries to binary columns is
  ever traversed.
* **Tamper evidence:** `pdf_sha256` computed from the final bytes; `template_content_sha256`
  copied from the template at signing time.
* **Testing:** component tests `features/agreement/agreement-signing.feature` for FR scenarios
  1–6 (PDF assertions via PDFBox text extraction; sha256 recomputed in the step and compared);
  WebMvc `@ApiTest` for request validation and ip/user-agent extraction; `ModulithBoundariesTest`
  green.
