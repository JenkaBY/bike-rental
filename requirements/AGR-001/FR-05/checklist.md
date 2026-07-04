# Implementation Checklist: FR-05 — Customer Signs the Rental Agreement

- [x] `task-001-liquibase-agreement-signatures-table.md`
- [x] `task-002-register-changelog-master.md`
- [x] `task-003-signing-snapshot-record.md`
- [x] `task-004-agreement-signature-aggregate.md`
- [x] `task-005-agreement-signature-summary-record.md`
- [x] `task-006-domain-exceptions.md`
- [x] `task-007-agreement-signature-repository-port.md`
- [x] `task-008-agreement-signed-event.md`
- [x] `task-009-content-hasher-byte-array-overload.md`
- [x] `task-010-usecase-interfaces.md`
- [x] `task-011-signing-assembly-mapper.md`
- [x] `task-012-sign-agreement-service.md`
- [x] `task-013-signature-query-service.md`
- [x] `task-014-jpa-entity.md`
- [x] `task-015-jpa-repository.md`
- [x] `task-016-jpa-mapper.md`
- [x] `task-017-repository-adapter.md`
- [x] `task-018-web-dtos.md`
- [x] `task-019-web-mapper.md`
- [x] `task-020-command-controller.md`
- [x] `task-021-query-controller.md`
- [x] `task-022-rest-controller-advice.md`
- [x] `task-023-docs-error-codes.md`
- [x] `task-024-dbsteps-truncate-list.md`
- [x] `task-025-sign-agreement-request-transformer.md`
- [x] `task-026-agreement-signing-steps.md`
- [x] `task-027-signature-summary-response-transformer.md`
- [x] `task-028-component-test-feature.md`
- [x] `task-029-webmvc-command-controller-test.md`
- [x] `task-030-webmvc-query-controller-test.md`
- [x] `task-031-full-verification.md`

**Next Task:** none — FR-05 implemented and verified (`:service:test` and `:component-test:test` green).

## Deviations from the task files (discovered during implementation / review)

- The duplicate-signature check (`agreement.signing.already_signed`) runs BEFORE the rental snapshot
  fetch: a signed rental is already ACTIVE, so the snapshot would otherwise mask the more specific
  already-signed error with `rental_not_awaiting_signature`.
- `AgreementSignatureJpaRepository.findSummaryByRentalId` uses an interface projection
  (`AgreementSignatureSummaryProjection`) mapped in the adapter instead of a JPQL constructor
  expression targeting the domain record (persistence must not construct domain read models —
  same review rule applied to `AgreementTemplateJpaRepository.findAllSummaries`).
- `SigningAssemblyMapper.toPdfData/toSigningSnapshot` are `default` methods constructing the
  records (MapStruct cannot write dotted nested targets into immutable records).
- The shared binary-content step in `WebRequestSteps` builds request headers locally instead of
  mutating `ScenarioContext` (a persisted `Accept: application/pdf` broke subsequent JSON calls).

## Review-feedback refactors shipped in this FR (apply to FR-02..FR-04 code)

- Preview endpoint moved to `POST /api/agreements/preview` (no "pdf" in the URL; format via
  `Accept: application/pdf`). ⚠️ Frontend contract change.
- `PdfBoxAgreementRenderer` configured via `app.agreement.pdf` `@ConfigurationProperties`
  (`AgreementPdfProperties`, defaults via `@DefaultValue`); TTF bytes cached at construction.
- `AgreementPdfData.RentalData` and `SigningSnapshot.Rental` carry `estimatedTotal`,
  `discountPercent`, `specialPrice`; `RentalSigningSnapshot` exposes discount/special price and
  the mapper reuses shared `MoneyMapper`/`DiscountMapper` (ArchUnit ignore extended).
- Preview fixtures extracted to `AgreementPdfFixtureProvider`.
- Template summaries read via `AgreementTemplateSummaryProjection`.
