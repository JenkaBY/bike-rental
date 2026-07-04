# System Design: FR-03 — Agreement Template Management (Admin)

## 1. Architectural Overview

Introduces the new Spring Modulith module `agreement` (package
`com.github.jenkaby.bikerental.agreement`) with the standard hexagonal layout (`web/`,
`application/`, `domain/`, `infrastructure/`), declared as
`@ApplicationModule(displayName = "Agreement Module", allowedDependencies = {"shared", "customer", "rental"})`
(customer/rental are declared now so FR-04/FR-05 need no module reconfiguration; unused
declarations are legal).

This story delivers the `AgreementTemplate` aggregate with a strictly linear lifecycle
(`DRAFT → ACTIVE → DEACTIVATED`) guarded inside the domain entity, the activation service with a
DB-safe two-step flush, and the admin REST API under `/api/agreements`.

---

## 2. Impacted Components

### Persistence

* **Liquibase** *(new)* `service/src/main/resources/db/changelog/v1/agreement_templates.create-table.xml`
  (changeset id = file name, `author="claude"`, guarded by `<preConditions onFail="MARK_RAN">`):

  | column | type | constraints |
  |---|---|---|
  | id | BIGSERIAL | PK |
  | version_number | INTEGER | nullable |
  | title | VARCHAR(255) | NOT NULL |
  | content | TEXT | NOT NULL |
  | content_sha256 | CHAR(64) | nullable |
  | status | VARCHAR(16) | NOT NULL |
  | lock_version | BIGINT | NOT NULL DEFAULT 0 |
  | created_at | TIMESTAMP WITH TIME ZONE | NOT NULL DEFAULT now() |
  | updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL DEFAULT now() |
  | activated_at | TIMESTAMP WITH TIME ZONE | nullable |
  | deactivated_at | TIMESTAMP WITH TIME ZONE | nullable |

  Plus `<sql dbms="postgresql">` in the same changeset:
  `CHECK (status IN ('DRAFT','ACTIVE','DEACTIVATED'))` (named `ck_agreement_templates_status`),
  partial unique index `uq_agreement_templates_single_active ON agreement_templates ((true)) WHERE status = 'ACTIVE'`,
  partial unique index `uq_agreement_templates_version_number ON (version_number) WHERE version_number IS NOT NULL`.
  Registered at the bottom of `db.changelog-master.xml`.

* **`AgreementTemplateJpaEntity`** *(new, `agreement/infrastructure/persistence/entity/`)*:
  fields per table; `@Version @Column(name = "lock_version") private Long lockVersion;`
  `@Enumerated(EnumType.STRING) status`; `@PrePersist/@PreUpdate` audit timestamps (same pattern
  as `RentalJpaEntity`).

* **`AgreementTemplateJpaRepository`** *(new, Spring Data)*: `Optional<...> findByStatus(...)`,
  `@Query("select max(t.versionNumber) from ...") Optional<Integer> findMaxVersionNumber()`,
  and a list projection query for the catalog (see Web).

* **`AgreementTemplateRepositoryAdapter`** *(new, implements domain port)*: standard
  toEntity/toDomain via a MapStruct `AgreementTemplateJpaMapper`. Implements `saveNow(...)` with
  `repository.saveAndFlush(...)` — the flush is a JPA detail hidden behind the port.

### Domain (`agreement/domain/`)

* **`AgreementTemplateStatus`** enum: `DRAFT, ACTIVE, DEACTIVATED` (no transition map needed —
  guards live in the aggregate; transitions are linear).
* **`AgreementTemplate`** aggregate (Lombok `@Getter @Builder`, mirrors `Rental` style):
    * `static AgreementTemplate createDraft(String title, String content)`
    * `updateContent(String title, String content)` — throws `AgreementTemplateNotEditableException` unless DRAFT
    * `activate(int versionNumber, String contentSha256, Instant activatedAt)` — throws
      `AgreementTemplateNotActivatableException` unless DRAFT; sets status/versionNumber/sha/activatedAt
    * `deactivate(Instant deactivatedAt)` — throws domain exception unless ACTIVE
    * `ensureDeletable()` — throws `AgreementTemplateNotDeletableException` unless DRAFT
* **Domain exceptions** *(new, `agreement/domain/exception/`)*, each extending shared
  `BikeRentalException` with its own error code (pattern: `InvalidRentalStatusException`):
    * `AgreementTemplateNotEditableException` — code `agreement.template.not_editable`
    * `AgreementTemplateNotActivatableException` — code `agreement.template.not_activatable`
    * `AgreementTemplateNotDeletableException` — code `agreement.template.not_deletable`
    * `ActiveAgreementTemplateNotFoundException` — code `agreement.template.no_active` (also reused by FR-05)
* **`AgreementTemplateRepository`** *(domain port, no JPA types)*:
  `save`, `saveNow` (immediate DB synchronization), `findById`, `findActive`,
  `findAllSummaries` (returns list DTOs without content), `nextVersionNumber()`, `delete`.

### Application (`agreement/application/`)

