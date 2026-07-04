# Implementation Checklist: FR-04 — Agreement PDF Renderer & Admin Preview

- [x] `task-001-add-pdfbox-version-catalog.md`
- [x] `task-002-service-build-pdfbox-dependency.md`
- [x] `task-003-agreement-pdf-data-record.md`
- [x] `task-004-agreement-pdf-renderer-port.md`
- [x] `task-005-agreement-pdf-rendering-exception.md`
- [x] `task-006-advice-pdf-rendering-handler.md`
- [x] `task-007-pdfbox-agreement-renderer.md`
- [x] `task-008-preview-usecase-interface.md`
- [x] `task-009-preview-agreement-pdf-service.md`
- [x] `task-010-preview-request-dto.md`
- [x] `task-011-preview-endpoint-controller.md`
- [x] `task-012-docs-error-code-catalogue.md`
- [x] `task-013-webmvc-preview-endpoint-tests.md`
- [x] `task-014-component-test-build-pdfbox.md`
- [x] `task-015-component-test-pdf-steps.md`
- [x] `task-016-component-test-feature.md`

**Next Task:** none — FR-04 implemented and verified (`:service:test` and `:component-test:test` green).

## Ad-hoc fixes discovered during the test stage (outside the original tasks)

- `component-test/.../steps/agreement/AgreementPdfSteps.java` — TestRestTemplate import corrected to
  Spring Boot 4 location `org.springframework.boot.resttestclient.TestRestTemplate`.
- `features/agreement/agreement-pdf-preview.feature` — pagination scenario content extended to 16
  paragraphs (the original 8 fit a single A4 page).
- `service/src/main/resources/fonts/DejaVuSans.ttf` — provisioned by the orchestrator (DejaVu 2.37 release).
