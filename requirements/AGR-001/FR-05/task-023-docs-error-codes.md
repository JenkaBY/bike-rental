<task_file_template>

# Task 023: Catalogue the new signing error codes in docs/error-codes.md

> **Applied Skill:** `error-responses` — every `errorCode` is documented in `docs/error-codes.md` in the SAME change
> that adds it; match the existing entry format (heading, HTTP+trigger bullet, example JSON). Depends on Task 006.

## 1. Objective

Add catalogue entries for the five new signing error codes in the `agreement.*` section.

## 2. File to Modify / Create

* **File Path:** `docs/error-codes.md`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** Inside the `## `agreement.*` — agreement templates` section, immediately AFTER the existing
`### `agreement.template.concurrent_activation`` entry (its closing ``` fenced JSON block) and BEFORE the `---` line
that ends the section. Insert EXACTLY:

````markdown
### `agreement.signing.already_signed`
- **HTTP:** 409 · **Trigger:** POST `/api/rentals/{rentalId}/signatures` for a rental that already has a signature
  (`AgreementAlreadySignedException`). · **Extra:** `params` = `{rentalId}`.

```json
{
  "status": 409,
  "detail": "Rental 42 has already been signed",
  "correlationId": "018f...",
  "errorCode": "agreement.signing.already_signed",
  "params": { "rentalId": 42 }
}
```

### `agreement.template.not_active`
- **HTTP:** 409 · **Trigger:** POST `/api/rentals/{rentalId}/signatures` with a `templateId` that is not the current
  active template (`AgreementTemplateNotActiveException`). · **Extra:** `params` = `{requestedTemplateId, activeTemplateId}`.

```json
{
  "status": 409,
  "detail": "Template 3 is not the current active template. Active template is 5",
  "correlationId": "018f...",
  "errorCode": "agreement.template.not_active",
  "params": { "requestedTemplateId": 3, "activeTemplateId": 5 }
}
```

### `agreement.signing.rental_version_mismatch`
- **HTTP:** 409 · **Trigger:** POST `/api/rentals/{rentalId}/signatures` with a stale `rentalVersion`
  (`SigningVersionMismatchException`), or the synchronous `completeSigning` losing the version race
  (`RentalSigningVersionMismatchException`, rental module API). · **Extra:** for the fencing check `params` =
  `{rentalId, expectedVersion, actualVersion}`; for the completeSigning race, no `params`.

```json
{
  "status": 409,
  "detail": "Rental 42 version mismatch. Expected: 2, actual: 3",
  "correlationId": "018f...",
  "errorCode": "agreement.signing.rental_version_mismatch",
  "params": { "rentalId": 42, "expectedVersion": 2, "actualVersion": 3 }
}
```

### `agreement.signing.rental_not_awaiting_signature`
- **HTTP:** 409 · **Trigger:** POST `/api/rentals/{rentalId}/signatures` while the rental is not `AWAITING_SIGNATURE`
  (`RentalNotAwaitingSignatureException`, rental module API). · **Extra:** none.

```json
{
  "status": 409,
  "detail": "Rental 42 is not awaiting signature. Current status: DRAFT",
  "correlationId": "018f...",
  "errorCode": "agreement.signing.rental_not_awaiting_signature"
}
```

### `agreement.signing.invalid_signature_image`
- **HTTP:** 400 · **Trigger:** POST `/api/rentals/{rentalId}/signatures` with a `signaturePng` that is not valid
  base64-encoded PNG data (`InvalidSignatureImageException`). · **Extra:** none.

```json
{
  "status": 400,
  "detail": "The submitted signature image is not valid base64-encoded PNG data",
  "correlationId": "018f...",
  "errorCode": "agreement.signing.invalid_signature_image"
}
```

````

## 4. Validation Steps

This is a docs-only change; no build is required. Verify the file still renders (the inserted block is inside the
`agreement.*` section, before its closing `---`).

```bash
git diff --stat docs/error-codes.md
```

</task_file_template>
