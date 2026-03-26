# System Design: FR-FIN-01 - System Account Provisioning

## 1. Architectural Overview

FR-FIN-01 establishes the foundational financial infrastructure required by all subsequent double-entry accounting
operations. At application startup a dedicated provisioning runner interrogates the `finance` module's data store for
the existence of the singleton System Account. If absent, it creates the account together with its five mandated
sub-ledgers (`CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`, `REVENUE`, `ADJUSTMENT`), each starting at a zero balance. If
the System Account already exists the runner exits silently — making every startup attempt idempotent. A provisioning
failure surfaces as an unhandled exception that prevents the Spring context from completing startup, satisfying the
requirement that operators receive a clear error message.

The topology change is entirely internal to the `finance` module and the `bike-rental-db` data store. No new
inter-module contracts, HTTP endpoints, or external integrations are introduced. Two supporting entities (`Account` and
`SubLedger`) are added to the `finance` module's domain and persistence layers. The System Account and its five
sub-ledgers are provisioned as **Liquibase data changesets** executed atomically during database migration, before
the Spring application context finishes startup. This approach guarantees idempotency through Liquibase's own
`preConditions` mechanism and means a migration failure prevents startup — satisfying the operator-notification
requirement without any additional application code.

---

## 2. Impacted Components

* **`FinanceFacadeImpl` (finance module facade):** Remains the authoritative cross-module boundary; no change to
  existing public API contracts. Internal provisioning logic is deliberately excluded from the facade to prevent
  accidental access from other modules.

* **`RecordPaymentService` / `PaymentRepository` (existing finance components):** Unchanged. The new
  Account / SubLedger domain objects operate independently from the existing Payment aggregate.

* **`AccountRepository` (new — domain port):** Interface in `finance/domain/repository`. Declares `save` and
  `getSystemAccount()` operations. `getSystemAccount()` wraps an internal call to
  `findByAccountType("SYSTEM")` and throws `ResourceNotFoundException` if the row is absent — mirroring the
  `PaymentRepository.get(UUID)` contract. No other module may reference this port directly.

* **`finance_accounts-provisioning` / `finance_sub_ledgers-provisioning` (new — Liquibase data changesets):**
  Two new XML changesets in `service/src/main/resources/db/changelog/data/` that INSERT the singleton System Account
  row and its five sub-ledger rows with `balance = 0`. Each changeset is guarded by a `rowCount = 0` preCondition so
  re-running migrations never creates duplicates. Both changesets are excluded from the `test` context (matching the
  convention of existing data provisioning changesets) and are appended to `db.changelog-master.xml` after the
  corresponding DDL includes.

* **`AccountRepositoryAdapter` (new — infrastructure):** JPA adapter in `finance/infrastructure/persistence/adapter`
  that implements `AccountRepository` using `AccountJpaRepository` (Spring Data) and `AccountJpaMapper` (MapStruct).
  The `getSystemAccount()` implementation delegates to `AccountJpaRepository.findByAccountType("SYSTEM")` and
  throws `ResourceNotFoundException` when the result is empty.

* **`AccountJpaEntity` / `SubLedgerJpaEntity` (new — infrastructure):** JPA entities mapped to `finance_accounts`
  and `finance_sub_ledgers` tables. `AccountJpaEntity` owns a one-to-many cascade relationship to  
  `SubLedgerJpaEntity`.

* **`Account` / `SubLedger` (new — domain model):** Pure value-carrying domain objects in `finance/domain/model`.
  `Account` holds `accountType`, optional `customerId`, and a list of `SubLedger` children. `SubLedger` carries
  `ledgerType` and `balance`.

* **`AccountType` / `LedgerType` (new — domain enums):** `AccountType` discriminates `SYSTEM` from `CUSTOMER`.
  `LedgerType` enumerates `CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`, `REVENUE`, and `ADJUSTMENT`.

* **`bike-rental-db` (data store):** Receives two new DDL tables (`finance_accounts`, `finance_sub_ledgers`) and
  the corresponding seed rows, all applied via Liquibase changesets included in `db.changelog-master.xml`.

---

## 3. Abstract Data Schema Changes

* **Entity: `Account`**
    * **New table:** `finance_accounts`
    * **Attributes:**
        * `id` (UUID, primary key)
        * `account_type` (Enum string: `SYSTEM` | `CUSTOMER`, not-null)
        * `customer_id` (UUID, nullable — null for the System Account)
        * `created_at` (Timestamp with timezone, not-null)
    * **Constraints:** None — the table has at most one row per customer plus one SYSTEM row; sequential scan is always
      faster than an index at this cardinality.

