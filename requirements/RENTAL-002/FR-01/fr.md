# User Story: FR-01 — Add Unique Partial Index on `rental_equipments` for Active/Assigned Status

## 1. Description

**As a** system,
**I want to** enforce at the database level that a given piece of equipment can appear in at most one
ACTIVE or ASSIGNED rental at any time,
**So that** double-booking of equipment is impossible even under concurrent write conditions.

## 2. Context & Business Rules

* **Trigger:** This migration runs automatically on application start-up via Liquibase.
* **Implementation approach:** Add the index definition **directly into the existing**
  `rental-equipments.create-table.xml` changeset — do not create a new changeset file. Backward
  compatibility with already-applied changesets is not a concern for this project.
  Because Liquibase XML has no native partial index element, use a raw `<sql>` block inside the
  existing changeset.
* **Rules Enforced:**
    - A `UNIQUE` partial index is created on `rental_equipments(equipment_id)` with the condition
      `WHERE status IN ('ACTIVE', 'ASSIGNED')`.
    - Index name: `idx_rental_equipments_one_active`.
    - The uniqueness constraint covers only rows in `ACTIVE` or `ASSIGNED` status; rows in `RETURNED`
      status are not constrained (the same equipment may appear in many past rentals).
    - If an `INSERT` or `UPDATE` would create a second ACTIVE/ASSIGNED row for the same `equipment_id`,
      PostgreSQL raises a unique constraint violation, which the application must surface as a 409 Conflict.
    - Only one index is created. The initially proposed non-unique partial index on the same
      column/condition was dropped as redundant — the unique index already serves both roles.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The partial index is small (only covers in-flight rows) and benefits availability
  look-ups in `RENTAL-002 FR-02`.
* **Security/Compliance:** The constraint provides a hard data-integrity guarantee independent of
  application-layer validation.
* **Usability/Other:** No rollback changeset required (direct modification approach).

## 4. Acceptance Criteria (BDD)

**Scenario 1: Index is created by the migration**

* **Given** a `rental_equipments` table without the index
* **When** Liquibase applies the changeset
* **Then** the index `idx_rental_equipments_one_active` exists with UNIQUE and the partial WHERE clause

**Scenario 2: Inserting a second ACTIVE row for the same equipment is rejected by the DB**

* **Given** equipment ID 42 already has a row in `rental_equipments` with `status = 'ACTIVE'`
* **When** an `INSERT` of a new row with `equipment_id = 42` and `status = 'ACTIVE'` is attempted
* **Then** PostgreSQL raises a unique constraint violation error

**Scenario 3: RETURNED rows are not constrained**

* **Given** equipment ID 42 has many `RETURNED` rows in `rental_equipments`
* **When** a new `ACTIVE` row is inserted for equipment ID 42
* **Then** the insert succeeds (RETURNED rows are excluded from the partial index)

**Scenario 4: ASSIGNED → ACTIVE transition does not violate the index**

* **Given** equipment ID 42 has exactly one row with `status = 'ASSIGNED'`
* **When** that row's status is updated to `'ACTIVE'`
* **Then** the update succeeds (still only one in-flight row for equipment 42)

## 5. Out of Scope

* Application-layer handling of the constraint violation — handled by the existing global exception
  handler and addressed in RENTAL-002 FR-04.
* Non-unique partial index on the same condition — explicitly dropped as redundant.
* Indexes on other columns of `rental_equipments`.
