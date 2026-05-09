# System Design: FR-02 — Introduce EquipmentSpec and Migrate Repository Search to JPA Specification API

## 1. Architectural Overview

FR-02 is a purely infrastructure-layer refactoring within the `equipment` module. It replaces the hand-written
JPQL method `findAllByFilters` on `EquipmentJpaRepository` with a declarative JPA Specification built from a new
`EquipmentSpec` interface, following the established `CustomerSpec` / `SpecificationBuilder` pattern already present
in the `customer` module.

No component contract visible to the application or domain layer changes in this story beyond what FR-01 already
requires (the new `searchText` argument on `EquipmentRepository.findAll()`). All changes are encapsulated inside the
infrastructure sub-package of the `equipment` module. The public API surface and all module boundaries remain intact.

## 2. Impacted Components

* **`EquipmentJpaRepository` (Infrastructure — JPA Repository):** Must extend
  `JpaSpecificationExecutor<EquipmentJpaEntity>` to gain the `findAll(Specification, Pageable)` method. The existing
  custom JPQL query method `findAllByFilters` must be removed; it is fully superseded.

* **`EquipmentRepositoryAdapter` (Infrastructure — Repository Adapter):** The `findAll()` method must be rewritten to
  use `SpecificationBuilder.specification(EquipmentSpec.class)` with `.withParam("status", statusSlug)`,
  `.withParam("type", typeSlug)`, and `.withParam("q", searchText)` to build the composite specification, then
  delegate to `jpaRepository.findAll(spec, pageable)`.

* **`EquipmentSpec` (Infrastructure — Specification Interface, NEW):** A new interface in
  `equipment/infrastructure/persistence/specification/` extending `Specification<EquipmentJpaEntity>`. Annotated
  with `@And` composing:
    * `@Spec(path="statusSlug", params="status", spec=Equal)` — exact match, omitted when param is absent.
    * `@Spec(path="typeSlug", params="type", spec=Equal)` — exact match, omitted when param is absent.
    * `@Or` grouping three `@Spec` entries, all bound to `params="q"`, using `LikeIgnoreCase`:
        * `path="uid"`
        * `path="serialNumber"`
        * `path="model"`

* **`EquipmentSpecConstant` (Infrastructure — Constant Utility, NEW):** A companion `@UtilityClass` in the same
  package holding string constants for the field paths and parameter names used in `EquipmentSpec` annotations,
  mirroring `SpecConstant` from the `customer` module.

## 3. Abstract Data Schema Changes

No schema changes. All matched columns (`status_slug`, `type_slug`, `uid`, `serial_number`, `model`) already exist in
the `equipments` table. The Specification API generates predicates against these columns using JDBC bind parameters.

## 4. Component Contracts & Payloads

* **Interaction: `EquipmentRepositoryAdapter` → `EquipmentJpaRepository`**
    * **Protocol:** In-process Spring Data JPA call
    * **Payload Changes:** The call site changes from `findAllByFilters(statusSlug, typeSlug, pageable)` to
      `findAll(Specification<EquipmentJpaEntity>, Pageable)`. The specification encodes all filter predicates. The
      return type remains `Page<EquipmentJpaEntity>`.

* **Interaction: `EquipmentJpaRepository` → `bike-rental-db`**
    * **Protocol:** JDBC / SQL
    * **Payload Changes:** Generated SQL `WHERE` clause changes from a static JPQL template to a dynamically
      composed predicate built by Hibernate from the active `Specification`. All values remain bind parameters.
      When `q` is supplied the clause gains an OR group:
      `(uid ILIKE ? OR serial_number ILIKE ? OR model ILIKE ?)` with `%value%` bindings.

## 5. Updated Interaction Sequence

**Specification construction (internal to `EquipmentRepositoryAdapter.findAll()`):**

1. `EquipmentRepositoryAdapter` receives `(statusSlug, typeSlug, searchText, pageRequest)`.
2. Adapter maps `pageRequest` to a Spring `Pageable` via `PageMapper`.
3. Adapter calls `SpecificationBuilder.specification(EquipmentSpec.class)`
   `.withParam("status", statusSlug)`
   `.withParam("type", typeSlug)`
   `.withParam("q", searchText)`
   `.build()` to produce `Specification<EquipmentJpaEntity>`.
4. Adapter calls `jpaRepository.findAll(spec, pageable)`.
5. Hibernate generates and executes a single SQL statement with all active predicates in the `WHERE` clause.
6. The resulting `Page<EquipmentJpaEntity>` is mapped to `Page<Equipment>` via `EquipmentJpaMapper` and returned.

**Null / blank params:**

1. When `statusSlug`, `typeSlug`, or `searchText` are null or blank, the library omits the corresponding
   `@Spec` clause from the generated predicate automatically.
2. If all params are absent, `findAll(spec, pageable)` returns all rows paged.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** All filter values are bound as JDBC parameters by the `spec-arg-resolver` library and
  Hibernate; no user-controlled string is concatenated into SQL at any layer.
* **Scale & Performance:** A single SQL statement is issued per search invocation. The `ILIKE` predicates for `q`
  operate as full-column scans unless a functional index exists; this is noted as a future optimisation concern
  outside this story's scope. The `status_slug` and `type_slug` equality predicates benefit from existing indexes
  if present.
