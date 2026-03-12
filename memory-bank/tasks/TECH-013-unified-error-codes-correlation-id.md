# [TECH-013] - Unified Error Codes, CorrelationId Filter & i18n-Ready Problem Details

**Status:** Completed  
**Added:** 2026-03-11  
**Updated:** 2026-03-11  
**Priority:** Medium  
**Module:** shared, all modules  
**Type:** Technical Improvement  
**Dependencies:** None

## Original Request

Unification of Problem Details to pass error code for all exceptions thrown by the application.
Add `OncePerRequestFilter` to set `CorrelationId`.
The aim is to allow i18n to show errors on UI.
This task also requires updating all ControllerAdvice classes and designing a pattern for error code naming.

Additionally, validation error responses must include a structured `errors` field:

```json
{
  // existing ProblemDetail fields...
  "errors": [
    {
      "field": "fieldName",
      "code": "error.code",
      "message": "error message 1",
      "params: { "min": 1, "max": 100 } // optional, for message formatting
    },
    {
      "field": "fieldName2",
      "code": "error.code",
      "message": "error message 2",
      "params: { "value": "abc" } // optional, for message formatting
    },
    //...
  ]
}
```

Response shape of business exceptions that requires additional parameters (e.g. `InvalidStatusTransitionException`)
looks like:

```json
{
  // existing ProblemDetail fields...
  "detail": "Business validation failed",
  "errorCode": "equipment.status.invalid_transition",
  "params: { "fromStatus": "AVAIALABLE", "toStatus": "RENTED" }
}
```

**Confirmed design decision:** Replace `errorId` with `correlationId` — `correlationId` comes from
`X-Correlation-ID` request header (or generated as UUID if absent), stored in MDC, returned in
`X-Correlation-ID` response header, and included in `ProblemDetail` as `correlationId` property.

## Thought Process

### Problem

Currently the API has four structural issues that block i18n on the UI:

1. **No `errorCode` in responses** — `ProblemDetail` contains only a human-readable `detail` string in
   English. The UI cannot perform i18n lookups without a machine-readable code.
2. **No `correlationId` propagation** — `errorId` is a UUID generated per exception-handler call with
   `UuidGenerator.generate()`. It is not shared with the client before the error happens, and there is no
   per-request tracing ID the UI can attach to outgoing requests.
3. **Inconsistent `ProblemDetail` shape** — 5 ControllerAdvice classes each generate `errorId`
   independently with duplicated boilerplate.
4. **No structured field errors** — Validation errors are collapsed into a single joined `detail` string.
   The UI cannot display per-field error messages without parsing text.

### Solution

**1. CorrelationId Filter** — `OncePerRequestFilter` reads `X-Correlation-ID` from the request header
(or generates a UUID if absent), puts it into MDC under key `correlationId`, writes it back as a response
header, and cleans up MDC in `finally`. All ControllerAdvice handlers stop calling `UUID.randomUUID()`
and instead call `MDC.get("correlationId")`.

**2. Error codes on exceptions** — Each `BikeRentalException` subclass defines
`public static final String ERROR_CODE` and passes it through the constructor chain to a new `errorCode`
field on `BikeRentalException`. For Spring framework exceptions, codes are assigned directly inside each
handler in `CoreExceptionHandlerAdvice`. All handlers add `errorCode` as a `ProblemDetail` property.

