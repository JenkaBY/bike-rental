# System Design: RENTAL-002/FR-05 — `GetAvailableForRentEquipmentsUseCase`

## 1. Architectural Overview

This story introduces the core availability query use case in the rental module. It is the only
component in the system that combines physical condition data (from the equipment module via Facade)
with operational occupancy data (from the rental module's own repository) to answer the question:
"what equipment can a customer rent right now?"

The use case follows the two-phase filter pattern:

1. Ask the equipment module for physically fit candidates (condition = GOOD, optional text filter).
2. Ask the rental module's availability service which of those candidates are currently occupied.
3. Return the difference as a best-effort page.

The cross-module call is strictly one-way: rental → `EquipmentFacade`. The equipment module has no
knowledge of this use case.

---

## 2. Impacted Components

* **`GetAvailableForRentEquipmentsUseCase` (new interface — `rental/application/usecase/`):**
  ```
  Page<EquipmentInfo> getAvailableEquipments(EquipmentSearchFilter filter, PageRequest pageRequest)
  ```

* **`GetAvailableForRentEquipmentsService` (new service — `rental/application/service/`):**
  Implements the use case interface. Dependencies:
    - `EquipmentFacade` — cross-module call for physical candidates
    - `EquipmentAvailabilityService` — rental-internal occupancy check

  Logic:
    1. Call `EquipmentFacade.getEquipmentsByConditions(Set.of(Condition.GOOD), filter)`.
    2. If result is empty → return `Page.empty(pageRequest)` immediately.
    3. Extract `Long` IDs from the result list.
    4. Call `EquipmentAvailabilityService.getUnavailableIds(ids)`.
    5. Filter the candidate list: remove entries whose ID is in the unavailable set.
    6. Apply in-memory pagination (offset + limit) over the filtered list.
    7. Return `Page<EquipmentInfo>` with `totalElements` = filtered list size.

* **`EquipmentFacade` (equipment module public interface — consumed):**
  No changes; `getEquipmentsByConditions` was added in EQUIP-002 FR-03.

* **`EquipmentAvailabilityService` (rental-internal — consumed):**
  No changes; introduced in FR-03.

---

## 3. Abstract Data Schema Changes

None. This story introduces only in-process service logic.

---

## 4. Component Contracts & Payloads

* **Interaction: `GetAvailableForRentEquipmentsService` → `EquipmentFacade`**
    * **Protocol:** In-process Facade call (cross-module, rental → equipment)
    * **Payload:**
      Input: `conditions = {GOOD}`, `filter = EquipmentSearchFilter(q)`
      Output: `List<EquipmentInfo>` (includes `condition` field per EQUIP-002 FR-02)

* **Interaction: `GetAvailableForRentEquipmentsService` → `EquipmentAvailabilityService`**
    * **Protocol:** In-process Spring bean call (rental-internal)
    * **Payload:**
      Input: `Set<Long>` of equipment IDs extracted from the Facade result
      Output: `Set<Long>` of IDs currently ACTIVE or ASSIGNED

* **Interaction: `RentalQueryController` → `GetAvailableForRentEquipmentsUseCase`**
    * **Protocol:** In-process use-case call
    * **Payload:**
      Input: `EquipmentSearchFilter`, `PageRequest`
      Output: `Page<EquipmentInfo>`

---

## 5. Updated Interaction Sequence

**Happy path — some equipment available:**

1. `RentalQueryController` calls `useCase.getAvailableEquipments(filter, page(0, 20))`.
2. Service calls `EquipmentFacade.getEquipmentsByConditions({GOOD}, filter)`.
3. Equipment module queries `equipments WHERE condition_slug = 'GOOD' [AND text filters]`.
4. Equipment module returns `[EquipmentInfo{id=1}, EquipmentInfo{id=2}, EquipmentInfo{id=3}]`.
5. Service extracts IDs: `{1L, 2L, 3L}`.
6. Service calls `EquipmentAvailabilityService.getUnavailableIds({1L, 2L, 3L})`.
7. Availability service queries `rental_equipments WHERE equipment_id IN (1,2,3) AND status IN ('ACTIVE','ASSIGNED')`.
8. Returns `{2L}` (equipment 2 is currently rented).
9. Service filters: removes `EquipmentInfo{id=2}` → available list = `[EquipmentInfo{id=1}, EquipmentInfo{id=3}]`.
10. Service applies in-memory pagination: page 0, size 20 → slice = full list (2 items).
11. Returns `Page{content=[...], totalElements=2, page=0, size=20}`.

**Early-exit path — no GOOD equipment matches filter:**

1. Service calls `EquipmentFacade.getEquipmentsByConditions({GOOD}, filter)`.
2. Equipment module returns `[]`.
3. Service returns `Page.empty(pageRequest)` immediately.
4. `EquipmentAvailabilityService` is NOT called.

**All occupied path:**

1. Equipment Facade returns 3 candidates.
2. Availability service returns all 3 as unavailable.
3. Filtered list is empty.
4. Service returns `Page{content=[], totalElements=0}`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — read-only; no authentication required (consistent with current policy).
* **Scale & Performance:** Two sequential DB queries per call. In-memory filtering and pagination are
  O(n) over the candidate list, which is bounded by the total GOOD-condition equipment count.
  For inventories under 1 000 units this is well within acceptable response time. Pagination is
  best-effort: `totalElements` reflects the filtered count, and a requested page may return fewer
  items than `pageRequest.size()` — this is a documented v1 limitation.
* **Spring Modulith compliance:** `GetAvailableForRentEquipmentsService` uses only `EquipmentFacade`
  from the equipment module (cross-module boundary respected). `EquipmentAvailabilityService` is
  rental-internal and not exposed outside the module.
