# User Story: FR-02 — Introduce EquipmentSpec and Migrate Repository Search to JPA Specification API

## 1. Description

**As a** backend developer maintaining the equipment module  
**I want to** replace the hand-written JPQL filter query with a declarative JPA Specification built via
`EquipmentSpec`  
**So that** all equipment search filters (status, type, and the new free-text `q`) are managed in a single, composable,
extensible specification rather than a growing JPQL string

## 2. Context & Business Rules

* **Trigger:** Implementing FR-01 requires the persistence layer to support a dynamic, composable predicate for
  `status`, `type`, and the new OR-based text search across `uid`, `serialNumber`, and `model`.
* **Rules Enforced:**
    * `EquipmentSpec` must be created in the
      `equipment.infrastructure.persistence.specification` package, following the same pattern as `CustomerSpec`.
    * `EquipmentSpec` must extend `Specification<EquipmentJpaEntity>`.
    * The specification must combine all filters with AND logic at the top level:
        * `statusSlug` — exact equality (`Equal`), bound to HTTP param `status`.
        * `typeSlug` — exact equality (`Equal`), bound to HTTP param `type`.
        * OR group across `uid`, `serialNumber`, `model` — case-insensitive substring (`LikeIgnoreCase`), all three
          bound to HTTP param `q`.
    * When a filter param is absent or blank, that spec clause must be silently omitted (treated as no restriction) by
      the library's default behaviour.
    * `EquipmentJpaRepository` must extend `JpaSpecificationExecutor<EquipmentJpaEntity>` to gain
      `findAll(Specification, Pageable)`.
    * `EquipmentRepositoryAdapter.findAll()` must use `SpecificationBuilder.specification(EquipmentSpec.class)`
      with `.withParam("status", statusSlug)`, `.withParam("type", typeSlug)`, `.withParam("q", searchText)` to build
      the specification, then delegate to `jpaRepository.findAll(spec, pageable)`.
    * A companion `EquipmentSpecConstant` utility class must hold the field name constants used in `@Spec` path and
      params attributes, mirroring the `SpecConstant` pattern from the customer module.
    * The existing custom JPQL method `findAllByFilters` on `EquipmentJpaRepository` must be removed; it is fully
      superseded by the Specification approach.
    * No changes to the domain layer (`EquipmentRepository` interface contract changes are covered in FR-01).
    * No changes to the application layer beyond what is already required by FR-01.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The generated SQL must apply all active filters in a single `WHERE` clause without additional
  round-trips.
* **Security/Compliance:** All filter values must be passed as JDBC bind parameters, never string-concatenated into
  SQL.
* **Usability/Other:** Removing `findAllByFilters` must not break any existing tests; existing integration/component
  tests that cover the `GET /api/equipments` endpoint must continue to pass unchanged.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Specification correctly filters by status**

* **Given** `EquipmentSpec` is built with `status=available`
* **When** `jpaRepository.findAll(spec, pageable)` is executed
* **Then** the resulting SQL `WHERE` clause contains a predicate on `status_slug = 'available'` and returns only
  equipment with that status

**Scenario 2: Specification correctly filters by free-text query (OR across fields)**

* **Given** `EquipmentSpec` is built with `q=bike`
* **When** the query is executed
* **Then** the `WHERE` clause contains `(uid ILIKE '%bike%' OR serial_number ILIKE '%bike%' OR model ILIKE '%bike%')`
  and only matching equipment is returned

**Scenario 3: All absent params produce no WHERE restrictions**

* **Given** `EquipmentSpec` is built with no params (all null/blank)
* **When** the query is executed
* **Then** all equipment rows are returned with no filter applied

**Scenario 4: Combined params produce AND-ed predicates**

* **Given** `EquipmentSpec` is built with `status=available` and `q=bike`
* **When** the query is executed
* **Then** the `WHERE` clause AND-s the status equality with the OR group, returning only available equipment that also
  matches the text query

**Scenario 5: `findAllByFilters` no longer exists on `EquipmentJpaRepository`**

* **Given** the refactoring is complete
* **When** the codebase is compiled
* **Then** `findAllByFilters` is absent and no compilation error occurs

## 5. Out of Scope

* Caching of specification results.
* Sorting by any field other than the currently defaulted `serialNumber` ASC.
* Migration to any other query abstraction (QueryDSL, Criteria API directly, etc.).
* Changes to any repository other than `EquipmentJpaRepository` and `EquipmentRepositoryAdapter`.
