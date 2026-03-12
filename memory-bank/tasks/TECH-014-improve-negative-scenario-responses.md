---
title: TECH-014 - Improve negative scenario responses and tests
status: Pending
added: 2026-03-12
---

# TECH-014 - Improve negative scenario responses and tests

**Status:** Pending  
**Added:** 2026-03-12  
**Updated:** 2026-03-12

## Original Request

Improve API responses for negative scenarios. Currently negative responses don't consistently output validation
messages, missing parameters are not reported properly, and WebMvc tests don't reflect actual negative responses. Update
the global error handling and add/update WebMvc and component tests to assert the standardized payload.

## Goal

Standardize and extend the global error response format so that all client-visible negative responses are predictable,
machine-parseable and testable. Improve WebMvc and component tests to assert the new structure for validation failures,
missing parameters, type mismatches, auth failures, not-found and internal errors.

## Acceptance criteria

1. All API error responses follow the standard shape with fields: `timestamp`, `status`, `error`, `errorCode`,
   `message`, `path`.
2. Validation failures (request body/format) return HTTP 400 and include `validationErrors` array. Each item contains
   `field`, `rejectedValue`, `message`.
3. Missing required parameters return HTTP 400 and include `missingParameters` array with `name` and optional
   `expectedType`.
4. Method argument type mismatches return HTTP 400 with parameter information.
5. Authorization/Authentication failures return 401/403 with consistent `errorCode` and `message`.
6. Server/internal errors return HTTP 500 with an application-level `errorCode` (e.g. `INTERNAL_ERROR`) and a safe
   generic `message`.
7. WebMvc tests assert HTTP status and JSON structure (presence and types of fields). Component tests verify end-to-end
   behavior for representative endpoints.
8. OpenAPI docs reference the global error schema.

## Implementation plan (ordered)

Estimated effort: 24–30h (single developer familiar with the codebase).

-
    1) Spec & task file (this file) — document schema and acceptance criteria. (2h)
-
    2) Create error DTOs and error code constants — `ErrorResponse`, `ValidationError`, `MissingParameter`,
       `ErrorCode`. (3h)
-
    3) Implement/extend `@ControllerAdvice` (e.g. `GlobalExceptionHandler`) to map common Spring exceptions and domain
       exceptions to `ErrorResponse`. Handle:

    - `MethodArgumentNotValidException`, `BindException` → `validationErrors`
    - `MissingServletRequestParameterException` → `missingParameters`
    - `MethodArgumentTypeMismatchException` → parameter type info
    - JSON parse/unreadable message → malformed JSON handling
    - Authentication/Authorization exceptions → 401/403
    - Domain / business exceptions → mapped status and `errorCode`
    - Fallback `Exception` → 500 `INTERNAL_ERROR` (log server-side only)
      (4–6h)
-
    4) Ensure controllers use `@Valid`, proper `@RequestBody`/`@RequestParam(required=true)`. Fix any controllers
       swallowing validation. (1–2h)
-
    5) Update/centralize messages for validation where appropriate (i18n integration if needed). (1–2h)
-
    6) Add/Update WebMvc tests to assert negative scenarios and JSON response shape. Replace brittle message text
       assertions with `errorCode` checks where possible. (6–8h)
-
    7) Add component tests to cover end-to-end negative responses for representative flows (component-test module). (
       3–4h)
-
    8) Update OpenAPI docs to reference new global error schema and examples. (1–2h)
-
    9) Draft release notes/migration doc and update `memory-bank/progress.md`. (1–2h)
-
    10) Run full test suite and fix regressions. Update memory-bank task progress. (2–4h)

## Files likely to change

