---
paths:
  - "service/src/main/resources/db/changelog/**"
---

# Liquibase Changelogs — hard constraints

- **Time columns are ALWAYS `TIMESTAMP WITH TIME ZONE`** — audit (`created_at`, `updated_at`) and business
  (`started_at`, `expected_return_at`) alike.
- One logical change per file; file name `{table}.{action}-table_{specific-action}.xml`; changeset id follows the same
  pattern (e.g. `customers.update-table_add-status-column`).
- Changeset `author="claude"` (consistent attribution).
- Guard with `<preConditions onFail="MARK_RAN">` checking existing structures.
- **No rollback sections.**
- Naming: tables plural snake_case; `idx_{table}_{column}`, `fk_{source}_{target}`, `uq_{table}_{column}`.
- Register new files at the **bottom** of `db.changelog-master.xml`; organize by version folder (v1, v2, ...).
- Define PKs, unique and not-null constraints at table creation; add indexes in the same changeset when possible.

Depth: `liquibase` skill.