* **Entity: `SubLedger`**
    * **New table:** `finance_sub_ledgers`
    * **Attributes:**
        * `id` (UUID, primary key)
        * `account_id` (UUID, not-null, FK → `finance_accounts.id`)
        * `ledger_type` (Enum string: `CASH` | `CARD_TERMINAL` | `BANK_TRANSFER` | `REVENUE` | `ADJUSTMENT`, not-null)
        * `balance` (Decimal 19,2, not-null, initial value `0`)
        * `created_at` (Timestamp with timezone, not-null)
        * `updated_at` (Timestamp with timezone, not-null)
    * **Constraints:**
        * Foreign key `fk_finance_sub_ledgers_finance_accounts` (`account_id` → `finance_accounts.id`)
        * Unique constraint `uq_finance_sub_ledgers_account_ledger` on `(account_id, ledger_type)` — prevents duplicate
          sub-ledger types per account
        * Index on `account_id` for fast join resolution

* **Relations:** One-to-many: `finance_accounts` → `finance_sub_ledgers` (cascade all, orphan removal). A single
  `SYSTEM` account always owns exactly five sub-ledger rows.

---

## 4. Component Contracts & Payloads

* **Interaction: Liquibase → `bike-rental-db` (provisioning)**
    * **Protocol:** SQL `INSERT` executed by Liquibase `loadData` during migration phase, before the Spring application
      context is fully started
    * **Payload — `finance_accounts-provisioning`:** Loads `data/finance_accounts.csv` into `finance_accounts`.
      The CSV carries a fixed UUID for the System Account, `account_type = 'SYSTEM'`, `customer_id = NULL`, and
      `created_at`. Guarded by a `sqlCheck preCondition` asserting `COUNT(*) = 0` for
      `account_type = 'SYSTEM'` before inserting.
    * **Payload — `finance_sub_ledgers-provisioning`:** Loads `data/finance_sub_ledgers.csv` into
      `finance_sub_ledgers`. The CSV carries five rows (one per `LedgerType`), each with a fixed UUID,
      the System Account UUID as `account_id`, `balance = 0`, and timestamps. Guarded by a `sqlCheck preCondition`
      asserting `COUNT(*) = 0` for rows belonging to the System Account UUID.

* **Interaction: `AccountRepositoryAdapter` → `AccountJpaRepository` (Spring Data)**
    * **Protocol:** SQL via Spring Data JPA / Hibernate
    * **Read — `getSystemAccount()`:** calls `findByAccountType("SYSTEM")` — returns `AccountJpaEntity`; throws
      `ResourceNotFoundException` if absent
    * **Write — `save(AccountJpaEntity)`:** Used by future customer-account creation stories; cascades to
      `SubLedgerJpaEntity` children

---

## 5. Updated Interaction Sequence

### Happy Path 1: First-time startup — System Account created

1. Liquibase applies `finance_accounts.create-table` and `finance_sub_ledgers.create-table` DDL changesets.
2. Liquibase evaluates `finance_accounts-provisioning`: `preConditions` checks `rowCount = 0` on the System Account
   row — condition is met, so the `INSERT` into `finance_accounts` executes (one row, `account_type = 'SYSTEM'`).
3. Liquibase evaluates `finance_sub_ledgers-provisioning`: `preConditions` checks `rowCount = 0` for sub-ledgers
   belonging to the System Account — condition is met, so five `INSERT` statements execute (one per `LedgerType`,
   all with `balance = 0`).
4. Liquibase marks all changesets as applied and commits; the Spring application context completes startup.

### Happy Path 2: Subsequent startup — idempotent

1. Liquibase evaluates `finance_accounts-provisioning`: `preConditions` checks `rowCount = 0` — condition is **not**
   met (row already exists), so the changeset is marked `MARK_RAN` and skipped.
2. Liquibase evaluates `finance_sub_ledgers-provisioning`: `preConditions` fails similarly — changeset skipped.
3. No writes occur; existing balances are untouched; application startup completes normally.

### Unhappy Path: Database unavailable during provisioning

1. Liquibase attempts to connect to the database during the migration phase.
2. The connection fails, throwing an exception inside the Spring Boot startup sequence.
3. Spring Boot treats the Liquibase failure as a fatal startup error; the application terminates with a descriptive
   error message logged at ERROR level — operator is notified.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The `Account` and `SubLedger` domain objects are not exposed via any web controller or
  cross-module Facade API in this story. The `AccountRepository` port and all implementing classes are
  package-private within the `finance` module. Spring Modulith's module boundary enforcement ensures no other module
  can import these types directly. Customer-facing APIs remain unchanged.

* **Idempotency:** Liquibase `preConditions` with `onFail="MARK_RAN"` guarantee that the provisioning INSERTs are
  skipped on every startup after the first. The unique constraint `uq_finance_sub_ledgers_account_ledger` on
  `(account_id, ledger_type)` provides a database-level safety net: even if `preConditions` were somehow bypassed, a
  duplicate insert would be rejected at the DB level rather than silently creating bad data.

* **Scale & Performance:** Provisioning executes at most six SQL statements during Liquibase's migration phase, which
  already runs before the application accepts traffic. There is no user-visible latency impact. No caching or
  asynchronous handling is required for this story.

* **Audit:** Liquibase logs each changeset execution at INFO level (including `MARK_RAN` skips), providing an
  auditable migration history in the `databasechangelog` table and in application startup logs.
