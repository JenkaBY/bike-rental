# User Story: FR-04 — Update `BaseRequestedEquipmentValidator#validateAvailability`

## 1. Description

**As a** rental operations system,
**I want to** validate equipment availability using the rental module's own occupancy data during rental
creation and update,
**So that** double-booking is detected and rejected with a precise error listing the conflicting
equipment IDs, without relying on the stale `status_slug` field from the equipment module.

## 2. Context & Business Rules

* **Trigger:** FR-03 (`EquipmentAvailabilityService`) must be complete before this story is implemented.
* **Rules Enforced:**
    - `BaseRequestedEquipmentValidator#validateAvailability(List<EquipmentInfo> equipments)` is refactored
      in-place; the **method signature does not change**.
    - The body is updated to:
        1. Extract `Long` IDs from the `EquipmentInfo` list.
        2. Call `EquipmentAvailabilityService.getUnavailableIds(Set<Long>)`.
        3. If the returned set is non-empty, throw a domain exception carrying the set of unavailable IDs.
    - The old `EquipmentInfo.isAvailable()` / `status_slug` check is removed from this method entirely.
    - The domain exception thrown must:
        - Extend `BikeRentalException` (the shared base class).
        - Carry the set of unavailable `Long` equipment IDs as its `params` payload.
        - Use a well-known `errorCode` constant (add to `ErrorCodes`: `EQUIPMENT_NOT_AVAILABLE`).
        - Be handled by an existing or new `@RestControllerAdvice` with HTTP `409 Conflict`.
        - Include an `unavailableIds` field in the `ProblemDetail` extra properties.
    - If `EquipmentNotAvailableException` already exists in shared, assess whether its `params` type
      (`EquipmentDetails`) can be extended to carry `Set<Long>`. If not, a new exception class is
      preferable over forcing an unrelated type to carry rental-domain data.
    - `validateSize` method (checks for missing equipment IDs) is not changed by this story.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The availability check adds one DB query per rental creation/update. This is
  acceptable; the query benefits from the partial index (FR-01).
* **Security/Compliance:** Error responses must never expose internal stack traces; only structured
  `ProblemDetail` with `correlationId`, `errorCode`, and `unavailableIds`.
* **Usability/Other:** Existing WebMVC controller tests that relied on the equipment `status_slug` for
  the unavailability scenario must be updated to mock `EquipmentAvailabilityService` instead.

## 4. Acceptance Criteria (BDD)

**Scenario 1: All requested equipment is available**

* **Given** equipment IDs {10, 11} are requested for a rental
* **And** neither ID is ACTIVE or ASSIGNED in any rental
* **When** `validateAvailability` is called with the corresponding `EquipmentInfo` list
* **Then** no exception is thrown; validation passes silently

**Scenario 2: Some requested equipment is occupied**

* **Given** equipment IDs {10, 11, 12} are requested
* **And** ID 11 is ACTIVE in another rental; ID 12 is ASSIGNED in another rental
* **When** `validateAvailability` is called
* **Then** a domain exception is thrown carrying `unavailableIds = {11, 12}`

**Scenario 3: HTTP response for unavailability is 409 with structured body**

* **Given** a POST /api/rentals request includes equipment that is currently occupied
* **When** the request is processed
* **Then** the response status is `409 Conflict`
* **And** the response body is a `ProblemDetail` with:
    - `status: 409`
    - `errorCode: "EQUIPMENT_NOT_AVAILABLE"`
    - `correlationId: <non-null UUID>`
    - `unavailableIds: [<list of Long IDs>]`

**Scenario 4: Validator no longer reads statusSlug for availability**

* **Given** an equipment row has `status_slug = 'RESERVED'` (stale after listener disable)
* **And** that equipment has no ACTIVE or ASSIGNED row in `rental_equipments`
* **When** `validateAvailability` is called
* **Then** no exception is thrown (availability is determined by rental data only)

## 5. Out of Scope

* Changing the `validateSize` method.
* Changing the method signature of `validateAvailability`.
* Removing `EquipmentInfo.isAvailable()` — deferred.
* Handling DB-level unique constraint violations from FR-01 in this story — those are handled by the
  global exception handler separately.
