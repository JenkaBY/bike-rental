# User Story: FR-02 — Create `Condition` Enum and Map to `Equipment` Domain

## 1. Description

**As a** developer working in the equipment module,
**I want to** have a `Condition` enum in the shared module and have it mapped from the `condition_slug` column
in both the JPA entity and the domain aggregate,
**So that** the physical condition of equipment is represented as a type-safe domain concept across all layers.

## 2. Context & Business Rules

* **Trigger:** FR-01 (adding the `condition_slug` column) must be completed before this story is implemented.
* **Rules Enforced:**
    - `Condition` enum is placed in the `shared` module so it is accessible to both the `equipment` and `rental`
      modules without creating a circular dependency.
    - Valid values: `GOOD`, `MAINTENANCE`, `BROKEN`, `DECOMMISSIONED`.
        - `GOOD` — equipment is in normal working order and may be rented.
        - `MAINTENANCE` — equipment is temporarily out of service for repair.
        - `BROKEN` — equipment is damaged and not usable.
        - `DECOMMISSIONED` — equipment has been permanently retired from the inventory.
    - `EquipmentJpaEntity` maps `condition_slug` using `@Enumerated(EnumType.STRING)` or column-name mapping
      consistent with the existing `status_slug` mapping pattern in the same entity.
    - The domain `Equipment` aggregate carries a `condition` field of type `Condition`.
    - `EquipmentInfo` (the Facade's public DTO) must be extended with a `condition` field of type `Condition`
      so consumers can observe physical condition.
    - `Condition` must NOT contain any references to rental concepts (statuses such as AVAILABLE, RESERVED,
      RENTED must not appear in this enum).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No performance impact — enum mapping is resolved at compile time; no extra DB queries.
* **Security/Compliance:** N/A.
* **Usability/Other:** The MapStruct `unmappedTargetPolicy=ERROR` build flag means any unmapped field in
  `EquipmentInfo` will cause a build failure; the mapper(s) that produce `EquipmentInfo` must be updated to
  include the new `condition` field.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Equipment with default condition is retrieved via Facade**

* **Given** an equipment row in the database with `condition_slug = 'GOOD'`
* **When** the equipment is fetched through any use case
* **Then** the resulting `EquipmentInfo` has `condition = Condition.GOOD`

**Scenario 2: Equipment with non-default condition is correctly mapped**

* **Given** an equipment row with `condition_slug = 'BROKEN'`
* **When** the equipment is fetched
* **Then** `EquipmentInfo.condition` equals `Condition.BROKEN`

**Scenario 3: Unknown slug value in database causes a descriptive error**

* **Given** a row where `condition_slug` contains a value not present in the `Condition` enum
* **When** the ORM attempts to map that row
* **Then** a mapping exception is thrown (Hibernate `IllegalArgumentException`) — no silent null or fallback

**Scenario 4: Application builds successfully with no unmapped fields**

* **Given** the `condition` field is added to `EquipmentInfo`
* **When** the project is compiled
* **Then** the build succeeds; no MapStruct `unmappedTargetPolicy` errors are reported

## 5. Out of Scope

* Creating REST endpoints to change `condition` — this story is domain/mapping only.
* Validation rules governing condition transitions — out of scope for this story.
* Removing `status_slug` or the `EquipmentInfo.statusSlug` field.
* Removing `EquipmentInfo.isAvailable()` — deferred to a follow-up task.
