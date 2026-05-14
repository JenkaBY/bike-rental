# System Design: FR-02 — Validate Date Range Consistency

## 1. Architectural Overview

This story adds a cross-parameter guard to the Search Rentals flow: when both `from` and `to` are
supplied, the system asserts that `from` is not after `to` before any repository call is made. The
validation lives in `FindRentalsService` (application layer) and is surfaced to callers via a new
domain exception `InvalidDateRangeException` that is mapped to `HTTP 400` with error code
`CONSTRAINT_VIOLATION` by `RentalRestControllerAdvice`.

No new HTTP endpoints, persistence changes, or cross-module interactions are introduced. The error
response shape follows the existing `ProblemDetail` convention established by the module-scoped
`RentalRestControllerAdvice`.

---

## 2. Impacted Components

* **`FindRentalsService` (Application Service — `rental/application/service/`):**
  Before executing any filter logic or calling the repository, adds a guard:
  if both `query.from()` and `query.to()` are non-null and `from.isAfter(to)`, throws
  `InvalidDateRangeException`. When only one parameter is present the guard is skipped.

* **`InvalidDateRangeException` (Domain Exception — `rental/domain/exception/`):** *(new)*
  A new unchecked exception signalling that the supplied date range is logically invalid (`from`
  is after `to`). Carries a human-readable detail message that includes both boundary values.
  Consistent with other domain exceptions in this package (e.g., `InvalidRentalStatusException`),
  it exposes an `errorCode` property.

* **`RentalRestControllerAdvice` (Error Handler — `rental/web/error/`):**
  Gains a new `@ExceptionHandler(InvalidDateRangeException.class)` method. Returns `HTTP 400`
  (`BAD_REQUEST`) with a `ProblemDetail` body whose `errorCode` property is set to
  `ErrorCodes.CONSTRAINT_VIOLATION`. The `detail` field describes the invalid range.
  Follows the same `correlationId` / `errorCode` pattern as existing handlers in this class.

---

## 3. Abstract Data Schema Changes

None.

---

## 4. Component Contracts & Payloads

* **Interaction: HTTP Client → `RentalQueryController`**
    * **Protocol:** REST / HTTP GET
    * **Payload Changes:** No new parameters beyond those introduced in FR-01. The validation
      constraint is enforced on existing `from` / `to` parameters when both are present.
    * **Error Response (new):**
      ```
      HTTP 400 Bad Request
      {
        "status": 400,
        "detail": "Date range is invalid: 'from' (2026-02-20) must not be after 'to' (2026-02-15)",
        "correlationId": "<uuid>",
        "errorCode": "shared.request.constraint_violation"
      }
      ```

* **Interaction: `FindRentalsService` → `RentalRepository`**
    * **Protocol:** In-process method call
    * **Payload Changes:** The `findAll` call introduced in FR-01 is only reached when the date
      range guard passes. If the guard throws `InvalidDateRangeException`, execution stops and no
      repository call is made.

---

## 5. Updated Interaction Sequence

### Happy path — `from` equals `to` (single day, valid)

1. HTTP client sends `GET /api/rentals?from=2026-02-15&to=2026-02-15`.
2. `RentalQueryController` binds both dates and builds `FindRentalsQuery`.
3. `FindRentalsService` evaluates `from.isAfter(to)` → `false` (same day, guard passes).
4. Execution continues to the repository call as described in FR-01.
5. `HTTP 200` returned with matching rentals.

### Happy path — only `from` provided (guard not applicable)

1. HTTP client sends `GET /api/rentals?from=2026-02-20`.
2. `FindRentalsQuery` has `from=2026-02-20`, `to=null`.
3. `FindRentalsService` guard checks: `to` is null → guard skipped.
4. Execution continues; result filtered by `createdFrom` only.

### Unhappy path — `from` after `to`

1. HTTP client sends `GET /api/rentals?from=2026-02-20&to=2026-02-15`.
2. `RentalQueryController` binds `from=2026-02-20`, `to=2026-02-15` and builds `FindRentalsQuery`.
3. `FindRentalsService` evaluates `from.isAfter(to)` → `true`.
4. `InvalidDateRangeException` is thrown with both boundary values in the message.
5. `RentalRestControllerAdvice.handleInvalidDateRange` intercepts the exception.
6. Returns `HTTP 400` with `ProblemDetail` body, `errorCode = CONSTRAINT_VIOLATION`, and a
   descriptive `detail` string. No repository call is made.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No authentication surface change. The guard is a fast pre-condition check;
  it does not expose any data beyond the error message.
* **Scale & Performance:** The guard is a single in-memory `LocalDate.isAfter` comparison executed
  before any I/O. Cost is negligible.
