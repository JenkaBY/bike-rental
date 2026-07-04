<task_file_template>

# Task 023: Document the agreement error codes

> **Applied Skill:** `error-responses` rule — every `errorCode` must be catalogued in
> `docs/error-codes.md` in the SAME change that introduces it, matching the existing entry format
> (heading `### \`code\``, HTTP/Trigger line, example JSON). These module codes are surfaced by the
> module advice (Task 018), not held in `ErrorCodes`.

## 1. Objective

Add a new `agreement.*` section documenting the four domain codes plus the concurrent-activation code.

## 2. File to Modify / Create

* **File Path:** `docs/error-codes.md`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** Immediately AFTER the `## \`identity.*\` — authentication & accounts` section's last
entry (the `### \`identity.password.invalid_current\`` block and its JSON fence) and BEFORE the
`---` separator that precedes `## Field-level validation codes (\`errors[].code\`)`.

**Snippet to add:**

```markdown
---

## `agreement.*` — agreement templates

### `agreement.template.not_editable`
- **HTTP:** 409 · **Trigger:** editing (`PATCH /api/agreements/{id}`) a template that is not `DRAFT`
  (`AgreementTemplateNotEditableException`). · **Extra:** `params` = `{currentStatus}`.

```json
{
  "status": 409,
  "detail": "Cannot edit agreement template in status ACTIVE. Only DRAFT templates are editable",
  "correlationId": "018f...",
  "errorCode": "agreement.template.not_editable",
  "params": { "currentStatus": "ACTIVE" }
}
```

### `agreement.template.not_activatable`
- **HTTP:** 409 · **Trigger:** activating (`PATCH /api/agreements/{id}/activate`) a template that is
  not `DRAFT`, or the previously-active template not being `ACTIVE` during the activation flow
  (`AgreementTemplateNotActivatableException`). · **Extra:** `params` = `{currentStatus}`.

```json
{
  "status": 409,
  "detail": "Cannot activate agreement template in status DEACTIVATED. Only DRAFT templates are activatable",
  "correlationId": "018f...",
  "errorCode": "agreement.template.not_activatable",
  "params": { "currentStatus": "DEACTIVATED" }
}
```

### `agreement.template.not_deletable`
- **HTTP:** 409 · **Trigger:** deleting (`DELETE /api/agreements/{id}`) a template that is not `DRAFT`
  (`AgreementTemplateNotDeletableException`). · **Extra:** `params` = `{currentStatus}`.

```json
{
  "status": 409,
  "detail": "Cannot delete agreement template in status ACTIVE. Only DRAFT templates are deletable",
  "correlationId": "018f...",
  "errorCode": "agreement.template.not_deletable",
  "params": { "currentStatus": "ACTIVE" }
}
```

### `agreement.template.no_active`
- **HTTP:** 404 · **Trigger:** `GET /api/agreements/active` when no `ACTIVE` template exists
  (`ActiveAgreementTemplateNotFoundException`). · **Extra:** none.

```json
{
  "status": 404,
  "detail": "No active agreement template exists",
  "correlationId": "018f...",
  "errorCode": "agreement.template.no_active"
}
```

### `agreement.template.concurrent_activation`
- **HTTP:** 409 · **Trigger:** two admins activate concurrently and the loser violates the partial
  unique index `uq_agreement_templates_single_active` (`DataIntegrityViolationException` scoped to the
  agreement module advice). · **Extra:** none. The client should retry.

```json
{
  "status": 409,
  "detail": "Another agreement template was activated concurrently. Please retry.",
  "correlationId": "018f...",
  "errorCode": "agreement.template.concurrent_activation"
}
```
```

## 4. Validation Steps

No build step. Verify the edit by confirming the new `## \`agreement.*\`` section renders between the
`identity.*` section and the `## Field-level validation codes` section, and that all five headings are
present.

```bash
grep -n "agreement.template" docs/error-codes.md
```

</task_file_template>
