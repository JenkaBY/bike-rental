# Implementation Checklist: FR-01 — Rental Optimistic Locking & Version in API

- [x] `task-001-rentals-add-version-column-changelog.md`
- [x] `task-002-register-changelog-include.md`
- [x] `task-003-rental-jpa-entity-version.md`
- [x] `task-004-rental-domain-version.md`
- [x] `task-005-rental-response-version.md`
- [x] `task-006-rental-response-transformer-version.md`
- [x] `task-007-rental-version-component-test.md`

**Next Task:** none — FR-01 implemented and verified (`:service:test` and `:component-test:test` green).

## Ad-hoc fixes discovered during the test stage (outside the original tasks)

- `component-test/.../transformer/RentalJpaEntityTransformer.java` — DB-seeded rentals now default
  `version` to `0` (the JDBC `JpaEntityInserter` inserts every entity field explicitly, so a null
  version violated the new NOT NULL column).
- `service/.../rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java` —
  `save()` uses `saveAndFlush()` so the incremented `@Version` value is visible in the domain
  object returned to the caller (responses must carry the fresh fencing token).