- service/src/main/java/.../web/dto/ErrorResponse.java (new)
- service/src/main/java/.../web/dto/ValidationError.java (new)
- service/src/main/java/.../web/dto/MissingParameter.java (new)
- service/src/main/java/.../web/dto/ErrorCode.java (new or extend existing)
- service/src/main/java/.../advice/GlobalExceptionHandler.java (new or modify existing ControllerAdvice)
- service/src/main/java/.../controller/*Controller.java (ensure @Valid and annotations)
- service/src/test/java/.../web/GlobalExceptionHandlerTest.java (new)
- service/src/test/java/.../web/controller/*NegativeTest.java (update/create WebMvc tests)
- component-test/src/test/java/.../NegativeScenariosComponentTest.java (new/updated)
- docs/technical-details.md (update error schema)
- memory-bank/progress.md (update)
- memory-bank/tasks/_index.md (update status)

Note: Replace `...` with actual package path (e.g. `com.example.bikerent`) used across the project.

## Example expected error JSON (validation error)

{
"timestamp": "2026-03-12T14:23:45.123Z",
"status": 400,
"error": "Bad Request",
"errorCode": "VALIDATION_ERROR",
"message": "Request validation failed",
"path": "/api/v1/rentals",
"validationErrors": [
{ "field": "customer.phone", "rejectedValue": "abc123", "message": "must be a valid phone number" },
{ "field": "rental.startDate", "rejectedValue": null, "message": "must not be null" }
],
"missingParameters": [
{ "name": "tariffId", "expectedType": "UUID" }
]
}

Notes:

- `timestamp` ISO-8601 UTC
- `errorCode` is application-level stable code (prefer UPPER_SNAKE or dot-separated convention)
- `validationErrors` and `missingParameters` are present only when applicable

## WebMvc & component test cases to add/update

1. Validation failure (invalid DTO field) — assert 400, `errorCode == VALIDATION_ERROR`, `validationErrors` contains
   expected field entries.
2. Missing required query parameter — assert 400, `errorCode == MISSING_PARAMETER`, `missingParameters` contains
   parameter name and expectedType.
3. Path/param type mismatch — assert 400 and parameter info.
4. Malformed JSON body — assert 400 with safe message and `errorCode`.
5. Not-found domain exception — assert 404 and `errorCode == NOT_FOUND`.
6. Unauthorized/forbidden — assert 401/403 and `errorCode` mapping.
7. Internal server error fallback — assert 500 and `errorCode == INTERNAL_ERROR`.
8. Representative business rule failure (e.g., 409) — assert status and `errorCode`.

Testing notes:

- Prefer asserting `errorCode` and JSON path existence rather than exact `message` text to avoid brittle tests across
  i18n changes.
- Use AssertJ and MockMvc JSON path assertions.
- Add builders/fixtures for invalid payloads.

## Backwards compatibility and migration

- Keep `message` and `error` fields to avoid breaking naive older clients.
- `errorCode` is additive — document the codes in `docs/technical-details.md`.
- Publish migration notes and update OpenAPI spec.
- If external clients depend on legacy payload shapes, consider a compatibility mode behind a header (e.g.
  `Accept-Version`) — avoid unless necessary.

## Progress tracking (task file structure)

Use the memory-bank task pattern:

- Status: Pending / In Progress / Completed
- Added / Updated dates
- Thought Process: rationale for changes
- Implementation Plan: ordered subtasks
- Progress Tracking table (subtasks with status & updated dates)
- Progress Log: per-day entries describing changes, test runs, blockers

### Suggested subtask table

| ID | Description                           | Status  | Updated    | Notes             |
|----|---------------------------------------|---------|------------|-------------------|
| 1  | Spec & task file                      | Done    | 2026-03-12 | This file created |
| 2  | Create ErrorResponse DTOs & ErrorCode | Pending |            |                   |
| 3  | Implement GlobalExceptionHandler      | Pending |            |                   |
| 4  | Update controllers for @Valid         | Pending |            |                   |
| 5  | Add/Update WebMvc tests               | Pending |            |                   |
| 6  | Add component tests                   | Pending |            |                   |
| 7  | Update OpenAPI & docs                 | Pending |            |                   |
| 8  | Release notes & monitoring            | Pending |            |                   |

### Initial progress log

- 2026-03-12: Task created with full plan and acceptance criteria. Next: implement DTOs and global advice.

---

End of task specification.