* Use case interfaces (`usecase/`) + `@Service` implementations (`service/`):
    * `CreateAgreementTemplateUseCase` / service — creates DRAFT.
    * `UpdateAgreementTemplateUseCase` / service — loads, `updateContent`, saves.
    * `ActivateAgreementTemplateUseCase` / **`ActivateAgreementTemplateService`** — the critical
      ordering, single `@Transactional` method:
      1. load draft (404 if absent);
      2. `repository.findActive()` → if present: `current.deactivate(now)` + **`repository.saveNow(current)`**
         (immediate UPDATE — Postgres checks the non-deferrable partial unique index per
         statement; without the explicit flush Hibernate's commit-time ordering could emit the
         new ACTIVE row first and violate the index);
      3. `draft.activate(repository.nextVersionNumber(), sha256(content), now)` + `repository.save(draft)`.
      `now` from the injected `Clock`. Sha256 via a small `ContentHasher` application component
      (`HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")...)`).
      Losing a concurrent activation race surfaces as `DataIntegrityViolationException`
      (unique index) or `ObjectOptimisticLockingFailureException` (lock_version) — both mapped to 409.
    * `DeleteAgreementTemplateUseCase` / service — loads, `ensureDeletable()`, deletes.
    * `GetAgreementTemplateUseCase` / `FindAgreementTemplatesUseCase` / `GetActiveAgreementTemplateUseCase`
      — queries; active absent → `ActiveAgreementTemplateNotFoundException` → 404.

### Web (`agreement/web/`)

* **`AgreementTemplateCommandController`** *(new, `web/command/`)* — base path `/api/agreements`:
    * `POST /api/agreements` `{title, content}` → 201 + full DTO
    * `PATCH /api/agreements/{id}` `{title, content}` → 200 (409 unless DRAFT)
    * `PATCH /api/agreements/{id}/activate` (no body) → 200 (409 unless DRAFT / concurrent)
    * `DELETE /api/agreements/{id}` → 204 (409 unless DRAFT)
* **`AgreementTemplateQueryController`** *(new, `web/query/`)*:
    * `GET /api/agreements` → 200, list of summary DTOs (id, versionNumber, title, status,
      createdAt, activatedAt, deactivatedAt — WITHOUT content)
    * `GET /api/agreements/{id}` → 200 detail DTO with content (404 if absent)
    * `GET /api/agreements/active` → 200 detail DTO with content (404 if none)
* **DTO records** (`web/*/dto/`): `AgreementTemplateRequest(@NotBlank @Size(max=255) title,
  @NotBlank content)`, `AgreementTemplateResponse`, `AgreementTemplateSummaryResponse`; MapStruct
  `AgreementTemplateWebMapper` (uses `InstantMapper`).
* **`AgreementRestControllerAdvice`** *(new, `web/error/`)* —
  `@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.agreement")`,
  `@Order(Ordered.LOWEST_PRECEDENCE - 1)`, same `correlationId`/`errorCode` pattern as
  `EquipmentRestControllerAdvice`:
    * `AgreementTemplateNotEditableException` / `NotActivatableException` / `NotDeletableException` → **409**
    * `ActiveAgreementTemplateNotFoundException` → **404**
    * `DataIntegrityViolationException` on activation → **409** with code
      `agreement.template.concurrent_activation` (handler scoped to this module's package only).

### Module declaration

* **`agreement/package-info.java`** *(new)*:
  ```java
  @org.springframework.modulith.ApplicationModule(
          displayName = "Agreement Module",
          allowedDependencies = {"shared", "customer", "rental"})
  package com.github.jenkaby.bikerental.agreement;
  ```

---

## 3. Abstract Data Schema Changes

New table `agreement_templates` (see above). No changes to existing tables.

---

## 4. Component Contracts & Payloads

* `POST /api/agreements` → 201
  `{"id":1,"versionNumber":null,"title":"...","content":"...","status":"DRAFT","createdAt":"...","activatedAt":null,"deactivatedAt":null}`
* `PATCH /api/agreements/{id}/activate` → 200 (same DTO, `status=ACTIVE`, `versionNumber` set)
* Conflict shape:
  ```
  HTTP 409
  {"status":409, "detail":"...", "correlationId":"<uuid>", "errorCode":"agreement.template.not_editable"}
  ```
* `GET /api/agreements` — summary list; the JPQL/projection explicitly selects only summary
  columns so the TEXT `content` column is never fetched (by construction).

---

## 5. Updated Interaction Sequence

### Activation with an existing active version

1. `PATCH /api/agreements/7/activate`.
2. Service loads draft #7; loads active #3.
3. `#3.deactivate(now)`; `saveNow(#3)` → immediate `UPDATE ... SET status='DEACTIVATED'` —
   partial unique index now has no ACTIVE row.
4. `#7.activate(4, sha256, now)`; `save(#7)` → flush at commit inserts/updates the new ACTIVE row.
5. Response 200; `GET /api/agreements/active` now returns #7.

### Concurrent activation (two admins)

1. Both transactions load the same active #3 and different drafts.
2. First commits fully.
3. Second: either `lock_version` of #3 changed → `ObjectOptimisticLockingFailureException`, or its
   ACTIVE insert violates `uq_agreement_templates_single_active` → `DataIntegrityViolationException`.
4. Both → 409 to the caller; state remains consistent (one ACTIVE).

---

## 6. Non-Functional Architecture Decisions

* **DDD:** flush ordering is expressed as the port method `saveNow` (domain-language contract:
  "persist this state change immediately"); JPA `saveAndFlush` stays in the adapter.
* **Single ACTIVE invariant** is owned by the database (partial unique index) with the service
  ordering merely avoiding false positives; application-level checks are conveniences, not the
  source of truth.
* **Testing:** Cucumber component tests (`features/agreement/agreement-template.feature`) for FR
  scenarios 1–6; WebMvc `@ApiTest` tests for request validation of both controllers. No unit
  tests on domain/service classes (project rule).
