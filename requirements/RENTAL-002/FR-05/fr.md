# User Story: FR-05 — `GetAvailableForRentEquipmentsUseCase` and Implementation

## 1. Description

**As a** rental operations system,
**I want to** retrieve a list of equipment that is both physically fit (GOOD condition) and not currently
occupied by any active or assigned rental,
**So that** rental staff or end users can see which equipment is genuinely available for a new rental.

## 2. Context & Business Rules

* **Trigger:** EQUIP-002 FR-03 (`EquipmentFacade.getEquipmentsByConditions`) and RENTAL-002 FR-03
  (`EquipmentAvailabilityService`) must both be complete before this story is implemented.
* **Rules Enforced:**
    - A use-case interface is created in `rental/application/usecase/`:
      ```
      Page<EquipmentInfo> getAvailableEquipments(EquipmentSearchFilter filter, PageRequest pageRequest)
      ```
      `EquipmentSearchFilter` carries a single optional `q` field (partial, case-insensitive text
      matched OR-style against uid, model, and serialNumber by the equipment module).
    - The implementation (`GetAvailableForRentEquipmentsService`) follows this two-step logic:

      **Step 1 — Physical candidates:**
      Call `EquipmentFacade.getEquipmentsByConditions(Set.of(Condition.GOOD), filter)`.
      The `filter` is passed through unchanged from the caller.

      **Step 2 — Early exit:**
      If the returned list is empty, return an empty `Page` immediately (skip Step 3 and Step 4).

      **Step 3 — Operational filter:**
      Extract `Long` IDs from the candidate list.
      Call `EquipmentAvailabilityService.getUnavailableIds(Set<Long>)`.

      **Step 4 — Result:**
      Remove from the candidate list any entries whose ID is in the unavailable set.
      Apply in-memory pagination using `pageRequest` (offset + limit) over the filtered list.
      Return the resulting page.

    - **Pagination is best-effort:** the returned page may contain fewer items than `pageRequest.size()`
      because unavailable equipment is removed after the full GOOD-condition list is fetched. This is a
      known and documented limitation at v1.
    - The total count in the returned `Page` reflects the **filtered** count (GOOD and available),
      not the total GOOD count from the equipment module.
    - `EquipmentSearchFilter` and `PageRequest` are passed through from the calling controller; the use
      case does not construct them internally.
    - The use case is **availability** logic, which is a rental domain concept — it must live in the
      rental module, not the equipment module.
    - The use case must not expose `EquipmentAvailabilityService` to the controller directly.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Two DB queries per call (equipment module query + rental availability query).
  Acceptable at current inventory scale (<1 000 active equipment items). Pagination limitation is
  documented.
* **Security/Compliance:** N/A — read-only operation.
* **Usability/Other:** The `Page<EquipmentInfo>` return type reuses existing shared page infrastructure.
  The `EquipmentInfo` type (now containing `condition` from EQUIP-002 FR-02) is returned directly to
  the caller.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Returns only available GOOD equipment**

* **Given** equipment IDs {1, 2, 3} are all GOOD condition
* **And** equipment 2 is currently ACTIVE in a rental
* **When** `getAvailableEquipments(emptyFilter, page(0, 10))` is called
* **Then** the result contains equipment {1, 3}; equipment 2 is excluded

**Scenario 2: Empty candidate list returns early without availability query**

* **Given** no GOOD equipment exists matching the filter
* **When** `getAvailableEquipments(filter, pageRequest)` is called
* **Then** an empty page is returned; `EquipmentAvailabilityService` is NOT called

**Scenario 3: All candidates are occupied — empty page returned**

* **Given** equipment IDs {4, 5} are GOOD but both ASSIGNED in rentals
* **When** `getAvailableEquipments(emptyFilter, page(0, 10))` is called
* **Then** an empty page is returned

**Scenario 4: Text filter is applied to physical candidates**

* **Given** 5 GOOD bikes exist, 2 of which have `model` containing "MTB"
* **And** none are occupied
* **When** `getAvailableEquipments(EquipmentSearchFilter.of("MTB"), page(0, 10))` is called
* **Then** only the 2 MTB bikes are returned

**Scenario 5: Best-effort page size is smaller than requested**

* **Given** 10 GOOD bikes exist matching the filter
* **And** 4 are currently occupied
* **When** `getAvailableEquipments(emptyFilter, page(0, 10))` is called (size = 10)
* **Then** the page contains 6 items; `totalElements = 6`, `page = 0`
* **And** no error is thrown; the response is valid

## 5. Out of Scope

* Date-range availability filtering — not in scope at v1.
* Sorting options — not specified; default order from equipment module is used.
* True pagination with guaranteed page sizes — deferred (requires a different architectural approach).
* Caching results — deferred.
