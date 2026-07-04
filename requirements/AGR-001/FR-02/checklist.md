# Implementation Checklist: FR-02 — Rental AWAITING_SIGNATURE Status & Signing Public API

- [x] `task-001-rental-status-awaiting-signature.md`
- [x] `task-002-rental-domain-signing-methods.md`
- [x] `task-003-prepare-cancel-signing-usecases.md`
- [x] `task-007-rental-signing-module-api-dtos.md`
- [x] `task-004-prepare-signing-service.md`
- [x] `task-005-cancel-signing-service.md`
- [x] `task-008-complete-signing-service.md`
- [x] `task-009-rental-signing-facade.md`
- [x] `task-006-rental-lifecycle-service-switch.md`
- [x] `task-010-lifecycle-status-enum.md`
- [x] `task-011-lifecycle-webmvc-test-update.md`
- [x] `task-012-signing-lifecycle-component-tests.md`

**Next Task:** none — FR-02 implemented and verified (`:service:test` and `:component-test:test` green).

## Ad-hoc fixes discovered during the test stage (outside the original tasks)

- `service/src/test/java/.../ModulithBoundariesTest.java` — the ModuleApi→shared.exception
  carve-out was widened from `tariff` to `tariff, rental` (the new `RentalSigningFacadeImpl` in
  the rental root package throws the shared `ResourceNotFoundException`, same cross-cutting
  precedent as tariff).
- `component-test/.../features/rental/rental-signing-lifecycle.feature` — the strict
  `the rental response only contains` assertion always checks `plannedDurationMinutes`, so the
  expectation tables carry an explicit `plannedDuration` column.
