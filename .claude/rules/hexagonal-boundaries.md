---
paths:
  - "service/src/main/java/**"
---

# Hexagonal & Module Boundaries — hard constraints

- **Spring Data repositories never construct domain types.** JPQL constructor expressions targeting domain
  records/read-models are forbidden. List/summary queries return an **interface projection** declared next to the
  repository (aliased `select ... as field`); the persistence **adapter** maps the projection to the domain read model
  via the module's JPA MapStruct mapper.
- **REST endpoints never encode the response format in the URL** (no `/preview-pdf`, `/{id}/pdf` suffixes). The format
  is selected by the `Accept` header via `produces` on separate handler methods sharing one path (see
  `RentalSignatureQueryController` — JSON list vs PDF attachment on the same GET).
- **Cross-module DTO assembly happens in a mapper, not in the facade.** A module-root facade impl delegates snapshot/DTO
  building to a package-private MapStruct mapper in the same package (see `RentalSigningSnapshotMapper`).
- **Module-root (API) classes have restricted shared access.** Per `ModulithBoundariesTest`, root-package classes may
  use `shared.domain.*` freely, but `shared.exception.*` / `shared.mapper.*` only for packages explicitly listed in the
  test's `ignoreDependency` carve-outs — extend the carve-out consciously in the same change, never by weakening the
  whole rule.
- **Report/document data models must be business-complete:** anything rendered for the customer (PDF data blocks,
  snapshots) carries the full pricing picture — per-unit costs, the computed total, and discount/special price when
  applicable — not just the raw line items.

Depth: `spring-boot-data-ddd`, `spring-boot-modulith` and `mapstruct-hexagonal` skills.
