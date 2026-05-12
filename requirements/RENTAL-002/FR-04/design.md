# System Design: RENTAL-002/FR-04 — Update `BaseRequestedEquipmentValidator#validateAvailability`

## 1. Architectural Overview

This story replaces the stale `status_slug`-based availability check in
`BaseRequestedEquipmentValidator` with a live query against the rental module's own occupancy data.
It also hardens the error contract: when equipment is occupied, a structured domain exception carrying
the specific unavailable IDs is thrown, which the REST layer maps to `409 Conflict`.

The change affects three layers simultaneously: the validator (application), the exception model
(shared or rental), and the REST error handler (web). The `validateAvailability` method signature
is intentionally preserved so that all existing callers (`CreateRentalService`,
`UpdateRentalService`) require no changes.

---

## 2. Impacted Components

* **`BaseRequestedEquipmentValidator` (`rental/application/service/validator/`):**
  The `validateAvailability(List<EquipmentInfo>)` method body is rewritten to:
    1. Extract `Long` IDs from the input list.
    2. Call `EquipmentAvailabilityService.getUnavailableIds(Set<Long>)`.
    3. If the result is non-empty, throw `EquipmentOccupiedException` (see below).
       The old `EquipmentInfo.isAvailable()` / `status_slug` check is removed entirely.
       `EquipmentAvailabilityService` is injected as a constructor dependency.

* **`EquipmentOccupiedException` (new domain exception):**
  A new exception class extending `BikeRentalException` is introduced (preferably in
  `rental/domain/` or `shared/exception/` — rental-scoped is preferred to avoid polluting shared
  with rental-specific semantics).
    - `errorCode`: `EQUIPMENT_NOT_AVAILABLE` (new constant added to `ErrorCodes`)
    - `params`: `Set<Long> unavailableIds`

  The existing `EquipmentNotAvailableException` in `shared` carries an `EquipmentDetails` params
  type and is not suitable for carrying a `Set<Long>`. A new exception is created rather than
  repurposing the shared one.

* **`RentalRestControllerAdvice` or `CoreExceptionHandlerAdvice` (web error handler):**
  A handler method is added (or updated) for `EquipmentOccupiedException`:
    - HTTP status: `409 Conflict`
    - `ProblemDetail` extra properties:
        - `errorCode`: `"EQUIPMENT_NOT_AVAILABLE"`
        - `correlationId`: from `MDC.get("correlationId")`, fallback to generated UUID
        - `unavailableIds`: the `Set<Long>` from the exception params

* **`ErrorCodes` (shared constants):**
  New constant: `EQUIPMENT_NOT_AVAILABLE`.

---

## 3. Abstract Data Schema Changes

None. This story makes no schema changes.

---

## 4. Component Contracts & Payloads

* **Interaction: `CreateRentalService` / `UpdateRentalService` → `BaseRequestedEquipmentValidator`**
    * **Protocol:** In-process call (unchanged)
    * **Payload Changes:** None — method signature `validateAvailability(List<EquipmentInfo>)` unchanged.
      Callers are unaffected.

* **Interaction: `BaseRequestedEquipmentValidator` → `EquipmentAvailabilityService`**
    * **Protocol:** In-process Spring bean call (new dependency)
    * **Payload:**
      Input: `Set<Long>` extracted from the `EquipmentInfo` list (IDs)
      Output: `Set<Long>` of occupied IDs

* **Interaction: `RentalRestControllerAdvice` → HTTP client**
    * **Protocol:** HTTP `409 Conflict`
    * **Payload (new error response structure):**
      ```json
      {
        "status": 409,
        "title": "Conflict",
        "errorCode": "EQUIPMENT_NOT_AVAILABLE",
        "correlationId": "<uuid>",
        "unavailableIds": [11, 12]
      }
      ```

---

## 5. Updated Interaction Sequence

**Happy path — all equipment available:**

1. `CreateRentalService` calls `validator.validateAvailability(equipmentInfoList)`.
2. Validator extracts IDs: `{10L, 11L}`.
3. Validator calls `EquipmentAvailabilityService.getUnavailableIds({10L, 11L})`.
4. Service returns `{}` (none occupied).
5. Validator returns without throwing; rental creation proceeds.

**Unhappy path — some equipment occupied:**

1. `CreateRentalService` calls `validator.validateAvailability(equipmentInfoList)`.
2. Validator extracts IDs: `{10L, 11L, 12L}`.
3. Validator calls `EquipmentAvailabilityService.getUnavailableIds({10L, 11L, 12L})`.
4. Service returns `{11L, 12L}`.
5. Validator throws `EquipmentOccupiedException(unavailableIds={11L, 12L})`.
6. Exception propagates to `RentalCommandController`.
7. `RentalRestControllerAdvice` intercepts; builds `ProblemDetail` with `status=409`,
   `errorCode=EQUIPMENT_NOT_AVAILABLE`, `unavailableIds=[11, 12]`.
8. `409 Conflict` response is returned to the HTTP client.

**Concurrent double-booking bypass (DB-level catch):**

1. Two concurrent requests pass application-layer validation simultaneously (race condition).
2. Both attempt to insert ACTIVE/ASSIGNED rows for the same equipment.
3. The second insert violates `idx_rental_equipments_one_active` (FR-01).
4. PostgreSQL raises a unique constraint violation → `DataIntegrityViolationException`.
5. Global exception handler maps this to `409 Conflict` with `errorCode: EQUIPMENT_NOT_AVAILABLE`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Error responses never include stack traces; only structured `ProblemDetail`
  with `correlationId`, `errorCode`, and `unavailableIds`.
* **Scale & Performance:** One additional DB query per rental creation/update (the availability check).
  The partial index (FR-01) keeps this query fast. The query is bounded in scope to the IDs of the
  requested equipment, not the full `rental_equipments` table.
