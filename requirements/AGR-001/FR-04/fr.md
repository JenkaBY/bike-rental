# User Story: FR-04 - Agreement PDF Renderer & Admin Preview

## 1. Description

**As an** administrator editing agreement templates
**I want to** preview any (possibly unsaved) agreement text as the exact PDF the customer will sign
**So that** I can verify layout, wording and Cyrillic rendering before activating a template, and the production signing flow (FR-05) reuses the very same renderer

## 2. Context & Business Rules

* **Trigger:** Admin clicks "Preview PDF" in the template editor → `POST /api/agreements/preview-pdf` with `{content, title}` and `Accept: application/pdf`.
* **Rules Enforced:**
    * PDF is produced with Apache PDFBox from plain text (no Markdown/HTML pipeline): manual
      word-wrap by page width, paragraph breaks on newlines, automatic pagination.
    * A TTF font with Cyrillic support (DejaVu Sans) is embedded from application resources —
      the standard 14 PDF fonts have no Cyrillic.
    * Document content order: agreement title + text, a data block (customer full name, phone;
      rental start date = signing moment; planned duration; equipment list: uid, name, per-unit
      estimated cost; total computed on the fly), the signature image, and metadata
      (signing date/time, rental number).
    * The same renderer serves production signing (FR-05) and admin preview; preview feeds it
      fixture data (sample customer, sample equipment set) and a placeholder rectangle instead of
      a signature image.
    * No hash is embedded inside the document (the sha256 is computed FROM the finished PDF in FR-05).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** preview generation under 2 seconds for a typical agreement text (a few pages).
* **Security/Compliance:** preview accepts arbitrary text but renders it inertly (plain text only).
* **Usability/Other:** response `Content-Type: application/pdf`; generated PDF opens correctly in
  standard viewers with Cyrillic glyphs intact.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Preview an unsaved Cyrillic text**

* **Given** an admin has typed agreement text containing Cyrillic characters (not saved)
* **When** they POST `{content, title}` to `/api/agreements/preview-pdf` with `Accept: application/pdf`
* **Then** the response is `200` with `Content-Type: application/pdf`, the body is a non-empty
  valid PDF, and extracting its text yields the Cyrillic content and the fixture data block

**Scenario 2: Long text paginates**

* **Given** agreement content long enough to exceed one page
* **When** the admin requests a preview
* **Then** the produced PDF has more than one page and no text is lost

**Scenario 3: Validation**

* **Given** a preview request with blank `content`
* **When** it is POSTed
* **Then** the response is `400` with the standard validation error body

## 5. Out of Scope

* Persisting anything (preview is stateless).
* Signature persistence and the signing endpoint (FR-05).
* Rich text formatting of any kind.
