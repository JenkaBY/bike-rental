# User Story: FR-01 — Add `condition_slug` Column to `equipments` Table

## 1. Description

**As a** system administrator managing the equipment catalogue,
**I want to** have a dedicated `condition_slug` column in the `equipments` table that records the physical
condition of each piece of equipment,
**So that** the equipment module can track physical state independently of rental operational status.

## 2. Context & Business Rules

* **Trigger:** This migration runs automatically on application start-up via Liquibase.
* **Implementation approach:** Add the `condition_slug` column definition **directly into the existing**
  `equipments.create-table.xml` changeset — do not create a new changeset file. Backward compatibility
  with already-applied changesets is not a concern for this project.
* **Rules Enforced:**
    - The column is `VARCHAR(50) NOT NULL DEFAULT 'GOOD'`.
    - Valid stored values are: `GOOD`, `MAINTENANCE`, `BROKEN`, `DECOMMISSIONED`.
    - The column is placed after `status_slug` in the `<createTable>` block.
    - The existing `status_slug` column is NOT dropped or modified.
    - No foreign key is required for `condition_slug` (unlike `status_slug` which references `equipment_statuses`);
      valid values are enforced at the application layer via the `Condition` enum.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The `DEFAULT` clause on a new column avoids a table rewrite in PostgreSQL.
* **Security/Compliance:** N/A — no rollback changeset required (direct modification approach).
* **Usability/Other:** No downtime expected; the column is additive with a default value.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Migration runs on a database without the column**

* **Given** a PostgreSQL database where the `equipments` table does not yet have a `condition_slug` column
* **When** the application starts and Liquibase applies the pending changeset
* **Then** the `equipments` table has a `condition_slug VARCHAR(50) NOT NULL DEFAULT 'GOOD'` column
* **And** all pre-existing rows have `condition_slug = 'GOOD'`

**Scenario 2: Migration is idempotent on a database where column already exists**

* **Given** a database where the `condition_slug` column already exists (e.g., re-run after partial failure)
* **When** Liquibase processes the changeset
* **Then** the migration is skipped without error (Liquibase checksum match)

**Scenario 3: New equipment row inserted without specifying condition**

* **Given** the migration has been applied
* **When** a new row is inserted into `equipments` without providing a `condition_slug` value
* **Then** the row has `condition_slug = 'GOOD'`

## 5. Out of Scope

* Dropping or altering the existing `status_slug` column — deferred to a separate migration task.
* Adding a foreign key constraint or lookup table for `condition_slug` values — enforced at application layer only.
* Populating `condition_slug` with values other than `GOOD` for existing rows — out of scope for this migration.
