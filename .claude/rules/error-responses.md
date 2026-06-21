---
paths:
  - "service/src/main/java/**/validation/**"
  - "service/src/main/java/**/web/advice/**"
  - "service/src/main/java/**/web/error/**"
  - "service/src/main/java/**/exception/**"
---

# API Error Responses — hard constraints

All error handlers return an RFC 9457 `ProblemDetail` carrying two mandatory custom properties — `correlationId` and
`errorCode` (see `ProblemDetailField`). Field-level validation adds an `errors[]` array; structured context goes under
`params`. Two things must stay in sync: the **validation mapping** and the **error-code catalogue**.

## Validation error mapping

Validation errors are mapped centrally by `shared/web/advice/BaseValidationErrorMapper`; each becomes a
`ValidationError` record `{field, code, params}`. The mapper unwraps the source `jakarta.validation.ConstraintViolation`
from every Spring error object, so all Jakarta constraints are covered across the three entry points: `@RequestBody
@Valid` (`MethodArgumentNotValidException`), `@RequestParam`/`@PathVariable` (`HandlerMethodValidationException`), and
method-level `@Validated` (`ConstraintViolationException`).

When adding a **custom** constraint (`@Constraint` annotation + `ConstraintValidator`):

- **Do NOT add a handler or edit the mapper/advice.** Standard Jakarta constraints flow through unchanged; an
  `@ExceptionHandler` for a single constraint is a smell.
- **`code` is auto-derived from the annotation's simple name** → `validation.<snake_case>` (e.g. `@Size` →
  `validation.size`). It is a public, i18n-facing contract — name the annotation deliberately; renaming changes the API.
- **`params` are the annotation's declared attributes** minus `message`/`groups`/`payload` (e.g. `@Size(min=2, max=5)` →
  `{min: 2, max: 5}`). Declare meaningful attributes; never put secrets there. To hide an attribute, add it to
  `INTERNAL_ATTRIBUTES` in `BaseValidationErrorMapper`.
- **Runtime context does not surface** — values added via `HibernateConstraintValidatorContext.addMessageParameter(...)`
  are not in `params`; only static annotation attributes are.
- **For `@RequestParam`/`@PathVariable`**, include `PARAMETER` in `@Target` (method validation applies automatically).
- **Field semantics:** field-level → `field` = property/param name; class-level (whole-DTO) → global error, `field = null`.
- Cover it with a WebMvc negative test (`@ApiTest`) asserting `errors[*].code` and `errors[*].params` — see
  `CoreExceptionHandlerAdviceTest` and [unit-tests.md](unit-tests.md).

## Error-code catalogue

Every API error code is catalogued in [docs/error-codes.md](../../docs/error-codes.md) (description, HTTP status,
trigger, payload fields, example JSON) — the public contract the frontend branches on. Keep it in sync:

- **Every `errorCode` must be documented** there; update it in the **same change** that adds, renames, or changes a code
  or the `params` it carries. A code change without a catalogue update is incomplete.
- **Prefer a constant in `ErrorCodes`** over a string literal in a handler/exception; new literals are a smell.
- **Codes are stable, machine-facing identifiers** — do not repurpose an existing code's meaning; add a new one.
  Renaming a code is a breaking API change. Mirror the UI key in `messages.properties` / `messages_ru.properties`.

Depth: [docs/error-codes.md](../../docs/error-codes.md) (the catalogue); `spring-boot-best-practices` skill.