**3. Structured `errors` array** — Validation handlers (`MethodArgumentNotValidException`,
`HandlerMethodValidationException`, `ConstraintViolationException`) set an additional `errors` property
on `ProblemDetail` as `List<Map<String, String>>`. Each map entry has the field name as key and the
validation message as value. The `detail` field continues to carry the joined string for backward
compatibility. Full response shape:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Fields ['name','email'] are invalid",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "errorCode": "shared.request.validation_failed",
  "instance": "/api/customers",
  "errors": [
    {
      "field": "fieldName",
      "code": "error.code",
      "message": "error message",
      "params: { "min": 1, "max": 100 } // optional, for message formatting
    },
    //...
  ],
}
```

**4. i18n contract** — The UI uses `errorCode` to look up translated messages client-side. Backend
`messages.properties` and `messages_ru.properties` get one key per code (format `error.{errorCode}`) as
a documentation reference for UI developers.

### Error Code Naming Pattern

Format: `{module}.{entity}.{error_type}` — all lowercase, dot-separated, underscores within a segment.

**Domain exceptions:**

| Exception Class                        | Error Code                            |
|----------------------------------------|---------------------------------------|
| `ResourceNotFoundException`            | `shared.resource.not_found`           |
| `ReferenceNotFoundException`           | `shared.reference.not_found`          |
| `ResourceConflictException`            | `shared.resource.conflict`            |
| `EquipmentNotAvailableException`       | `shared.equipment.not_available`      |
| `InvalidRentalStatusException`         | `rental.status.invalid`               |
| `InvalidRentalUpdateException`         | `rental.update.invalid`               |
| `PrepaymentRequiredException`          | `rental.prepayment.required`          |
| `InsufficientPrepaymentException`      | `rental.prepayment.insufficient`      |
| `RentalNotReadyForActivationException` | `rental.activation.not_ready`         |
| `InvalidStatusTransitionException`     | `equipment.status.invalid_transition` |
| `DuplicateUidException`                | `equipment.uid.duplicate`             |
| `DuplicateSerialNumberException`       | `equipment.serial.duplicate`          |
| `DuplicatePhoneException`              | `customer.phone.duplicate`            |
| `SuitableTariffNotFoundException`      | `tariff.suitable.not_found`           |

**Spring framework exceptions (set in `CoreExceptionHandlerAdvice`):**

| Exception                                 | Error Code                                |
|-------------------------------------------|-------------------------------------------|
| `MethodArgumentNotValidException`         | `shared.request.validation_failed`        |
| `MethodArgumentTypeMismatchException`     | `shared.request.type_mismatch`            |
| `MissingServletRequestParameterException` | `shared.request.param_missing`            |
| `HandlerMethodValidationException`        | `shared.request.validation_failed`        |
| `ConstraintViolationException`            | `shared.request.constraint_violation`     |
| `HttpMessageNotReadableException`         | `shared.request.not_readable`             |
| `InvalidApiVersionException`              | `shared.api.version_invalid`              |
| `MissingApiVersionException`              | `shared.api.version_missing`              |
| `HttpRequestMethodNotSupportedException`  | `shared.request.method_not_allowed`       |
| `HttpMediaTypeNotSupportedException`      | `shared.request.media_type_not_supported` |
| `NoResourceFoundException`                | `shared.resource.not_found`               |
| `Exception` (fallback)                    | `shared.server.internal_error`            |

### Architecture Decisions

**errorCode placement:** Each `BikeRentalException` subclass defines `public static final String ERROR_CODE`
and passes it through the constructor chain. `ResourceConflictException` gets a `protected` errorCode-threading
constructor so subclasses (`DuplicatePhoneException`, `DuplicateUidException`, `DuplicateSerialNumberException`)
can override the code. Spring framework exceptions have codes assigned directly in handler methods.

**correlationId in MDC:** `ControllerAdvice` handlers call `MDC.get("correlationId")` — safe because
`CorrelationIdFilter` always sets it before the request reaches the controller. If null (e.g. unit tests
without the filter), handlers fall back to `UUID.randomUUID().toString()`.

**errors array implementation:** `body.setProperty("errors", List.of(Map.of("field", "msg"), ...))` using
`List<Map<String, String>>`. Applied only to the three validation exception handlers.

**SuitableTariffNotFoundException duplication:** Handled in both `TariffRestControllerAdvice` and
`RentalRestControllerAdvice`. Both use the same code `tariff.suitable.not_found`. No consolidation at this stage.

## Implementation Plan

- [ ] 1.1 Add `errorCode` field to `BikeRentalException` — new protected constructor
  `BikeRentalException(String message, String errorCode)`, keep all existing constructors with
  `errorCode = null` default, add `getErrorCode()` getter
- [ ] 1.2 Add errorCode-threading constructor to `ResourceConflictException` —
  `protected ResourceConflictException(String resourceName, String identifier, String errorCode)`;
  existing public constructor calls `this(resourceName, identifier, "shared.resource.conflict")`
- [ ] 1.3 Update `ResourceNotFoundException` — `ERROR_CODE = "shared.resource.not_found"`, pass via constructor
- [ ] 1.4 Update `ReferenceNotFoundException` — `ERROR_CODE = "shared.reference.not_found"`, pass via constructor
- [ ] 1.5 Update `EquipmentNotAvailableException` — `ERROR_CODE = "shared.equipment.not_available"`, pass via
  constructor
- [ ] 1.6 Update `SuitableTariffNotFoundException` — `ERROR_CODE = "tariff.suitable.not_found"`, pass via both
  constructors
- [ ] 1.7 Update `DuplicatePhoneException` — `ERROR_CODE = "customer.phone.duplicate"`, call
  `super(resourceName, identifier, ERROR_CODE)`
- [ ] 1.8 Update `DuplicateUidException` — `ERROR_CODE = "equipment.uid.duplicate"`, call `super` with code
- [ ] 1.9 Update `DuplicateSerialNumberException` — `ERROR_CODE = "equipment.serial.duplicate"`, call `super` with code
- [ ] 1.10 Update `InvalidStatusTransitionException` — `ERROR_CODE = "equipment.status.invalid_transition"`, pass via
  constructor
- [ ] 1.11 Update `InvalidRentalStatusException` — `ERROR_CODE = "rental.status.invalid"`, pass via constructor
- [ ] 1.12 Update `InvalidRentalUpdateException` — `ERROR_CODE = "rental.update.invalid"`, pass via constructor
- [ ] 1.13 Update `PrepaymentRequiredException` — `ERROR_CODE = "rental.prepayment.required"`, pass via constructor
- [ ] 1.14 Update `InsufficientPrepaymentException` — `ERROR_CODE = "rental.prepayment.insufficient"`, pass via
  constructor
- [ ] 1.15 Update `RentalNotReadyForActivationException` — `ERROR_CODE = "rental.activation.not_ready"`, pass via
  constructor
- [ ] 2.1 Create `CorrelationIdFilter extends OncePerRequestFilter` in `shared/web/filter/` — reads
  `X-Correlation-ID` header (or generates UUID), `MDC.put("correlationId", value)`,
  `response.setHeader("X-Correlation-ID", value)`, `MDC.remove` in finally block
- [ ] 2.2 Register `CorrelationIdFilter` as `@Component` (auto-registered by Spring Boot filter chain)
- [ ] 3.1 Update `CoreExceptionHandlerAdvice` — replace `UUID.randomUUID()` with `resolveCorrelationId()`
  helper (MDC.get + UUID fallback), rename all `errorId` log/property keys to `correlationId`, add
  `errorCode` property for all 12 handlers; add structured `errors` list property for
  `MethodArgumentNotValidException`, `HandlerMethodValidationException`, `ConstraintViolationException`
- [ ] 3.2 Update `RentalRestControllerAdvice` — correlationId from MDC, `errorCode` from `ex.getErrorCode()`
- [ ] 3.3 Update `EquipmentRestControllerAdvice` — same
- [ ] 3.4 Update `CustomerRestControllerAdvice` — same
- [ ] 3.5 Update `TariffRestControllerAdvice` — same
- [ ] 4.1 Write unit test `CorrelationIdFilterTest` — (a) header present: filter reuses value; (b) header
  absent: filter generates UUID; (c) MDC cleared after request; (d) `X-Correlation-ID` set on response
- [ ] 4.2 Write WebMvc tests — assert `errorCode` in response body, assert `correlationId` replaces `errorId`,
  assert `X-Correlation-ID` response header, assert `errors` array structure for validation errors
- [ ] 4.3 Add `error.{errorCode}` message keys to `messages.properties` (EN) and `messages_ru.properties`
  (RU) as reference for UI developers

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID   | Description                                                                | Status   | Updated    | Notes                          |
|------|----------------------------------------------------------------------------|----------|------------|--------------------------------|
| 1.1  | Add `errorCode` field + getter to `BikeRentalException`                    | Complete | 2026-03-11 | Keep all existing constructors |
| 1.2  | Threading constructor on `ResourceConflictException`                       | Complete | 2026-03-11 | For subclass override          |
| 1.3  | `ResourceNotFoundException` — `shared.resource.not_found`                  | Complete | 2026-03-11 |                                |
| 1.4  | `ReferenceNotFoundException` — `shared.reference.not_found`                | Complete | 2026-03-11 |                                |
| 1.5  | `EquipmentNotAvailableException` — `shared.equipment.not_available`        | Complete | 2026-03-11 |                                |
| 1.6  | `SuitableTariffNotFoundException` — `tariff.suitable.not_found`            | Complete | 2026-03-11 | Two constructors               |
| 1.7  | `DuplicatePhoneException` — `customer.phone.duplicate`                     | Complete | 2026-03-11 |                                |
| 1.8  | `DuplicateUidException` — `equipment.uid.duplicate`                        | Complete | 2026-03-11 |                                |
| 1.9  | `DuplicateSerialNumberException` — `equipment.serial.duplicate`            | Complete | 2026-03-11 |                                |
| 1.10 | `InvalidStatusTransitionException` — `equipment.status.invalid_transition` | Complete | 2026-03-11 |                                |
| 1.11 | `InvalidRentalStatusException` — `rental.status.invalid`                   | Complete | 2026-03-11 |                                |
| 1.12 | `InvalidRentalUpdateException` — `rental.update.invalid`                   | Complete | 2026-03-11 |                                |
| 1.13 | `PrepaymentRequiredException` — `rental.prepayment.required`               | Complete | 2026-03-11 |                                |
| 1.14 | `InsufficientPrepaymentException` — `rental.prepayment.insufficient`       | Complete | 2026-03-11 |                                |
| 1.15 | `RentalNotReadyForActivationException` — `rental.activation.not_ready`     | Complete | 2026-03-11 |                                |
| 2.1  | Create `CorrelationIdFilter` in `shared/web/filter/`                       | Complete | 2026-03-11 | OncePerRequestFilter           |
| 2.2  | Register `CorrelationIdFilter` as `@Component`                             | Complete | 2026-03-11 |                                |
| 3.1  | Update `CoreExceptionHandlerAdvice`                                        | Complete | 2026-03-11 | 12 handlers + errors on 3      |
| 3.2  | Update `RentalRestControllerAdvice`                                        | Complete | 2026-03-11 | 6 handlers                     |
| 3.3  | Update `EquipmentRestControllerAdvice`                                     | Complete | 2026-03-11 | 1 handler                      |
| 3.4  | Update `CustomerRestControllerAdvice`                                      | Complete | 2026-03-11 | 1 handler                      |
| 3.5  | Update `TariffRestControllerAdvice`                                        | Complete | 2026-03-11 | 1 handler                      |
| 4.1  | Unit test `CorrelationIdFilterTest`                                        | Complete | 2026-03-11 | 4 scenarios, all pass          |
| 4.2  | WebMvc tests for errorCode, correlationId, errors array                    | Complete | 2026-03-11 | 7 scenarios, all pass          |
| 4.3  | Add `error.{errorCode}` keys to messages.properties files                  | Complete | 2026-03-11 | EN + RU, 24 keys each          |

## Progress Log

### 2026-03-11

- Task created
- Defined error code naming pattern: `{module}.{entity}.{error_type}`, all lowercase, dot-separated
- Confirmed design decision: replace `errorId` with `correlationId` (Option A) — single ID per request
  from `X-Correlation-ID` header or generated UUID, stored in MDC, returned in response header and `ProblemDetail`
- Added requirement: validation error responses must include structured `errors` array in `ProblemDetail`
  for per-field display on UI — applies to `MethodArgumentNotValidException`,
  `HandlerMethodValidationException`, `ConstraintViolationException`; format `List<Map<String, String>>`
  with field name as key and message as value; `detail` string preserved for backward compatibility
- Mapped all 14 domain exceptions to error codes
- Mapped all 12 Spring framework exception handlers to error codes
- Architecture decision: `errorCode` field on `BikeRentalException` via constructor chain; threading
  constructor on `ResourceConflictException` allows subclasses to override the code
- 25 subtasks defined across 4 phases: exception layer (1.1–1.15), filter (2.1–2.2),
  ControllerAdvice updates (3.1–3.5), tests + messages (4.1–4.3)
- Implementation started and completed in the same session
- All 25 subtasks completed:
    - `BikeRentalException` extended with `errorCode` field and `protected (String, String)` constructor
    - `ResourceConflictException` gets threading constructor for subclass code override
    - All 14 domain exception classes updated with `ERROR_CODE` constant
    - `CorrelationIdFilter` created in `shared/web/filter/` as `@Component`
    - All 5 ControllerAdvice classes updated: `correlationId` from MDC, `errorCode` from exception, `errors` array on 3
      validation handlers
    - `CorrelationIdFilterTest` — 4 unit tests: header reuse, UUID generation, uniqueness, MDC cleanup
    - `CoreExceptionHandlerAdviceTest` — 7 WebMvc tests: correlationId propagation, errors array, error codes
    - `messages.properties` (EN) and `messages_ru.properties` (RU) — 24 `error.{code}` keys each

### 2026-03-11 (follow-up)

- Corrected `errors` array format in all three validation handlers — now uses `{ "field", "code" }` objects
- Code pattern updated to match the global `errorCode` convention (`{module}.{entity}.{error_type}`):
  - Constraint annotation simple name (e.g. `NotBlank`) is converted via `toValidationCode()` helper:
    CamelCase → snake_case with `([a-z])([A-Z])` regex → lowercase → prefixed with `validation.`
  - Examples: `NotBlank` → `validation.not_blank`, `NotNull` → `validation.not_null`,
    `PositiveOrZero` → `validation.positive_or_zero`
- `MethodArgumentNotValidException`: last element of `FieldError.getCodes()` passed through `toValidationCode()`
- `HandlerMethodValidationException`: last element of `MessageSourceResolvable.getCodes()` passed through
  `toValidationCode()`
- `ConstraintViolationException`: annotation simple name from `getConstraintDescriptor()` passed through
  `toValidationCode()`
- `toValidationCode(String)` private static helper added to `CoreExceptionHandlerAdvice`
- `CoreExceptionHandlerAdviceTest`: `whenBodyHasInvalidField_thenErrorsArrayIsPresent` updated to assert
  `$.errors[0].code = "validation.not_blank"`
- All 7 WebMvc tests pass

### 2026-03-11 (follow-up 2)

- Added `error.validation.*` keys to `messages.properties` (EN) and `messages_ru.properties` (RU)
  for all 12 constraint annotations actually used in the codebase:
  `not_blank`, `not_null`, `not_empty`, `size`, `min`, `positive`, `decimal_min`,
  `digits`, `email`, `past`, `pattern`, `assert_true`
- Key format matches existing convention: `error.{errorCode}` → `error.validation.not_blank` etc.
- Discovered by scanning all `@*` annotations in `service/src/main/**/*.java`

