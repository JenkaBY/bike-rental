# System Design: EQUIP-002/FR-03 — Add `getEquipmentsByConditions` to `EquipmentFacade`

## 1. Architectural Overview

This story expands the equipment module's public Facade API with a new query method that allows other
modules to retrieve equipment filtered by physical condition and optional text criteria. It also
introduces `EquipmentSearchFilter` — a new public record in the equipment module's API package — as the
structured parameter type for the text filters.

The implementation extends the existing `EquipmentSpec` Specification pattern with a new
`conditionIn` predicate. No new REST endpoint is added; the method is consumed only by in-process
callers (specifically `RENTAL-002`'s `GetAvailableForRentEquipmentsService`). The dependency direction
remains strictly rental → equipment via the Facade.

---

## 2. Impacted Components

* **`EquipmentFacade` (public interface):**
  Gains one new method:
  ```
  List<EquipmentInfo> getEquipmentsByConditions(Set<Condition> conditions, EquipmentSearchFilter filter)
  ```

* **`EquipmentFacadeImpl` (Service):**
  Implements the new method. Delegates to a new use case
  `GetEquipmentsByConditionsUseCase` / `GetEquipmentsByConditionsService`.

* **`EquipmentSearchFilter` (new public type in equipment module API package):**
  A record with a single optional field: `q` (nullable String). Placed in the equipment module's
  public API package so it can be referenced by callers without violating module boundaries.
  Provides a static factory `empty()` that returns an instance with `q = null`.

* **`EquipmentSpec` (infrastructure/persistence/specification):**
  Gains a new predicate `conditionIn(Set<Condition> conditions)` that generates a
  `condition_slug IN (...)` SQL predicate. The existing OR text-search predicate (`uid`, `model`,
  `serialNumber` via partial case-insensitive match) is reused as-is.

* **`GetEquipmentsByConditionsService` (new application service):**
  Accepts `Set<Condition>` and `EquipmentSearchFilter`, builds a combined Specification (condition
  predicate AND text OR predicate when filter fields are non-null), and delegates to
  `EquipmentRepository.findAll(Specification, Sort)`. Returns an unpaged `List<EquipmentInfo>`.

---

## 3. Abstract Data Schema Changes

No schema changes. This story uses the `condition_slug` column added in FR-01 and mapped in FR-02.

---

## 4. Component Contracts & Payloads

* **Interaction: caller module → `EquipmentFacade.getEquipmentsByConditions`**
    * **Protocol:** In-process method call (Spring Modulith Facade)
    * **Payload Changes (new method signature):**

      Input:
      ```
      conditions: Set<Condition>          // non-null, non-empty; e.g. {GOOD}
      filter: EquipmentSearchFilter       // uid / model / serialNumber — all nullable
      ```

      Output:
      ```
      List<EquipmentInfo>                 // may be empty; never null
      ```

      Error:
      ```
      IllegalArgumentException            // thrown when conditions is empty
      ```

* **Interaction: `GetEquipmentsByConditionsService` → `EquipmentRepository`**
    * **Protocol:** Spring Data JPA (`findAll(Specification, Sort)`)
    * **Payload Changes:** A new `Specification<EquipmentJpaEntity>` is constructed combining:
        - `EquipmentSpec.conditionIn(conditions)` — mandatory
        - `EquipmentSpec.textSearch(filter.q())` — applied only when `q` is non-null, composed with AND
          against the condition predicate (reuses the existing `q` OR predicate across uid/model/serialNumber)

---

## 5. Updated Interaction Sequence

**Happy path — caller requests GOOD equipment with an optional text filter:**

1. `GetAvailableForRentEquipmentsService` (rental module) calls
   `EquipmentFacade.getEquipmentsByConditions(Set.of(GOOD), filter)`.
2. `EquipmentFacadeImpl` delegates to `GetEquipmentsByConditionsService`.
3. `GetEquipmentsByConditionsService` validates that `conditions` is non-empty.
4. Service builds `Specification`: `conditionIn({GOOD})` AND (if `filter.q()` is non-null) `textSearch(filter.q())`.
5. Service calls `EquipmentRepository.findAll(spec, sort)`.
6. Repository executes:
   `SELECT * FROM equipments WHERE condition_slug IN ('GOOD') [AND (uid ILIKE '%x%' OR model ILIKE '%x%' OR serial_number ILIKE '%x%')]`
7. Result rows are mapped `EquipmentJpaEntity` → `Equipment` → `EquipmentInfo` (including `condition`).
8. `List<EquipmentInfo>` is returned to the caller.

**Unhappy path — empty conditions set:**

1. Caller passes `Set.of()` as conditions.
2. `GetEquipmentsByConditionsService` throws `IllegalArgumentException` immediately.
3. No DB query is issued.

**Unhappy path — no equipment matches:**

1. Query executes; no rows match.
2. An empty `List` is returned. No exception is thrown.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Read-only, no side effects. Consistent with all other Facade query methods.
* **Scale & Performance:** The `condition_slug` column should be indexed for efficient `IN` queries
  on large tables. The method returns an unpaged list; callers are responsible for slicing. For the
  immediate consumer (`RENTAL-002`), inventory size is expected to be small enough that a full
  condition-filtered list is safe to retrieve before in-memory availability filtering.
