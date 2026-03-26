# User Story: FR-FIN-01 - System Account Provisioning

## 1. Description

**As a** system operator
**I want to** have a System Account with all required sub-ledgers automatically provisioned at application startup
**So that** every financial transaction has a valid system-side account to record against from day one

## 2. Context & Business Rules

* **Trigger:** Application startup sequence.
* **Rules Enforced:**
    * Exactly one System Account must exist at all times; provisioning is idempotent — re-running startup must not
      create a duplicate.
    * The System Account must own five sub-ledgers upon provisioning: `CASH` (Asset), `CARD_TERMINAL` (Asset),
      `BANK_TRANSFER` (Asset), `REVENUE` (Income), and `ADJUSTMENT` (Control).
    * The System Account has no customer owner and must never be linked to any customer identity.
    * All sub-ledger balances start at zero.
    * If the System Account already exists, startup must complete without error and without modifying existing data.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Provisioning must complete within the normal Spring context startup time; no user-visible latency
  impact.
* **Security/Compliance:** The System Account and its sub-ledgers must not be accessible via any customer-facing API.
  All provisioning activity must be logged for audit purposes.
* **Usability/Other:** Provisioning failure must prevent application startup with a clear error message to the operator.

## 4. Acceptance Criteria (BDD)

**Scenario 1: First-time startup — System Account is created**

* **Given** no System Account exists in the database
* **When** the application starts
* **Then** exactly one System Account is created
* **And** it owns sub-ledgers: `CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`, `REVENUE`, and `ADJUSTMENT`
* **And** all sub-ledger balances equal zero

**Scenario 2: Subsequent startup — idempotent provisioning**

* **Given** a System Account with all sub-ledgers already exists
* **When** the application starts again
* **Then** no duplicate System Account or sub-ledger is created
* **And** existing balances are unchanged
* **And** the application starts successfully

**Scenario 3: Provisioning failure halts startup**

* **Given** the database is unavailable during provisioning
* **When** the application tries to start
* **Then** the application fails to start with a descriptive error message

## 5. Out of Scope

* Manual creation or modification of the System Account via an API.
* Multiple system accounts (e.g., per-branch or per-region accounts).
* Adding or removing sub-ledger types at runtime.

## Implementation changes made

The following changes were implemented in the codebase to satisfy FR-FIN-01 and related integration/maintenance
improvements. Each entry lists the modified/added file and a short rationale.

- `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedger.java`: removed `createdAt` and
  `updatedAt` audit fields from the domain value object so domain remains pure and immutable; auditing is handled at the
  JPA/entity layer.

- `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Account.java`: removed `createdAt` from the
  domain aggregate and replaced raw `UUID customerId` with a typed `CustomerRef` wrapper to improve domain
  expressiveness.

- `service/src/main/java/com/github/jenkaby/bikerental/shared/domain/CustomerRef.java`: added a small `record` wrapper
  for customer identity. It validates non-null IDs (throws `IllegalArgumentException` for null) and is annotated with
  `@NotNull` to express the invariant.

- `service/src/main/java/com/github/jenkaby/bikerental/shared/mapper/CustomerRefMapper.java`: added a MapStruct mapper (
  component model `spring`) to convert between `UUID` and `CustomerRef`. Default methods handle nulls safely.

- `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/mapper/AccountJpaMapper.java`:
  updated to `uses = CustomerRefMapper.class` and added explicit `@Mapping` rules to map `customerId <-> customerRef`.
  Also adjusted mappings to ignore audit fields when mapping domain→entity so JPA auditing can populate them.

- `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/AccountJpaEntity.java`:
  added `@EntityListeners(AuditingEntityListener.class)` and annotated `createdAt` with `@CreatedDate` so the
  persistence layer populates creation time automatically.

-

`service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/SubLedgerJpaEntity.java`:
added `@EntityListeners(AuditingEntityListener.class)` and annotated `createdAt` / `updatedAt` with `@CreatedDate` /
`@LastModifiedDate` so timestamps are maintained by JPA.

- `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java`: enabled JPA auditing by adding
  `@EnableJpaAuditing` to the application configuration.

-

`service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`:
added `@Transactional(readOnly = true)` at class level and `@Transactional` on the `save(...)` method so read operations
are optimized as read-only transactions while save remains writable.

Notes & follow-ups:

- MapStruct: mappers were updated to ignore audit fields when mapping domain→entity. Because MapStruct is configured
  with `unmappedTargetPolicy=ERROR`, compile-time mapping fixes were applied so annotation processing succeeds.
- Tests: any tests that previously constructed domain objects including audit fields or expecting `UUID customerId` were
  updated to use `CustomerRef` or to construct domain objects without audit timestamps. Run `:service:test` to validate.
- Database/provisioning: Liquibase changelogs and provisioning CSVs retain `created_at`/`updated_at` columns. JPA
  auditing will populate those values on insert/update so provisioning data remains compatible.

Verification steps after changes:

1. Compile the service module so MapStruct generates mappers:

```powershell
.\gradlew.bat :service:compileJava "-Dspring.profiles.active=test"
```

2. Run unit tests for the `service` module if desired:

```powershell
.\gradlew.bat :service:test "-Dspring.profiles.active=test"
```

If you want, I can update any remaining tests or DTOs that still reference the removed audit fields or the previous
`customerId` field.
