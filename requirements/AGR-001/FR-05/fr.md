# User Story: FR-05 - Customer Signs the Rental Agreement

## 1. Description

**As a** customer (assisted by an operator)
**I want to** sign the active rental agreement for my prepared rental with a hand-drawn signature
**So that** the rental becomes ACTIVE with a legally meaningful, immutable PDF record of exactly what I signed

## 2. Context & Business Rules

* **Trigger:** Frontend POSTs the canvas-drawn signature to `POST /api/rentals/{rentalId}/signatures`.
* **Rules Enforced:**
    * Signing is allowed ONLY while the rental is `AWAITING_SIGNATURE` (via `RentalSigningFacade`).
    * The whole operation is ONE transaction: validations → PDF generation → signature insert →
      synchronous `rental.completeSigning` → (after commit) `AgreementSignedEvent`.
    * One signature per rental (DB unique on `rental_id`); repeat attempt → 409 `agreement.signing.already_signed`.
    * The submitted `rentalVersion` must equal the rental's current version (one-time fencing
      token from the lifecycle response) → mismatch → 409 `agreement.signing.rental_version_mismatch`.
    * The submitted `templateId` must be the CURRENT ACTIVE template → otherwise 409
      `agreement.template.not_active`; no active template at all → 409 `agreement.template.no_active`.
    * Customer data (name, phone) and rental data for the PDF are taken ONLY from the server
      (`CustomerFacade`, `RentalSigningFacade`) — never from the request body.
    * A single `Clock` instant is used for `signed_at`, the rental's `startedAt` and the date in
      the PDF.
    * The signature row stores: template reference + `template_content_sha256`, the signing
      snapshot (JSONB: customer + rental data that went into the PDF, incl. the informational
      rental version), the PDF (BYTEA) + its sha256, the raw PNG, `operator_id`, `ip_address`
      (X-Forwarded-For aware) and `user_agent`.
    * Losing the race against cancel-signing → 409 `shared.resource.optimistic_lock`.
    * Rental does NOT listen to `AgreementSignedEvent` (its state is already changed synchronously);
      the event is a future extension point (notifications, finance).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** signing completes within a few seconds including PDF generation.
* **Security/Compliance:** the stored PDF and sha256 provide tamper evidence; the PNG and PDF are
  never returned in list responses.
* **Usability/Other:** listing and download share one URL via `Accept` content negotiation.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Happy path**

* **Given** a rental in `AWAITING_SIGNATURE` (version V) and an ACTIVE template
* **When** the client POSTs `{signaturePng, rentalVersion: V, templateId, operatorId}`
* **Then** the response is `201 {signatureId, signedAt}`; the rental is `ACTIVE` with
  `startedAt == signedAt`; the stored PDF is non-empty, its sha256 matches `pdf_sha256`, and its
  extracted text contains the customer name and equipment uids

**Scenario 2: Version mismatch**

* **Given** a rental in `AWAITING_SIGNATURE` whose version advanced after the token was issued
* **When** the client POSTs with the stale `rentalVersion`
* **Then** `409` with `errorCode = agreement.signing.rental_version_mismatch` and no signature row exists

**Scenario 3: Already signed**

* **Given** a rental that already has a signature
* **When** a second signature is POSTed
* **Then** `409` with `errorCode = agreement.signing.already_signed`

**Scenario 4: Wrong rental state**

* **Given** a rental in `DRAFT` (or `ACTIVE`)
* **When** a signature is POSTed
* **Then** `409` with `errorCode = agreement.signing.rental_not_awaiting_signature`

**Scenario 5: Template checks**

* **Given** a rental in `AWAITING_SIGNATURE`
* **When** the client POSTs with a `templateId` that is not the current active template
* **Then** `409` with `errorCode = agreement.template.not_active` (or
  `agreement.template.no_active` when no active template exists)

**Scenario 6: Listing and download**

* **Given** a signed rental
* **When** the client GETs `/api/rentals/{id}/signatures` with `Accept: application/json`
* **Then** `200` with an array of exactly one DTO (signatureId, templateId,
  templateVersionNumber, signedAt) without any binary fields
* **When** the client GETs the same URL with `Accept: application/pdf`
* **Then** `200`, `Content-Type: application/pdf`, `Content-Disposition: attachment`, body equals
  the stored PDF; for an unsigned rental the PDF request returns `404` and the JSON request
  returns `200 []`

## 5. Out of Scope

* Removing the direct activation path (FR-06).
* Any consumer of `AgreementSignedEvent`.
* Re-signing / signature invalidation flows.
