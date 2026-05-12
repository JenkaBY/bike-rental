# System Design: RENTAL-002/FR-02 — `RentalEquipmentRepository` Availability Query

## 1. Architectural Overview

This story adds a read-only query method to the rental module's repository layer that answers the
question: "which of these candidate equipment IDs are currently occupied by an active or assigned
rental?" It is the foundational data-access primitive on which `EquipmentAvailabilityService`
(FR-03) and `GetAvailableForRentEquipmentsService` (FR-05) are built.

The method is added to the existing `RentalRepository` domain port (or a new dedicated
`RentalEquipmentRepository` port if the existing one does not cover rental-equipment-level queries).
The implementation lives in the infrastructure persistence adapter and benefits directly from the
partial index created in FR-01.

---

## 2. Impacted Components

* **`RentalRepository` / `RentalEquipmentRepository` (domain port — `rental/domain/repository/`):**
  Gains one new method:
  ```
  Set<Long> findOccupiedEquipmentIds(Set<Long> candidateIds)
  ```
  If a dedicated `RentalEquipmentRepository` port does not yet exist, it is created to keep
  rental-equipment-level queries separate from rental-aggregate-level queries.

* **`RentalEquipmentJpaRepository` / persistence adapter (`rental/infrastructure/persistence/`):**
  Provides the implementation via a Spring Data JPA `@Query` using JPQL or native SQL:
  ```sql
  SELECT re.equipment_id
  FROM rental_equipments re
  WHERE re.equipment_id IN (:candidateIds)
    AND re.status IN ('ACTIVE', 'ASSIGNED')
  ```
  The adapter short-circuits and returns an empty set immediately when `candidateIds` is empty,
  without issuing a DB query.

---

## 3. Abstract Data Schema Changes

No schema changes. This story queries `rental_equipments.equipment_id` and `rental_equipments.status`
using the partial index added in FR-01.

---

## 4. Component Contracts & Payloads

* **Interaction: `EquipmentAvailabilityService` → `RentalEquipmentRepository`**
    * **Protocol:** In-process Spring Data JPA repository call
    * **Payload (new method):**

      Input:
      ```
      candidateIds: Set<Long>    // subset of equipment IDs to check; may be empty
      ```

      Output:
      ```
      Set<Long>                  // subset of candidateIds that are ACTIVE or ASSIGNED; never null
      ```

      Empty-input contract:
      ```
      candidateIds = {} → returns {} immediately, no DB round-trip
      ```

---

## 5. Updated Interaction Sequence

**Happy path — some candidates are occupied:**

1. Caller passes `{1L, 2L, 3L}` to `findOccupiedEquipmentIds`.
2. Adapter verifies `candidateIds` is non-empty.
3. Adapter executes query:
   `SELECT equipment_id FROM rental_equipments WHERE equipment_id IN (1,2,3) AND status IN ('ACTIVE','ASSIGNED')`
4. PostgreSQL uses the partial index `idx_rental_equipments_one_active` to resolve the query efficiently.
5. Result rows `{1L, 3L}` are returned as a `Set<Long>`.

**Short-circuit path — empty input:**

1. Caller passes `{}`.
2. Adapter detects empty set and returns `{}` immediately.
3. No DB query is issued.

**No-match path:**

1. Query executes; no rows match.
2. Empty `Set<Long>` is returned (not null, not an exception).

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — internal repository method; not reachable from any external API surface.
* **Scale & Performance:** The partial index (FR-01) ensures the query scans only in-flight rows.
  For a typical inventory of <1 000 active rentals, the query is expected to complete in single-digit
  milliseconds. The `IN (:candidateIds)` clause is bounded by the size of the candidate set passed
  from the Facade result list, which itself is bounded by the equipment catalogue size.
