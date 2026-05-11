# System Design: EQUIP-002/FR-01 — Add `condition_slug` Column to `equipments` Table

## 1. Architectural Overview

This story is a purely additive database migration with no changes to any component's business logic or
public API. It introduces a new column, `condition_slug`, to the `equipments` table to provide a
dedicated slot for the physical condition of equipment — separate from the operational `status_slug`
column that is currently overloaded with rental lifecycle states.

The migration is applied by modifying the existing `equipments.create-table.xml` Liquibase changeset
directly (backward compatibility with already-applied changesets is not required in this project). No
new components, Facade methods, or domain types are introduced in this story; those follow in FR-02.

---

## 2. Impacted Components

* **`bike-rental-db` (PostgreSQL Data Store):**
  Gains a new `condition_slug VARCHAR(50) NOT NULL DEFAULT 'GOOD'` column on the `equipments` table,
  placed after `status_slug`. The column has no foreign key constraint; valid values are enforced at
  the application layer by the `Condition` enum introduced in FR-02.

---

## 3. Abstract Data Schema Changes

* **Entity: `equipments`**
    * **Attributes Added:**
      `condition_slug VARCHAR(50) NOT NULL DEFAULT 'GOOD'` — stores the physical condition of the
      equipment unit. Permitted string values: `GOOD`, `MAINTENANCE`, `BROKEN`, `DECOMMISSIONED`.
      All pre-existing rows receive `GOOD` via the column default at migration time.
    * **Attributes Unchanged:**
      `status_slug` remains as-is; it continues to reference the `equipment_statuses` lookup table and
      is updated by rental lifecycle events until FR-04 disables those listeners.

---

## 4. Component Contracts & Payloads

No component contracts change in this story. The new column is invisible to all current callers until
FR-02 maps it to the domain model.

---

## 5. Updated Interaction Sequence

This story has no runtime interaction sequence change. The only interaction is:

1. `Liquibase` reads the modified `equipments.create-table.xml` changeset at application start-up.
2. `Liquibase` verifies the changeset has not already been applied (checksum check).
3. `Liquibase` executes the DDL: adds `condition_slug VARCHAR(50) NOT NULL DEFAULT 'GOOD'` to the
   `equipments` table.
4. All existing rows receive `condition_slug = 'GOOD'` automatically via the column default.
5. Application context continues initialising normally.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — this is a schema-only migration; no data is exposed.
* **Scale & Performance:** PostgreSQL adds a column with a `DEFAULT` value without rewriting the table
  (fast DDL). The migration is safe to run on a live database with no downtime.
