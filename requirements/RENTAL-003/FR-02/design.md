# System Design: FR-02 — Lifecycle Endpoint & Request Validation

## 1. Architectural Overview

This story adds a new HTTP command endpoint `PATCH /api/rentals/{rentalId}/lifecycles` to the
existing `RentalCommandController` within the `rental` module. It introduces a thin web layer
artifact — a `RentalLifecycleRequest` DTO with a constrained `LifecycleStatus` enum — that acts
as the entry gate for all operator-initiated lifecycle transitions.

The controller contains no routing logic. It validates the incoming request and delegates to a
single orchestrator use case — `RentalLifecycleUseCase` — which inspects the requested status and
dispatches to the appropriate downstream use case (`ActivateRentalUseCase` or
`CancelRentalUseCase` from FR-03 and FR-04). This keeps the controller free of `switch`/`if`
branching and makes adding future lifecycle statuses a change confined to the orchestrator.

The response contract is identical to `GET /api/rentals/{id}` — the updated `Rental` domain
object is mapped to the existing `RentalResponse` DTO via the existing `RentalQueryMapper`.

---

## 2. Impacted Components

* **`RentalCommandController` (API — `rental/web/command/`):**
  Gains one new handler method mapped to `PATCH /api/rentals/{rentalId}/lifecycles`. Accepts a
  `@Valid @RequestBody RentalLifecycleRequest`, delegates unconditionally to
  `RentalLifecycleUseCase`, and returns `200 OK` with the updated `RentalResponse`. Contains no
  status-based branching.

* **`RentalLifecycleUseCase` (Application Use Case Interface — `rental/application/usecase/`):** *(new)*
  Defines a single method `execute(RentalLifecycleCommand command)` returning the updated `Rental`.
  `RentalLifecycleCommand` carries `rentalId` (Long) and `targetStatus` (`LifecycleStatus`).

* **`RentalLifecycleService` (Application Service — `rental/application/service/`):** *(new)*
  Implements `RentalLifecycleUseCase`. Contains the routing logic: inspects `command.targetStatus()`
  and delegates to `ActivateRentalUseCase` for `ACTIVE` or `CancelRentalUseCase` for `CANCELLED`.
  Returns the result of the delegated use case directly.

* **`RentalLifecycleRequest` (DTO — `rental/web/command/dto/`):** *(new)*
  A record or class with a single field `status` of type `LifecycleStatus` (see below). Annotated
  with `@NotNull` on the `status` field to produce a `CONSTRAINT_VIOLATION` error on null or
  missing values.

* **`LifecycleStatus` (Enum — `rental/web/command/dto/`):** *(new)*
  An enum with exactly two constants: `ACTIVE` and `CANCELLED`. Declared separately from
  `RentalStatus` so that the HTTP contract explicitly restricts which values are accepted at this
  endpoint. Bean Validation deserialization failure for any unrecognized string value produces a
  `400 Bad Request`.

* **`RentalRestControllerAdvice` (Error Handler — `rental/web/error/`):**
  No changes required. Existing handlers for `InvalidRentalStatusException`,
  `ResourceNotFoundException`, and `ConstraintViolationException` / `MethodArgumentNotValidException`
  cover all error paths introduced by this endpoint.

---

## 3. Abstract Data Schema Changes

None. This story adds no new persistence attributes or tables.

---

## 4. Component Contracts & Payloads

* **Interaction: HTTP Client → `RentalCommandController`**
    * **Protocol:** REST / HTTP PATCH
    * **Endpoint:** `PATCH /api/rentals/{rentalId}/lifecycles`
    * **Request Payload:**
      ```
      { "status": "ACTIVE" | "CANCELLED" }
      ```
      Any other value (including `"COMPLETED"`, `"DRAFT"`, `"DEBT"`, `null`, or absent field)
      results in `400 Bad Request` with body:
      ```
      {
        "errorCode": "CONSTRAINT_VIOLATION",
        "errors": [{ "field": "status", "message": "..." }]
      }
      ```
    * **Success Response:** `200 OK` — same `RentalResponse` shape returned by
      `GET /api/rentals/{id}`.
    * **Error Responses:**
        - `400` — validation failure (null, missing, or disallowed status value)
        - `404` — rental not found (from `ResourceNotFoundException`)
        - `409` / `422` — invalid state transition (from `InvalidRentalStatusException`)

* **Interaction: `RentalCommandController` → `RentalLifecycleUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** `RentalLifecycleCommand(rentalId, targetStatus)`. Returns updated `Rental`.

* **Interaction: `RentalLifecycleService` → `ActivateRentalUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** Called when `targetStatus == ACTIVE`; passes `rentalId`. Returns updated `Rental`.

* **Interaction: `RentalLifecycleService` → `CancelRentalUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** Called when `targetStatus == CANCELLED`; passes `rentalId`. Returns updated `Rental`.

---

## 5. Updated Interaction Sequence

### Happy path — ACTIVE

1. HTTP client sends `PATCH /api/rentals/42/lifecycles` with `{ "status": "ACTIVE" }`.
2. Spring MVC deserializes `"ACTIVE"` into `LifecycleStatus.ACTIVE`; `@Valid` passes.
3. `RentalCommandController` invokes
   `RentalLifecycleUseCase.execute(RentalLifecycleCommand{rentalId=42, targetStatus=ACTIVE})`.
4. `RentalLifecycleService` inspects `targetStatus == ACTIVE` → delegates to `ActivateRentalUseCase.execute(42L)`.
5. Use case performs business logic (FR-03) and returns the updated `Rental`.
6. Controller maps `Rental` → `RentalResponse` via `RentalQueryMapper`.
7. `200 OK` with `RentalResponse` body is returned to the client.

### Happy path — CANCELLED

1. HTTP client sends `PATCH /api/rentals/42/lifecycles` with `{ "status": "CANCELLED" }`.
2. Spring MVC deserializes `"CANCELLED"` into `LifecycleStatus.CANCELLED`; `@Valid` passes.
3. `RentalCommandController` invokes
   `RentalLifecycleUseCase.execute(RentalLifecycleCommand{rentalId=42, targetStatus=CANCELLED})`.
4. `RentalLifecycleService` inspects `targetStatus == CANCELLED` → delegates to `CancelRentalUseCase.execute(42L)`.
5. Use case performs business logic (FR-04) and returns the updated `Rental`.
6. Controller maps `Rental` → `RentalResponse`.
7. `200 OK` returned.

### Unhappy path — invalid status value

1. HTTP client sends `{ "status": "COMPLETED" }`.
2. Spring MVC fails to deserialize `"COMPLETED"` into `LifecycleStatus` (enum constant not found).
3. `MethodArgumentNotValidException` (or `HttpMessageNotReadableException`) is raised.
4. `RentalRestControllerAdvice` (or `CoreExceptionHandlerAdvice`) maps it to `400 Bad Request`
   with `errorCode: CONSTRAINT_VIOLATION` and an `errors` array.

### Unhappy path — rental not found

1. Deserialization succeeds; use case calls `RentalRepository.findById(rentalId)`.
2. No record found → `ResourceNotFoundException` thrown.
3. Mapped to `404 Not Found`.

### Unhappy path — invalid state transition

1. Deserialization succeeds; use case calls `RentalStatus.validateTransitionTo(target)`.
2. Transition disallowed → `InvalidRentalStatusException` thrown.
3. `RentalRestControllerAdvice` maps to the configured HTTP status.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No security filter chain is active; endpoint is open. No change.
* **Scale & Performance:** The endpoint adds one read + one write database operation (same as all
  other command endpoints). No caching or queuing required.
