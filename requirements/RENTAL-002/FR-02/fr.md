# User Story: FR-02 — Create `RentalEquipmentRepository` Availability Query

## 1. Description

**As a** developer in the rental module,
**I want to** query which equipment IDs from a candidate set are currently in an ACTIVE or ASSIGNED rental,
**So that** the rental module can determine operational availability without querying the equipment module.

## 2. Context & Business Rules

* **Trigger:** FR-01 (unique partial index) must be complete before this story is implemented, as the
  index makes the query efficient.
* **Rules Enforced:**
    - A new method is added to the existing `RentalEquipmentRepository` domain port (or a new dedicated
      port interface `RentalEquipmentRepository` is created if one does not already exist):
      ```
      Set<Long> findOccupiedEquipmentIds(Set<Long> candidateIds)
      ```
    - The method returns only IDs from `candidateIds` that currently have at least one row in
      `rental_equipments` with `status IN (ACTIVE, ASSIGNED)`.
    - IDs in `candidateIds` that have no ACTIVE/ASSIGNED row are NOT included in the result.
    - If `candidateIds` is empty, the method returns an empty set immediately (no DB query issued).
    - If no candidates are occupied, an empty set is returned (not null, not an exception).
    - The query is scoped to IDs within the provided `candidateIds` set so the result is always a subset
      of the input — callers may safely use it for set difference operations.
    - The implementation uses `RentalEquipmentStatus.ACTIVE` and `RentalEquipmentStatus.ASSIGNED` enum
      values (not raw strings) to ensure type safety.
    - The method is read-only; it has no side effects and requires no transaction write access.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The query must benefit from the partial index created in FR-01. The `IN (candidateIds)`
  clause paired with the partial index predicate allows efficient look-up even on large tables.
* **Security/Compliance:** N/A — internal repository method, not exposed via any API.
* **Usability/Other:** The implementation adapter (Spring Data JPA or JPQL query) lives in
  `rental/infrastructure/persistence/` and must not be referenced directly by any component outside
  the rental module.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Some candidates are occupied**

* **Given** equipment IDs {1, 2, 3} are provided as candidates
* **And** equipment 1 has an ACTIVE rental equipment row; equipment 3 has an ASSIGNED row; equipment 2
  has only RETURNED rows
* **When** `findOccupiedEquipmentIds(Set.of(1L, 2L, 3L))` is called
* **Then** the result is `Set.of(1L, 3L)`

**Scenario 2: No candidates are occupied**

* **Given** none of the provided candidate IDs have ACTIVE or ASSIGNED rows
* **When** `findOccupiedEquipmentIds(Set.of(10L, 11L))` is called
* **Then** the result is an empty set

**Scenario 3: All candidates are occupied**

* **Given** both IDs in the input set have ACTIVE rows
* **When** `findOccupiedEquipmentIds(Set.of(5L, 6L))` is called
* **Then** the result is `Set.of(5L, 6L)`

**Scenario 4: Empty input set returns empty result without a DB query**

* **Given** an empty set is passed
* **When** `findOccupiedEquipmentIds(Set.of())` is called
* **Then** an empty set is returned; no database round-trip is made

**Scenario 5: RETURNED rows do not count as occupied**

* **Given** equipment ID 7 has only RETURNED rows in `rental_equipments`
* **When** `findOccupiedEquipmentIds(Set.of(7L))` is called
* **Then** the result is an empty set

## 5. Out of Scope

* Querying by time range (date-based availability) — not needed at this stage.
* Returning the rental ID or rental details alongside the equipment ID — the method returns IDs only.
* Any public-facing API for this repository method.
