<task_file_template>

# Task 012: Catalogue `agreement.pdf.rendering_failed` in `docs/error-codes.md`

> **Applied Skill:** `.claude/rules/error-responses.md` — every `errorCode` MUST be documented in the catalogue in the
> same change that introduces it. Depends on Task 005 (the exception) and Task 006 (the handler).

## 1. Objective

Add a catalogue entry for the new 500 error code so the public error-code contract stays in sync.

## 2. File to Modify / Create

* **File Path:** `docs/error-codes.md`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None (Markdown documentation).

**Code to Add/Replace:**

* **Location:** In the `## \`agreement.*\` — agreement templates` section, immediately AFTER the entire
  `### \`agreement.template.no_active\`` entry (its heading, bullet paragraph, and the closing ` ``` ` of its JSON
  example block). Add the new subsection below.
* **Snippet:**

```markdown
### `agreement.pdf.rendering_failed`
- **HTTP:** 500 · **Trigger:** an `IOException` while rendering the agreement PDF
  (`POST /api/agreements/preview-pdf`, or the FR-05 signing flow) via
  `AgreementPdfRenderingException`. Not expected in normal operation. · **Extra:** none.

```json
{
  "status": 500,
  "detail": "Failed to render the agreement PDF document",
  "correlationId": "018f...",
  "errorCode": "agreement.pdf.rendering_failed"
}
```
```

> Keep the fenced ` ```json ` block intact when copying. Do NOT reorder or edit the existing agreement entries.

## 4. Validation Steps

No build applies to a docs-only change. Verify the file renders as valid Markdown (matching heading level `###` and a
fenced `json` block) and that the new entry sits inside the `agreement.*` section.

```bash
grep -n "agreement.pdf.rendering_failed" docs/error-codes.md
```

</task_file_template>
