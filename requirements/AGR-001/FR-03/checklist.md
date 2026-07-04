# Implementation Checklist: FR-03 — Agreement Template Management (Admin)

- [x] `task-001-agreement-module-package-info.md`
- [x] `task-002-liquibase-agreement-templates-table.md`
- [x] `task-003-register-changelog-master.md`
- [x] `task-004-agreement-template-status-enum.md`
- [x] `task-005-agreement-domain-exceptions.md`
- [x] `task-006-agreement-template-summary-record.md`
- [x] `task-007-agreement-template-aggregate.md`
- [x] `task-008-agreement-template-repository-port.md`
- [x] `task-009-content-hasher.md`
- [x] `task-010-agreement-usecase-interfaces.md`
- [x] `task-011-create-update-delete-services.md`
- [x] `task-012-query-services.md`
- [x] `task-013-activate-agreement-template-service.md`
- [x] `task-014-web-dtos.md`
- [x] `task-015-web-mapper.md`
- [x] `task-016-command-controller.md`
- [x] `task-017-query-controller.md`
- [x] `task-018-rest-controller-advice.md`
- [x] `task-019-jpa-entity.md`
- [x] `task-020-jpa-repository.md`
- [x] `task-021-jpa-mapper.md`
- [x] `task-022-repository-adapter.md`
- [x] `task-023-docs-error-codes.md`
- [x] `task-024-component-test-seeding-infra.md`
- [x] `task-025-component-test-feature.md`
- [x] `task-026-webmvc-command-controller-test.md`
- [x] `task-027-webmvc-query-controller-test.md`

**Next Task:** none — FR-03 implemented and verified (`:service:test` and `:component-test:test` green).

## Ad-hoc fixes discovered during the test stage (outside the original tasks)

- `component-test/.../steps/common/hook/DbSteps.java` — `agreement_templates` added to the
  between-scenario cleanup table list (seeded rows with explicit ids collided with rows left by
  previous scenarios).
