# System Design: EQUIP-002/FR-02 — Create `Condition` Enum and Map to `Equipment` Domain

## 1. Architectural Overview

This story introduces the `Condition` enum as a new shared domain type and threads it through all
layers of the equipment module — from the JPA entity up to the public Facade DTO (`EquipmentInfo`).
No new endpoints or use cases are added; the change is entirely within the mapping and model layers.

Because `Condition` must be visible to both the `equipment` and `rental` modules without creating a
cycle, it is placed in the `shared` module, which is declared as a shared Spring Modulith module in
`BikeRentalApplication` and is therefore accessible to all other modules without an explicit
dependency edge.

The `EquipmentInfo` record (the only public DTO crossing module boundaries) gains a `condition` field,
making physical condition observable to any caller of `EquipmentFacade`. The existing `statusSlug`
field and `isAvailable()` method on `EquipmentInfo` are left unchanged in this story.

---

## 2. Impacted Components

* **`shared` module (new type):**
  Gains the `Condition` enum with values `GOOD`, `MAINTENANCE`, `BROKEN`, `DECOMMISSIONED`. It carries
  no behaviour and references no other module type.

* **`EquipmentFacadeImpl` (EquipmentFacade public DTO — `EquipmentInfo`):**
  The `EquipmentInfo` record is extended with a `condition` field of type `Condition`. This is the only
  change to the public API surface of the equipment module in this story.

* **`EquipmentJpaEntity` (infrastructure/persistence):**
  The `condition_slug` column (added in FR-01) is mapped to a `Condition` enum field using
  `@Enumerated(EnumType.STRING)`, consistent with the existing approach for string-mapped columns in
  the same entity.

* **`Equipment` domain aggregate (domain/model):**
  Gains a `condition` field of type `Condition`. No new domain behaviour is added in this story.

* **MapStruct mappers that produce `EquipmentInfo`:**
  Any mapper in the equipment module that maps `Equipment` → `EquipmentInfo` (or
  `EquipmentJpaEntity` → domain → `EquipmentInfo`) must be updated to include the `condition` field.
  The project's `unmappedTargetPolicy=ERROR` build flag enforces this; the build will fail if any
  mapper is left incomplete.

---

## 3. Abstract Data Schema Changes

* **Entity: `equipments`**
    * **No new columns** — `condition_slug` was added in FR-01.
    * **ORM mapping change only:** the `condition_slug` column is now bound to the `Condition` enum via
      `@Enumerated(EnumType.STRING)` in `EquipmentJpaEntity`.

---

## 4. Component Contracts & Payloads

* **Interaction: `EquipmentFacadeImpl` → callers (e.g., `CreateRentalService`, `UpdateRentalService`)**
    * **Protocol:** In-process method call (Spring Modulith Facade)
    * **Payload Changes:**
      `EquipmentInfo` gains one new field:
      ```
      condition: Condition   // e.g. GOOD | MAINTENANCE | BROKEN | DECOMMISSIONED
      ```
      Existing callers that receive `EquipmentInfo` but do not yet use `condition` are unaffected at
      runtime (they simply ignore the new field).

---

## 5. Updated Interaction Sequence

The runtime sequence for any equipment fetch is unchanged in structure; only the data carried changes.

1. A use case (e.g., `GetEquipmentByIdService`) calls `EquipmentRepository.findById(id)`.
2. `EquipmentRepository` returns a domain `Equipment` aggregate with the `condition` field populated
   from `condition_slug` via the JPA entity mapper.
3. `EquipmentFacadeImpl` (or its internal mapper) maps `Equipment` → `EquipmentInfo`, including the
   new `condition` field.
4. The caller receives `EquipmentInfo` with `condition` set to the appropriate `Condition` enum value.

**Unhappy path — unknown slug value in DB:**

1. Hibernate reads a `condition_slug` value not present in `Condition`.
2. Hibernate throws `IllegalArgumentException` during result-set mapping.
3. The exception propagates as a 500 Internal Server Error (handled by global exception handler).
   No silent null or fallback occurs.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — no new data exposure beyond what the equipment endpoints already return.
* **Scale & Performance:** Enum mapping is resolved at compile time by Hibernate; no additional DB
  queries are introduced. The `condition` field adds negligible payload size to `EquipmentInfo`.
