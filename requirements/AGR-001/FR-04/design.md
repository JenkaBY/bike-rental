# System Design: FR-04 — Agreement PDF Renderer & Admin Preview

## 1. Architectural Overview

Adds a reusable PDF rendering capability to the `agreement` module (created in FR-03) and exposes
it through a stateless admin preview endpoint. The renderer is a domain port implemented in the
infrastructure layer (the application layer may not depend on infrastructure per
`ModulithBoundariesTest`); Spring injects the implementation at runtime.

```
web (preview controller) → application (PreviewAgreementPdfService, fixture data)
                          → domain port AgreementPdfRenderer ← infrastructure/pdf (PDFBox impl)
FR-05 signing service reuses the same port with real data.
```

---

## 2. Impacted Components

### Build & resources

* **`gradle/libs.versions.toml`**: add `pdfbox = "3.0.5"` under `[versions]` and
  `pdfbox = { module = "org.apache.pdfbox:pdfbox", version.ref = "pdfbox" }` under `[libraries]`.
* **`service/build.gradle`**: `implementation libs.pdfbox`.
* **`service/src/main/resources/fonts/DejaVuSans.ttf`** *(new binary resource)* — provisioned by
  the orchestrator (downloaded from the DejaVu release, public-domain-like license), NOT by a dev
  task. Dev tasks must reference the classpath location `fonts/DejaVuSans.ttf` and may assume the
  file exists.

### Domain (`agreement/domain/`)

* **`AgreementPdfRenderer`** *(new port interface, `domain/service/`)*:
  ```java
  byte[] render(AgreementPdfData data);
  ```
* **`AgreementPdfData`** *(new record, `domain/model/` or next to the port)* — everything the
  document needs, framework-free:
  `String title, String content, CustomerData customer, RentalData rental, byte[] signaturePng`
  with nested records:
  `CustomerData(String firstName, String lastName, String phone)`,
  `RentalData(Long rentalId, LocalDateTime startedAt, Duration plannedDuration, List<EquipmentLine> equipments)`,
  `EquipmentLine(String uid, String name, BigDecimal estimatedCost)`.
  `signaturePng == null` ⇒ renderer draws a labeled placeholder rectangle ("место подписи")
  instead of an image — this is exactly the preview mode; no separate preview flag exists.

### Infrastructure (`agreement/infrastructure/pdf/`)

* **`PdfBoxAgreementRenderer`** *(new `@Component`, implements the port)*:
    * Loads the TTF once per document via `PDType0Font.load(document, inputStream, true)` from
      classpath `fonts/DejaVuSans.ttf` (embedded subset).
    * Layout constants: A4 (`PDRectangle.A4`), margins 50pt, body font 11pt, title 16pt bold-ish
      (same font, larger size), leading = fontSize * 1.4.
    * Word-wrap: split content into paragraphs by `\n`; greedy word wrapping using
      `font.getStringWidth(candidate) / 1000 * fontSize` against the printable width; empty line
      between paragraphs; page break when cursor reaches the bottom margin (new page +
      `PDPageContentStream` restart).
    * After the agreement text: data block rendered as labeled lines (customer full name, phone,
      start date/time formatted `dd.MM.yyyy HH:mm`, duration in human form `HH:mm`), then the
      equipment table as aligned text lines (uid — name — cost) with a computed total line.
    * Signature area: if `signaturePng != null` → `PDImageXObject.createFromByteArray`, drawn
      ~180x80pt; else a stroked rectangle with the placeholder label.
    * Metadata footer line: signing date/time + rental number (or fixture marker in preview).
    * Returns `ByteArrayOutputStream.toByteArray()`; any `IOException` wrapped into
      `AgreementPdfRenderingException` *(new domain exception, code `agreement.pdf.rendering_failed`)*
      → mapped to 500 by the module advice (added handler) — not expected in normal operation.

### Application (`agreement/application/`)

* **`PreviewAgreementPdfUseCase`** *(new interface)*: `byte[] execute(PreviewCommand(String title, String content))`.
* **`PreviewAgreementPdfService`** *(new)*: builds `AgreementPdfData` from the command plus
  hard-coded fixture data (constants in the service): sample customer "Иван Иванов,
  +375291234567", rentalId 0, startedAt = `LocalDateTime.now(clock)`, duration 2h, two sample
  equipment lines with costs; `signaturePng = null`. Delegates to the port. No transaction.

### Web (`agreement/web/command/`)

* **`AgreementTemplateCommandController`** gains:
  ```java
  @PostMapping(value = "/preview-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  ResponseEntity<byte[]> previewPdf(@Valid @RequestBody AgreementPdfPreviewRequest request)
  ```
  `AgreementPdfPreviewRequest(@NotBlank @Size(max = 255) String title, @NotBlank String content)`.
  Response: 200, `Content-Type: application/pdf`, body = rendered bytes. No caching headers needed.

---

## 3. Abstract Data Schema Changes

None.

---

## 4. Component Contracts & Payloads

* **HTTP Client → `POST /api/agreements/preview-pdf`**
    * Request: `Accept: application/pdf`, body `{"title": "...", "content": "..."}`.
    * `200` → binary PDF.
    * `400` → standard validation `ProblemDetail` (blank title/content).
* **application → `AgreementPdfRenderer`** (in-process): `AgreementPdfData` in, `byte[]` out;
  deterministic for identical input except embedded timestamps supplied by the caller (the
  renderer itself never reads the clock — testability + FR-05's single-instant rule).

---

## 5. Updated Interaction Sequence

1. Admin POSTs unsaved `{title, content}` to `/api/agreements/preview-pdf`.
2. Controller validates → `PreviewAgreementPdfService.execute`.
3. Service assembles fixture `AgreementPdfData` (null signature) → `PdfBoxAgreementRenderer.render`.
4. Renderer lays out title, wrapped Cyrillic text, fixture data block, equipment lines + total,
   placeholder signature rectangle, metadata line; embeds DejaVu Sans subset.
5. Controller returns the bytes as `application/pdf`.

---

## 6. Non-Functional Architecture Decisions

* **Clock discipline:** the renderer is pure (no clock, no randomness beyond PDF internals);
  timestamps come from callers — in FR-05 the same `Instant` feeds `signed_at`, `startedAt` and
  the PDF.
* **Font licensing:** DejaVu fonts are freely redistributable (Bitstream Vera + public domain
  additions); the TTF is committed to the repo as a resource.
* **Testing:** component test (`features/agreement/agreement-pdf-preview.feature`): POST preview
  with Cyrillic content → 200, content-type PDF, body parses with PDFBox and extracted text
  contains the Cyrillic phrase and fixture marker (step definition uses PDFBox `PDFTextStripper`
  from the test classpath — pdfbox must also be a `component-test` testImplementation dependency).
  WebMvc `@ApiTest` validation tests for blank title/content.
