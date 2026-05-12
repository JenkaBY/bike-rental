# System Design: RENTAL-002/FR-01 — Unique Partial Index on `rental_equipments`

## 1. Architectural Overview

This story is a purely additive database migration with no changes to any component's business logic.
It adds a `UNIQUE` partial index to the `rental_equipments` table that enforces a hard data-integrity
guarantee: a given piece of equipment can only appear in one row with status `ACTIVE` or `ASSIGNED`
at any point in time, making double-booking physically impossible at the database level regardless of
application-layer concurrency.

The migration is applied by adding a raw `<sql>` block directly into the existing
`rental-equipments.create-table.xml` Liquibase changeset (no new changeset file; backward
compatibility with already-applied changesets is not required in this project).

---

## 2. Impacted Components

* **`bike-rental-db` (PostgreSQL Data Store):**
  Gains a new unique partial index `idx_rental_equipments_one_active` on
  `rental_equipments(equipment_id) WHERE status IN ('ACTIVE', 'ASSIGNED')`.
  No table structure changes; the index is purely a constraint enforcement mechanism.

---

## 3. Abstract Data Schema Changes

* **Entity: `rental_equipments`**
    * **Attributes Added/Modified:** None — no column changes.
    * **Constraints Added:**
      `UNIQUE INDEX idx_rental_equipments_one_active ON rental_equipments(equipment_id) WHERE status IN ('ACTIVE', 'ASSIGNED')`
      — at most one row per `equipment_id` may be in `ACTIVE` or `ASSIGNED` status simultaneously.
      Rows in `RETURNED` status are excluded from the partial index and are not constrained.

---

## 4. Component Contracts & Payloads

No component contracts change in this story. The constraint is transparent to the application under
normal operation; it only surfaces when an attempt is made to insert or update a row that would
violate the uniqueness rule. In that case, PostgreSQL raises a unique constraint violation, which
propagates to the application as a `DataIntegrityViolationException` and must be mapped to a
`409 Conflict` response by the global exception handler.

---

## 5. Updated Interaction Sequence

This story has no new runtime interaction sequence. The only interaction is at migration time:

1. `Liquibase` reads the modified `rental-equipments.create-table.xml` changeset at application start-up.
2. `Liquibase` verifies the changeset has not already been applied (checksum check).
3. `Liquibase` executes the DDL via the embedded `<sql>` block:
   ```sql
   CREATE UNIQUE INDEX idx_rental_equipments_one_active
       ON rental_equipments (equipment_id)
       WHERE status IN ('ACTIVE', 'ASSIGNED');
   ```
4. Application context continues initialising normally.

**Constraint violation path (post-migration, concurrent write scenario):**

1. Two concurrent transactions each attempt to insert a row for the same `equipment_id` with
   `status = 'ACTIVE'`.
2. One transaction commits first; the second transaction is rejected by the index with a unique
   constraint violation.
3. The rejected transaction surfaces as a `DataIntegrityViolationException` in the application.
4. The global exception handler maps this to `409 Conflict` with `errorCode: EQUIPMENT_NOT_AVAILABLE`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A — schema-only migration.
* **Scale & Performance:** Partial indexes cover only in-flight rows (a small fraction of the total
  `rental_equipments` table). Index maintenance cost is negligible. The index also accelerates the
  availability query introduced in FR-02, which filters on exactly this condition.
