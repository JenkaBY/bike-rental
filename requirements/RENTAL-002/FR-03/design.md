# System Design: RENTAL-002/FR-03 — `EquipmentAvailabilityService` (Internal Rental Service)

## 1. Architectural Overview

This story introduces a thin application service inside the rental module that encapsulates the
single concern of "given a set of equipment IDs, which are currently unavailable?" It acts as an
anti-corruption layer between use cases and the repository, preventing repository calls from leaking
directly into multiple callers and ensuring the early-exit optimisation (empty set short-circuit) is
applied in one place.

The service is strictly internal to the rental module. It is not registered in any public Facade and
must not be placed in the module's public package, so Spring Modulith prevents other modules from
injecting it directly.

---

## 2. Impacted Components

* **`EquipmentAvailabilityService` (new — `rental/application/service/` or `rental/domain/service/`):**
  New service class (or interface + implementation pair) with a single public method:
  ```
  Set<Long> getUnavailableIds(Set<Long> equipmentIds)
  ```
  Delegates to `RentalEquipmentRepository.findOccupiedEquipmentIds(equipmentIds)`.
  Short-circuits and returns `{}` when `equipmentIds` is empty without calling the repository.

* **`RentalEquipmentRepository` (domain port — dependency):**
  Consumed by `EquipmentAvailabilityService`; no changes to this component in this story (added in
  FR-02).

---

## 3. Abstract Data Schema Changes

None. This story adds only an in-process service layer; no schema or entity changes.

---

## 4. Component Contracts & Payloads

* **Interaction: `GetAvailableForRentEquipmentsService` → `EquipmentAvailabilityService`**
    * **Protocol:** In-process Spring bean injection (rental module internal)
    * **Payload:**

      Input:
      ```
      equipmentIds: Set<Long>    // candidate equipment IDs to check
      ```

      Output:
      ```
      Set<Long>                  // IDs that are currently ACTIVE or ASSIGNED; never null
      ```

* **Interaction: `BaseRequestedEquipmentValidator` → `EquipmentAvailabilityService`**
    * **Protocol:** In-process Spring bean injection (rental module internal)
    * **Payload:** Same as above; validator passes extracted equipment IDs from `EquipmentInfo` list.

* **Module boundary contract:**
  `EquipmentAvailabilityService` must NOT appear in the equipment module's public API package or any
  shared module. Spring Modulith will reject any cross-module injection attempt at context startup.

---

## 5. Updated Interaction Sequence

**Delegation path (non-empty input):**

1. Caller (e.g., `GetAvailableForRentEquipmentsService`) calls
   `EquipmentAvailabilityService.getUnavailableIds({1L, 2L, 3L})`.
2. `EquipmentAvailabilityService` verifies input is non-empty.
3. Delegates to `RentalEquipmentRepository.findOccupiedEquipmentIds({1L, 2L, 3L})`.
4. Repository returns `{1L, 3L}`.
5. `EquipmentAvailabilityService` returns `{1L, 3L}` to the caller.

**Short-circuit path (empty input):**

1. Caller passes `{}`.
2. `EquipmentAvailabilityService` detects empty set.
3. Returns `{}` immediately; repository is NOT called.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — internal service, no external surface.
* **Scale & Performance:** No overhead beyond the repository call. The short-circuit for empty input
  eliminates unnecessary DB round-trips when the upstream equipment query returns no results (the
  FR-05 early-exit path).
* **Spring Modulith compliance:** Service class is placed in a non-public package within the rental
  module. Module boundary verification at startup will confirm no external module references it.
