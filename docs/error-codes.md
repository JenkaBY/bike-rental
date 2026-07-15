# API Error Codes

Every error response in this service is an [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `ProblemDetail`
(`application/problem+json`) enriched with two mandatory custom properties and, depending on the case, extra fields.
This document catalogues every code declared in
[`ErrorCodes`](../service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/ErrorCodes.java).

> **Maintenance rule:** whenever you add or change an entry in `ErrorCodes` (or the `params` an error carries), update
> this catalogue in the same change. Enforced by [.claude/rules/error-responses.md](../.claude/rules/error-responses.md).

## Response envelope

| Field           | Always present | Meaning                                                                        |
|-----------------|:--------------:|--------------------------------------------------------------------------------|
| `type`          | yes            | `about:blank` (no problem-type registry yet)                                    |
| `title`         | yes            | Short, status-derived or handler-set title                                      |
| `status`        | yes            | HTTP status code                                                                |
| `detail`        | yes            | Human-readable message (safe to show to the user)                               |
| `instance`      | yes            | Request path                                                                    |
| `correlationId` | **yes**        | Request id from `X-Correlation-ID` (or generated UUIDv7); also echoed as header |
| `errorCode`     | **yes**        | Stable machine code from this catalogue — the key the frontend branches on      |
| `errors`        | validation     | Array of per-field `{field, code, params}` (see [error-responses rule](../.claude/rules/error-responses.md)) |
| `params`        | some           | Structured context object specific to the error                                 |

The frontend should branch on `errorCode` (and `errors[].code` for field-level validation), never on `detail`.

---

## `shared.*` — cross-cutting

### `shared.method_arguments.validation_failed`
- **HTTP:** 400 · **Trigger:** `@RequestBody @Valid` bean validation failed (`MethodArgumentNotValidException`).
- **Extra:** `errors[]` — one `{field, code, params}` per violation; `params` carries the constraint attributes.

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation error",
  "instance": "/api/tariffs",
  "correlationId": "018f...-uuidv7",
  "errorCode": "shared.method_arguments.validation_failed",
  "errors": [
    { "field": "name", "code": "validation.size", "params": { "min": 2, "max": 50 } },
    { "field": "age",  "code": "validation.min",  "params": { "value": 18 } }
  ]
}
```

### `shared.request.method_parameters_invalid`
- **HTTP:** 400 · **Trigger:** constraints on `@RequestParam` / `@PathVariable` / `@RequestHeader`
  (`HandlerMethodValidationException`).
- **Extra:** `errors[]` of `{field, code, params}`; `field` is the parameter name.

```json
{
  "status": 400,
  "detail": "Validation error",
  "correlationId": "018f...",
  "errorCode": "shared.request.method_parameters_invalid",
  "errors": [ { "field": "page", "code": "validation.min", "params": { "value": 1 } } ]
}
```

### `shared.request.constraint_violation`
- **HTTP:** 400 · **Trigger:** method-level `@Validated` constraints (`ConstraintViolationException`).
- **Extra:** `errors[]` of `{field, code, params}`.
- **Reused (without `errors[]`)** by rental `InvalidDateRangeException`, which sets only `detail`.

```json
{
  "status": 400,
  "detail": "Bad Request",
  "correlationId": "018f...",
  "errorCode": "shared.request.constraint_violation",
  "errors": [ { "field": "customerId", "code": "validation.positive", "params": {} } ]
}
```

### `shared.request.type_mismatch`
- **HTTP:** 400 · **Trigger:** a path/query value can't be converted to the target type
  (`MethodArgumentTypeMismatchException`). · **Extra:** none.

```json
{
  "status": 400,
  "detail": "Failed to convert value 'abc' to required type 'Long'",
  "correlationId": "018f...",
  "errorCode": "shared.request.type_mismatch"
}
```

### `shared.request.param_missing`
- **HTTP:** 400 · **Trigger:** a required `@RequestParam` is absent
  (`MissingServletRequestParameterException`). · **Extra:** none.

```json
{
  "status": 400,
  "detail": "Required request parameter 'status' for method parameter type String is not present",
  "correlationId": "018f...",
  "errorCode": "shared.request.param_missing"
}
```

### `shared.api.version_missing`
- **HTTP:** 400 (status from Spring) · **Trigger:** required API version missing (`MissingApiVersionException`).
- **Extra:** none.

```json
{ "status": 400, "detail": "API version is required", "correlationId": "018f...", "errorCode": "shared.api.version_missing" }
```

### `shared.api.version_invalid`
- **HTTP:** 400 (status from Spring) · **Trigger:** unsupported API version requested (`InvalidApiVersionException`).
- **Extra:** none.

```json
{ "status": 400, "detail": "Invalid API version", "correlationId": "018f...", "errorCode": "shared.api.version_invalid" }
```

### `shared.request.not_readable`
- **HTTP:** 400 · **Trigger:** malformed or missing request body (`HttpMessageNotReadableException`). · **Extra:** none.

```json
{ "status": 400, "detail": "Malformed or missing request body", "correlationId": "018f...", "errorCode": "shared.request.not_readable" }
```

### `shared.request.method_not_allowed`
- **HTTP:** 405 · **Trigger:** HTTP method not supported for the route (`HttpRequestMethodNotSupportedException`).
- **Extra:** none.

```json
{ "status": 405, "detail": "Method 'DELETE' is not supported", "correlationId": "018f...", "errorCode": "shared.request.method_not_allowed" }
```

### `shared.request.media_type_not_supported`
- **HTTP:** 415 · **Trigger:** request `Content-Type` not supported (`HttpMediaTypeNotSupportedException`).
- **Extra:** none.

```json
{ "status": 415, "detail": "Content-Type 'text/plain' is not supported", "correlationId": "018f...", "errorCode": "shared.request.media_type_not_supported" }
```

### `shared.server.internal_error`
- **HTTP:** 500 · **Trigger:** any unhandled exception (catch-all `Exception` handler). · **Extra:** none.

```json
{
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Internal Server Error",
  "correlationId": "018f...",
  "errorCode": "shared.server.internal_error"
}
```

### `shared.resource.not_found`
- **HTTP:** 404 · **Trigger:** an entity is not found by id (`ResourceNotFoundException`) or no route matches
  (`NoResourceFoundException`).
- **Extra:** `params` = `{resourceName, identifier}` for `ResourceNotFoundException`; absent for `NoResourceFoundException`.

```json
{
  "status": 404,
  "detail": "Customer with identifier '42' not found",
  "correlationId": "018f...",
  "errorCode": "shared.resource.not_found",
  "params": { "resourceName": "Customer", "identifier": "42" }
}
```

### `shared.reference.not_found`
- **HTTP:** 422 · **Trigger:** a referenced entity in the payload does not exist (`ReferenceNotFoundException`).
- **Extra:** `params` = `{resourceName, identifier}`.

```json
{
  "status": 422,
  "detail": "Referenced Equipment with identifier '7' not found",
  "correlationId": "018f...",
  "errorCode": "shared.reference.not_found",
  "params": { "resourceName": "Equipment", "identifier": "7" }
}
```

### `shared.resource.conflict`
- **HTTP:** 409 · **Trigger:** creating a resource that already exists (`ResourceConflictException`).
- **Extra:** `params` = `{resourceName, identifier}`.

```json
{
  "status": 409,
  "detail": "Customer with identifier '+49123456' already exists",
  "correlationId": "018f...",
  "errorCode": "shared.resource.conflict",
  "params": { "resourceName": "Customer", "identifier": "+49123456" }
}
```

### `shared.resource.optimistic_lock`
- **HTTP:** 409 · **Trigger:** concurrent update lost the optimistic-lock check
  (`ObjectOptimisticLockingFailureException`). · **Extra:** none. The client should retry.

```json
{
  "title": "Optimistic lock",
  "status": 409,
  "detail": "Concurrent update — please retry",
  "correlationId": "018f...",
  "errorCode": "shared.resource.optimistic_lock"
}
```

### `shared.equipment.not_available`
- **HTTP:** 422 · **Trigger:** equipment exists but is not in `GOOD` state — e.g. in maintenance or decommissioned
  (`EquipmentNotAvailableException`). · **Extra:** `params` = `{identifiers}` (array of equipment ids).
- Distinct from the rental code `rental.equipment.not_available` (409), which means the equipment is *occupied* by an
  active rental rather than out of service.

```json
{
  "status": 422,
  "detail": "Equipments with ids [7, 8] is not in GOOD state",
  "correlationId": "018f...",
  "errorCode": "shared.equipment.not_available",
  "params": { "identifiers": [7, 8] }
}
```

---

## `finance.*`

### `finance.insufficient_balance`
- **HTTP:** 422 · **Trigger:** `InsufficientBalanceException` raised by a **finance** endpoint (deposit / withdrawal /
  adjustment). · **Extra:** `params` = `{available, requested}` as `BigDecimal` amounts.

```json
{
  "status": 422,
  "detail": "Insufficient wallet balance. Available: 10.00, requested deduction: 25.00",
  "correlationId": "018f...",
  "errorCode": "finance.insufficient_balance",
  "params": { "available": 10.00, "requested": 25.00 }
}
```

### `finance.over_budget_settlement`
- **Internal by design — not surfaced over HTTP.** `OverBudgetSettlementException` is caught inside the settlement
  services (`@Transactional noRollbackFor` + `try/catch` in `ReturnEquipmentService` / `SettleDebtRentalsService`) and
  resolved as a domain outcome (the rental moves to debt), so it never reaches a client. The code exists on the
  exception (`Details = {finalCost, availableAmount}`) for logging/traceability; document a payload example here if an
  `@ExceptionHandler` is ever added.

---

## `rental.*`

### `rental.insufficient_funds`
- **HTTP:** 422 · **Trigger:** `InsufficientBalanceException` raised by a **rental** endpoint (creation / hold). The
  module-scoped `RentalRestControllerAdvice` overrides the code to this value (vs. `finance.insufficient_balance`).
- **Extra:** `params` = `{available, requested}` as `BigDecimal` amounts (same shape as `finance.insufficient_balance`).

```json
{
  "status": 422,
  "detail": "Insufficient wallet balance. Available: 5.00, requested deduction: 12.50",
  "correlationId": "018f...",
  "errorCode": "rental.insufficient_funds",
  "params": { "available": 5.00, "requested": 12.50 }
}
```

### `rental.hold.required`
- **HTTP:** 409 · **Trigger:** an operation requires an existing payment hold (`HoldRequiredException`).
- **Extra:** none (rental id is logged, not returned).

```json
{
  "status": 409,
  "detail": "A payment hold is required before activating this rental",
  "correlationId": "018f...",
  "errorCode": "rental.hold.required"
}
```

### `rental.equipment.not_available`
- **HTTP:** 409 · **Trigger:** requested equipment is already occupied by another rental
  (`EquipmentOccupiedException`). · **Extra:** `unavailableIds` (array of equipment ids).

```json
{
  "status": 409,
  "detail": "Equipment [7, 8] is occupied by another rental",
  "correlationId": "018f...",
  "errorCode": "rental.equipment.not_available",
  "params": {"unavailableIds": [7, 8]}
}
```

> **Sibling code:** equipment that exists but is *out of service* (`EquipmentNotAvailableException`) returns the
> distinct `shared.equipment.not_available` (HTTP 422) — see the `shared.*` section above.

### `rental.window.elapsed`
- **HTTP:** 422 · **Trigger:** equipment was requested to be added to an ACTIVE rental whose expected return time
  has already passed (`RentalWindowElapsedException`), leaving no billable window for the new item.
- **Extra:** `params` = `{rentalId, expectedReturnAt, now}`.

```json
{
  "status": 422,
  "detail": "Cannot add equipment to rental 10: rental window has elapsed. Expected return at 2026-02-10T10:00, now 2026-02-10T11:00",
  "correlationId": "018f...",
  "errorCode": "rental.window.elapsed",
  "params": {"rentalId": 10, "expectedReturnAt": "2026-02-10T10:00:00", "now": "2026-02-10T11:00:00"}
}
```

### `rental.quote.mismatch`
- **HTTP:** 409 · **Trigger:** a cost quote presented at return confirmation
  (`POST /api/rentals/{rentalId}/returns`) is inconsistent with the current state of the rental — the equipment
  composition, planned duration, discount, or special tariff/price differs, or the quote is an estimate
  (`QuoteRentalMismatchException`). Indicates a stale quote that must be re-taken.
- **Extra:** `params` = `{quoteId, rentalId, reason}`.

```json
{
  "status": 409,
  "detail": "Cost quote 'a1b2...' is inconsistent with rental 10: equipment composition differs from the rental",
  "correlationId": "018f...",
  "errorCode": "rental.quote.mismatch",
  "params": {"quoteId": "a1b2c3d4-...", "rentalId": 10, "reason": "equipment composition differs from the rental"}
}
```

### `rental.completion.flow_violation`
- **HTTP:** 409 · **Trigger:** `POST /api/rentals/return` was called with the last outstanding piece of equipment on
  the rental, which would complete it directly (`RentalCompletionFlowViolationException`). Rentals may only be
  completed via the quote-based return flow (`POST /api/rentals/{rentalId}/returns`); the legacy endpoint still
  accepts partial returns but rejects the return that would finalize the rental.
- **Extra:** `params` = `{rentalId}`.

```json
{
  "status": 409,
  "detail": "Rental 10 cannot be completed directly via POST /api/rentals/return; return the last equipment via the quote-based confirmation flow (POST /api/rentals/{rentalId}/returns)",
  "correlationId": "018f...",
  "errorCode": "rental.completion.flow_violation",
  "params": {"rentalId": 10}
}
```

---

## `tariff.*` — cost quotes

Thrown by the tariff module and surfaced by `RentalRestControllerAdvice` (return confirmation flow) and
`TariffRestControllerAdvice` (`/api/tariffs/quotes/**` endpoints).

### `tariff.quote.not_found`
- **HTTP:** 404 · **Trigger:** no cost quote exists for the supplied id (`QuoteNotFoundException`), thrown when
  fetching/consuming a quote during return confirmation, or when calling `DELETE /api/tariffs/quotes/{id}`.
- **Extra:** `params` = `{quoteId}`.

```json
{
  "status": 404,
  "detail": "Cost quote 'a1b2c3d4-...' not found",
  "correlationId": "018f...",
  "errorCode": "tariff.quote.not_found",
  "params": {"quoteId": "a1b2c3d4-..."}
}
```

### `tariff.quote.expired`
- **HTTP:** 410 · **Trigger:** the cost quote has passed its validity window (`QuoteExpiredException`); the TTL is
  configured via `app.tariff.quote-ttl` (default 5m).
- **Extra:** `params` = `{quoteId, expiresAt}`.

```json
{
  "status": 410,
  "detail": "Cost quote 'a1b2c3d4-...' expired at 2026-02-10T10:05:00Z",
  "correlationId": "018f...",
  "errorCode": "tariff.quote.expired",
  "params": {"quoteId": "a1b2c3d4-...", "expiresAt": "2026-02-10T10:05:00Z"}
}
```

### `tariff.quote.already_consumed`
- **HTTP:** 409 · **Trigger:** the quote is single-use and was already consumed by a prior successful confirmation
  (`QuoteAlreadyConsumedException`).
- **Extra:** `params` = `{quoteId}`.

```json
{
  "status": 409,
  "detail": "Cost quote 'a1b2c3d4-...' has already been consumed",
  "correlationId": "018f...",
  "errorCode": "tariff.quote.already_consumed",
  "params": {"quoteId": "a1b2c3d4-..."}
}
```

---

## `identity.*` — authentication & accounts

### `identity.authentication.required`
- **HTTP:** 401 · **Trigger:** a protected `/api/**` endpoint was called without a valid access token
  (resource-server `AuthenticationEntryPoint`). · **Extra:** none.

```json
{
  "status": 401,
  "detail": "Authentication required",
  "correlationId": "018f...",
  "errorCode": "identity.authentication.required"
}
```

### `identity.access.denied`
- **HTTP:** 403 · **Trigger:** the authenticated principal lacks the role required for the endpoint
  (resource-server `AccessDeniedHandler`, e.g. a non-admin calling `/api/auth/users/**`). · **Extra:** none.

```json
{
  "status": 403,
  "detail": "Access denied",
  "correlationId": "018f...",
  "errorCode": "identity.access.denied"
}
```

### `identity.username.duplicate`
- **HTTP:** 409 · **Trigger:** creating an account with an already-used username (`DuplicateUsernameException`).
- **Extra:** `params` = `{resourceName, identifier}`.

```json
{
  "status": 409,
  "detail": "User with identifier 'j.doe' already exists",
  "correlationId": "018f...",
  "errorCode": "identity.username.duplicate",
  "params": { "resourceName": "User", "identifier": "j.doe" }
}
```

### `identity.email.duplicate`
- **HTTP:** 409 · **Trigger:** creating an account with an already-used email (`DuplicateEmailException`).
- **Extra:** `params` = `{resourceName, identifier}`.

```json
{
  "status": 409,
  "detail": "User with identifier 'j.doe@example.com' already exists",
  "correlationId": "018f...",
  "errorCode": "identity.email.duplicate",
  "params": { "resourceName": "User", "identifier": "j.doe@example.com" }
}
```

### `identity.password.policy_violation`
- **HTTP:** 422 · **Trigger:** a supplied password fails the policy, e.g. below minimum length
  (`PasswordPolicyViolationException`). · **Extra:** none.

```json
{
  "status": 422,
  "detail": "Password must be at least 8 characters long",
  "correlationId": "018f...",
  "errorCode": "identity.password.policy_violation"
}
```

### `identity.password.invalid_current`
- **HTTP:** 422 · **Trigger:** the current password supplied to `POST /api/auth/password` does not match
  (`InvalidCurrentPasswordException`). · **Extra:** none.

```json
{
  "status": 422,
  "detail": "Current password is incorrect",
  "correlationId": "018f...",
  "errorCode": "identity.password.invalid_current"
}
```

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

### `agreement.pdf.rendering_failed`
- **HTTP:** 500 · **Trigger:** an `IOException` while rendering the agreement PDF
  (`POST /api/agreements/preview`, or the FR-05 signing flow) via
  `AgreementPdfRenderingException`. Not expected in normal operation. · **Extra:** none.

```json
{
  "status": 500,
  "detail": "Failed to render the agreement PDF document",
  "correlationId": "018f...",
  "errorCode": "agreement.pdf.rendering_failed"
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
  (`RentalSigningVersionMismatchException`, rental module API). · **Extra:** `params` =
  `{rentalId, expectedVersion, actualVersion}` in both cases.

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
  (`RentalNotAwaitingSignatureException`, rental module API). · **Extra:** `params` = `{rentalId, currentStatus}`.

```json
{
  "status": 409,
  "detail": "Rental 42 is not awaiting signature. Current status: DRAFT",
  "correlationId": "018f...",
  "errorCode": "agreement.signing.rental_not_awaiting_signature",
  "params": { "rentalId": 42, "currentStatus": "DRAFT" }
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

---

## Field-level validation codes (`errors[].code`)

Inside the `errors[]` array each entry's `code` is **not** from `ErrorCodes`; it is derived automatically from the
failing constraint annotation as `validation.<snake_case>` (e.g. `@Size` → `validation.size`), and `params` carries the
annotation's attributes. The fallback when no constraint code resolves is `shared.request.validation_failed`
(constant `VALIDATION_ERROR`). See [.claude/rules/error-responses.md](../.claude/rules/error-responses.md).

## Module-specific codes

Module exceptions (rental status, tariff, equipment status transition, duplicate phone, etc.) expose their own
`getErrorCode()` values surfaced through the module-scoped advices (`*RestControllerAdvice`). These codes are not held
in `ErrorCodes`; document each alongside its exception when you add or change it, and mirror the UI reference key in
`messages.properties` / `messages_ru.properties`.
