# User Story: FR-03 — Add `getEquipmentsByConditions` to `EquipmentFacade`

## 1. Description

**As a** developer in the rental module,
**I want to** query the equipment module for physical candidates filtered by condition and optional text fields,
**So that** the rental module can determine which equipment is physically eligible for rent without directly
accessing equipment internals.

## 2. Context & Business Rules

* **Trigger:** FR-02 must be completed (the `Condition` enum and entity mapping must exist) before this story
  is implemented.
* **Rules Enforced:**
    - The method is added to the existing `EquipmentFacade` interface (public module API):
      ```
      List<EquipmentInfo> getEquipmentsByConditions(Set<Condition> conditions, EquipmentSearchFilter filter)
      ```
    - `EquipmentSearchFilter` is a new record in the equipment module's public API package with a single
      optional field: `q` (nullable String). This aligns with the existing `EquipmentSpec` `q` search
      pattern that already performs a case-insensitive partial match OR across uid, model, and serialNumber.
    - When `q` is non-null, the implementation applies a case-insensitive partial match against uid, model,
      and serialNumber using OR semantics (consistent with the existing `EquipmentSpec` text predicate).
    - `conditions` is never null and never empty — callers must supply at least one condition value.
      If the set is empty the implementation throws `IllegalArgumentException`.
    - `EquipmentSearchFilter` with `q = null` means "no text filter applied — return all equipment
      matching the given conditions."
    - The implementation reuses the existing `EquipmentSpec` Specification approach; a new condition
      predicate is added to filter by `condition_slug IN (conditions)`.
    - The method does NOT apply pagination — it returns a plain `List`. The caller is responsible for any
      further slicing.
    - Return type is `List<EquipmentInfo>` (the existing public record, extended with `condition` field per
      FR-02).
    - This method has no side effects; it is read-only.
    - No new REST endpoint is added to the equipment module for this method.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The query must use indexed access on `condition_slug`; no full table scan for small
  inventories. For large data sets, callers should apply additional filters via `EquipmentSearchFilter`
  to reduce result size before pagination.
* **Security/Compliance:** No authentication required (consistent with the rest of the API); this is a
  read-only internal Facade method.
* **Usability/Other:** Spring Modulith module boundary: `EquipmentFacade`, `EquipmentInfo`,
  `EquipmentSearchFilter`, and `Condition` are the only public types consumed by external modules.
  No internal equipment types (JPA entities, domain models, repositories) are visible outside the module.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Filter by single condition with no text filter**

* **Given** the equipment catalogue contains 3 GOOD bikes, 1 BROKEN bike, and 1 MAINTENANCE bike
* **When** `getEquipmentsByConditions(Set.of(GOOD), EquipmentSearchFilter.empty())` is called
* **Then** a list of 3 `EquipmentInfo` records is returned, all with `condition = GOOD`

**Scenario 2: Filter by multiple conditions**

* **Given** the catalogue contains 3 GOOD and 2 MAINTENANCE bikes
* **When** `getEquipmentsByConditions(Set.of(GOOD, MAINTENANCE), EquipmentSearchFilter.empty())` is called
* **Then** a list of 5 records is returned

**Scenario 3: Text filter applied as OR across uid, model, serialNumber**

* **Given** equipment A has `uid = "BIKE-001"`, equipment B has `model = "BIKE-001-PRO"`, equipment C has
  `serialNumber = "SN-999"`, all with condition GOOD
* **When** `getEquipmentsByConditions(Set.of(GOOD), EquipmentSearchFilter.of("BIKE-001"))` is called
* **Then** equipment A and B are returned (both match the OR pattern on uid/model); equipment C is not returned

**Scenario 4: Empty result when no equipment matches**

* **Given** no equipment with condition DECOMMISSIONED exists
* **When** `getEquipmentsByConditions(Set.of(DECOMMISSIONED), EquipmentSearchFilter.empty())` is called
* **Then** an empty list is returned (not an exception)

**Scenario 5: Empty conditions set is rejected**

* **Given** a caller passes an empty Set for conditions
* **When** `getEquipmentsByConditions(Set.of(), ...)` is called
* **Then** `IllegalArgumentException` is thrown with a descriptive message

**Scenario 6: No Spring Modulith boundary violation**

* **Given** the application starts
* **When** Spring Modulith verifies module boundaries
* **Then** no cycle is reported; rental → equipment dependency is one-way via the Facade only

## 5. Out of Scope

* Pagination inside the Facade method — the caller handles slicing.
* Sorting — not specified; implementation may use any stable order (e.g., `id ASC`).
* New REST endpoints in the equipment module for this filter.
* Filtering by `status_slug` — that column is intentionally not used by this method.
