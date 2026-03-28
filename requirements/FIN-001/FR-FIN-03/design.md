# System Design: FR-FIN-03 - Fund Deposit

## 1. Architectural Overview

FR-FIN-03 introduces the first externally-triggered financial write operation: a staff-initiated fund deposit that
increases a customer's wallet balance via a balanced double-entry journal entry. The feature layers a new transaction
recording capability on top of the Account / SubLedger infrastructure established in FR-FIN-01 and FR-FIN-02.

Two new domain entities — `Transaction` (the journal header) and `TransactionRecord` (an individual debit or credit line)
— are added to the `finance` module. A new `DepositCommandController` exposes a single POST endpoint through which
staff record deposits. The application service resolves both ledger participants (the System Account's
payment-method sub-ledger on the debit side and the customer`s `CUSTOMER_WALLET` on the credit side), constructs
the balanced journal, updates both sub-ledger balances, and persists everything in a single database transaction.
The `AccountRepository` (already in place) is the sole cross-entity lookup; no new cross-module facades are required.
Authentication enforcement is structurally reserved at the HTTP boundary, pending the activation of the
security layer noted in the current architecture.

---

## 2. Impacted Components

* **`DepositCommandController` (new — finance module API):** HTTP entry point for deposit operations under
  `POST /api/finance/deposits`. Validates the inbound request DTO (non-null `customerId`, positive `amount`,
  non-null `paymentMethod`), maps it to the use-case command, and delegates to `RecordDepositUseCase`. Returns
  `201 Created` with the transaction identifier on success.

* **`RecordDepositUseCase` (new — finance module application use-case interface):** Port interface in
  `finance/application/usecase`. Declares a single `execute(RecordDepositCommand)` operation that returns a
  `DepositResult`. Accepts `customerId`, `amount`, and `paymentMethod` as the command attributes. The command
  also carries an `idempotencyKey` (client-provided UUID) to guarantee exactly-once semantics for repeated
  submissions.

* **`RecordDepositService` (new — finance module application service):** Implements `RecordDepositUseCase`.
  Orchestrates the full deposit flow: account lookup, balance mutation, journal construction, and persistence.
  Runs within a single `@Transactional` boundary to guarantee atomicity across the account balance updates and
  the journal row insertions.

* **`AccountRepository` (existing finance domain port — unmodified by this story):** Used to resolve both
  participating Account aggregates: `findByCustomerId(UUID)` for the customer's account and `getSystemAccount()`
  for the System Account. Both methods are already defined as of FR-FIN-01 and FR-FIN-02.

* **`Account` / `SubLedger` (existing finance domain model — extended):** `SubLedger` gains two balance-mutation
  methods — `credit(Money)` and `debit(Money)` — that update the in-memory balance value. `Account` gains a
  convenience method `getSubLedger(LedgerType)` that resolves a named sub-ledger or throws
  `ResourceNotFoundException` when the requested ledger type is absent on the account.

  Important domain changes:
  - Monetary values across the finance domain now use the shared `Money` value object (scale=2, HALF_UP rounding)
    instead of raw `BigDecimal` primitives.
  - The `credit(Money)` and `debit(Money)` domain methods return an id-less payload (`TransactionRecordWithoutId`)
    describing the mutation (sub-ledger ref, ledger type, direction, amount). The application service assigns
    UUIDs to those payloads when building persistent `TransactionRecord` instances before calling the repository.

* **`Transaction` (new — finance module domain model):** Journal header entity carrying `id` (UUID), `type`
  (`DEPOSIT`), `paymentMethod` (`CASH | CARD_TERMINAL | BANK_TRANSFER`), `amount` (domain `Money`), `customerId`, `operatorId`
  (the identifier of the staff member who recorded the operation), `sourceType` (nullable — discriminates the
  originating business context, e.g. `RENTAL`), `sourceId` (nullable UUID — the ID of the linked business
  entity such as a rental; absent for standalone counter operations like deposits), `recordedAt` (timestamp), and
  `idempotencyKey` (domain `IdempotencyKey`) used to deduplicate client retries. Owns a list of exactly two
  immutable `TransactionRecord` children formed at construction time.

* **`TransactionSourceType` (new — finance module domain enum):** Discriminates the originating business
  context of a transaction. Initial value: `RENTAL`. Designed for extension (e.g. `SUBSCRIPTION`,
  `ADJUSTMENT`) as new source entities are introduced. Used together with the nullable `sourceId` field on
  `Transaction`.

* **`TransactionRecord` (new — finance module domain value object):**** Represents a single debit or credit leg of a
  journal entry. Carries `subLedgerId` (UUID), `ledgerType` (for audit readability), `direction` (`DEBIT | CREDIT`),
  and `amount`.

* **`EntryDirection` (new — finance module domain enum):** Discriminates `DEBIT` from `CREDIT`.

* **`TransactionType` (new — finance module domain enum):** Initial values: `DEPOSIT`. Designed for extension with
  `WITHDRAWAL`, `HOLD`, `CAPTURE`, `RELEASE`, and `ADJUSTMENT` in subsequent stories.

* **`PaymentMethod` (existing — extended and relocated):** Currently declared at the finance module root
  (`finance/PaymentMethod.java`) with values `CASH`, `CARD`, `ELECTRONIC`. Must be relocated to
  `finance/domain/model/` to comply with hexagonal architecture (domain enum, not a facade-level type). Two
  new values — `CARD_TERMINAL` and `BANK_TRANSFER` — must be added to align with the deposit payment methods
  mandated by this story. Existing callers referencing `CARD` or `ELECTRONIC` are unaffected; new deposit
  flows use the new constants exclusively.

* **`TransactionRepository` (new — finance module domain port):** Interface in `finance/domain/repository`.
  Declares `save(Transaction) : Transaction` and
  `findByIdempotencyKeyAndCustomerId(IdempotencyKey, UUID) : Optional<Transaction>` to
  support deduplication of repeated client submissions. The lookup is intentionally scoped to both key and
  `customerId` to prevent cross-customer data leaks: a lookup by key alone could return a transaction belonging
  to a different customer if a key were reused across customers, silently skipping the credit and disclosing
  an internal transaction identifier. Implementation note: the JPA adapter marks `save(...)`
  with `@Transactional(propagation = Propagation.MANDATORY)` so it must be invoked inside an existing service
  transaction that is already persisting related account balance mutations.

* **`TransactionRepositoryAdapter` (new — finance module infrastructure):** JPA adapter implementing
  `TransactionRepository`. Persists a `TransactionJpaEntity` with cascaded `TransactionRecordJpaEntity` children.
  Uses a `TransactionJpaMapper` (MapStruct) to convert between domain and JPA representations.

* **`TransactionJpaEntity` / `TransactionRecordJpaEntity` (new — finance module infrastructure):** JPA entities mapped
  to `finance_transactions` and `finance_transaction_records` tables respectively.
  `TransactionJpaEntity` owns a one-to-many cascade-all relationship to `TransactionRecordJpaEntity`.

  Mapping notes: `TransactionJpaEntity` includes an `idempotency_key` UUID column (unique). MapStruct mappers
  are used to convert domain ↔ JPA types. A dedicated `TransactionRecordMapper` handles `TransactionRecord`
  conversions, and shared helper mappers (`MoneyMapper`, `IdempotencyKeyMapper`) convert `Money` and
  `IdempotencyKey` to `BigDecimal`/`UUID` for persistence.

* **`bike-rental-db` (data store):** Receives two new DDL tables via Liquibase changesets:
  `finance_transactions` and `finance_transaction_records`.

---

## 3. Abstract Data Schema Changes

* **Entity: `Transaction`**
    * **New table:** `finance_transactions`
    * **Attributes:**
        * `id` (UUID, primary key)
        * `transaction_type` (Enum string: `DEPOSIT | ...`, not-null)
        * `payment_method` (Enum string: `CASH | CARD_TERMINAL | BANK_TRANSFER`, not-null)
        * `amount` (Decimal 19,2, not-null, check > 0)
      * `idempotency_key` (UUID, not-null) — client-provided key used to deduplicate repeated requests.
        * `customer_id` (UUID, not-null — references the customer identity for audit; no FK constraint to avoid
          cross-module coupling)
        * `operator_id` (Varchar, not-null — identifier of the staff member who recorded the transaction;
          stored as a plain string to avoid coupling to a future User/Auth module)
        * `source_type` (Enum string: `RENTAL | ...`, nullable — discriminates the linked business context;
          null for standalone operations such as counter deposits)
        * `source_id` (UUID, nullable — ID of the linked business entity, e.g. rental ID; null when
          `source_type` is null; no FK constraint to avoid cross-module coupling)
        * `recorded_at` (Timestamp with timezone, not-null)
  * **Constraints:**
      * Composite unique constraint on `(idempotency_key, customer_id)` — idempotency deduplication is
        scoped per customer. A key may be reused across different customers without conflict, but the same
        key submitted twice for the same customer is guaranteed to be idempotent. This prevents a
        cross-customer information leak where a lookup by key alone could return a transaction belonging to
        a different customer.
    * **Indices:**
        * Index on `customer_id` to support future audit queries by customer.
        * Index on `(source_type, source_id)` to support future lookups of all transactions for a given rental.

* **Entity: `TransactionRecord`**
    * **New table:** `finance_transaction_records`
    * **Attributes:**
        * `id` (UUID, primary key)
        * `transaction_id` (UUID, not-null, FK → `finance_transactions.id`)
        * `sub_ledger_id` (UUID, not-null, FK → `finance_sub_ledgers.id`)
        * `ledger_type` (Enum string — denormalized copy of `LedgerType` for audit readability, not-null)
        * `direction` (Enum string: `DEBIT | CREDIT`, not-null)
        * `amount` (Decimal 19,2, not-null, check > 0)
    * **Constraints:**
        * Foreign key `fk_transaction_records_transactions` (`transaction_id` → `finance_transactions.id`)
        * Foreign key `fk_transaction_records_sub_ledgers` (`sub_ledger_id` → `finance_sub_ledgers.id`)
        * Index on `transaction_id` for fast join resolution.
        * Index on `sub_ledger_id` to support future ledger-balance reconstruction queries.

* **Entity: `SubLedger`** (existing table `finance_sub_ledgers`)
    * **Attributes Modified:** `balance` is mutated in-place on each deposit. No DDL change; the column already
      exists and is type Decimal 19,2.

* **Relations:**
    * One-to-many: `finance_transactions` → `finance_transaction_records` (exactly two records per transaction; cascade
      all, orphan removal enforced at the aggregate boundary, not at the DB level).
    * Many-to-one: `finance_transaction_records` → `finance_sub_ledgers` (read-only FK; no cascade).

---

## 4. Component Contracts & Payloads

* **Interaction: External HTTP client → `DepositCommandController`**
    * **Protocol:** REST — `POST /api/finance/deposits`
    * **Request Payload:**
        ```
        {
          idempotencyKey: UUID       (not-null) ← client-generated to ensure exactly-once
          customerId:     UUID       (not-null)
          amount:         Decimal    (> 0)
          paymentMethod:  Enum       (CASH | CARD_TERMINAL | BANK_TRANSFER)
          operatorId:     String     (not-null, not-blank)
        }
        ```
    * **Success Response:** `201 Created`
        ```
        {
          transactionId: UUID
          recordedAt:    Timestamp
        }
        ```
    * **Error Responses:**
        * `400 Bad Request` — validation failure (amount ≤ 0, missing field, unknown payment method).
        * `404 Not Found` — customer finance account does not exist.

* **Interaction: `RecordDepositService` → `AccountRepository`**
    * **Protocol:** In-process synchronous call (same transaction)
    * **Payload Changes:** Two existing methods are consumed without modification:
        * `findByCustomerId(UUID customerId)` — resolves the customer's `Account` aggregate including all its
          `SubLedger` children.
        * `getSystemAccount()` — resolves the System Account aggregate including all its `SubLedger` children.
    * An `AccountPersistencePort` (or the same `AccountRepository`) must also support an `update(Account)` or
      `save(Account)` operation so mutated sub-ledger balances are flushed to `finance_sub_ledgers`. If
      `AccountRepository.save()` already provides upsert semantics via JPA merge, no new method signature is needed.

* **Interaction: `RecordDepositService` → `TransactionRepository`**
    * **Protocol:** In-process synchronous call (same transaction)
  * **Payload Changes:** New `save(Transaction)` method and new
    `findByIdempotencyKeyAndCustomerId(IdempotencyKey, UUID)` method replacing the plain `findByIdempotencyKey` lookup.
    Scoping the lookup to `(idempotencyKey, customerId)` ensures the idempotency short-circuit can never return a
    transaction that belongs to a different customer — a globally-unique key lookup would allow a key reused for a
    different customer to silently skip the credit and leak the original customer's transaction identifier. The
    `Transaction` object carries `type = DEPOSIT`,
      the chosen `paymentMethod`, `amount`, `customerId`, `operatorId`, `recordedAt`, `sourceType = null`,
      `sourceId = null` (a counter deposit has no linked business entity), and exactly two `TransactionRecord`
      children: one `DEBIT` on the System Account's payment-method sub-ledger and one `CREDIT` on the customer's
      `CUSTOMER_WALLET` sub-ledger.

---

## 5. Updated Interaction Sequence

### Happy Path: Staff records a cash deposit of €50

1. Staff submits `POST /api/finance/deposits` with `{ customerId, amount: 50, paymentMethod: CASH }`.
2. `DepositCommandController` validates the request DTO (amount > 0, paymentMethod non-null, customerId non-null).
3. `DepositCommandController` invokes `RecordDepositUseCase.execute(RecordDepositCommand)`.
4. `RecordDepositService` checks
   `TransactionRepository.findByIdempotencyKeyAndCustomerId(command.idempotencyKey(), command.customerId())` — if
  present, it returns the existing `transactionId` + `recordedAt` and short-circuits the rest of the flow.
   The lookup is scoped to both the key **and** the customer to prevent a cross-customer data leak: a globally-keyed
   lookup could return a transaction belonging to a different customer if the same key were reused across customers.
5. `RecordDepositService` opens a `@Transactional` boundary.
6. `RecordDepositService` calls `AccountRepository.findByCustomerId(customerId)` — Customer Account found,
   including `CUSTOMER_WALLET` sub-ledger.
7. `RecordDepositService` calls `AccountRepository.getSystemAccount()` — System Account found, including
   `CASH` sub-ledger.
8. `RecordDepositService` resolves the debit sub-ledger: `SystemAccount.getSubLedger(CASH)`.
9. `RecordDepositService` resolves the credit sub-ledger: `CustomerAccount.getSubLedger(CUSTOMER_WALLET)`.
9. `RecordDepositService` mutates balances in-memory:
    * `cashSubLedger.debit(50)` → `CASH.balance += 50`
    * `walletSubLedger.credit(50)` → `CUSTOMER_WALLET.balance += 50`
10. `RecordDepositService` constructs a `Transaction(type=DEPOSIT, paymentMethod=CASH, amount=50, customerId,
    operatorId, sourceType=null, sourceId=null, recordedAt=now)` with two `TransactionRecord` children:
    * `TransactionRecord(subLedgerId=cashSubLedger.id, ledgerType=CASH, direction=DEBIT, amount=50)`
    * `TransactionRecord(subLedgerId=walletSubLedger.id, ledgerType=CUSTOMER_WALLET, direction=CREDIT, amount=50)`
11. `AccountRepository.save(systemAccount)` flushes the mutated `CASH` sub-ledger balance to `finance_sub_ledgers`.
12. `AccountRepository.save(customerAccount)` flushes the mutated `CUSTOMER_WALLET` sub-ledger balance to
    `finance_sub_ledgers`.
13. `TransactionRepository.save(transaction)` inserts one row in `finance_transactions` and two rows in
    `finance_transaction_records`.
14. `@Transactional` boundary commits — all three writes land atomically.
15. `RecordDepositService` returns `DepositResult { transactionId, recordedAt }` to `DepositCommandController`.
16. `DepositCommandController` returns `201 Created` with the deposit result to the caller.

### Unhappy Path 1: Customer finance account not found

1. Staff submits `POST /api/finance/deposits` for a `customerId` with no registered finance account.
2. `RecordDepositService` calls `AccountRepository.findByCustomerId(customerId)` — result is empty.
3. `RecordDepositService` throws `ResourceNotFoundException` (errorCode: `RESOURCE_NOT_FOUND`).
4. `@Transactional` rolls back; no balance mutation or journal row is written.
5. `CoreExceptionHandlerAdvice` maps the exception to `404 Not Found` with `ProblemDetail`.

### Unhappy Path 2: Deposit amount is zero or negative

1. Staff submits `POST /api/finance/deposits` with `amount = 0` (or negative).
2. `DepositCommandController` constraint validation rejects the request before reaching the service.
3. `CoreExceptionHandlerAdvice` returns `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION` and an
   `errors` array identifying the `amount` field.
4. No account lookup or persistence occurs.

### Unhappy Path 3: No payment method selected

1. Staff submits `POST /api/finance/deposits` with `paymentMethod` absent or null.
2. `DepositCommandController` constraint validation rejects the request.
3. Response: `400 Bad Request` — same handling as Unhappy Path 2.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The `POST /api/finance/deposits` endpoint must be restricted to authenticated staff roles
  once the security layer is activated. The current architecture declares spring-boot-starter-security as a dependency
  but has no active `SecurityFilterChain`; all endpoints are open. The deposit endpoint must be listed as a
  staff-only secured path in the future `SecurityFilterChain` configuration to satisfy the compliance requirement
  that only authenticated staff record deposits. No structural code change is needed today beyond ensuring the
  endpoint URL follows a consistent namespace (`/api/finance/...`) that can be targeted by a single security
  rule pattern. The `operatorId` field is accepted from the request payload and persisted verbatim on every
  `Transaction` row, providing a complete operator audit trail without coupling to a User/Auth module. Once
  authentication is active, the resolved principal identity should be validated against or replace the
  submitted `operatorId`.

* **Scale & Performance:** The service completes in a single database transaction touching at most three rows in
  `finance_sub_ledgers` and two rows in `finance_transaction_records`. No caching, async queuing, or external I/O is
  involved, making the 2-second response-time NFR trivially achievable under normal PostgreSQL load. Optimistic
  locking (`@Version`) on `SubLedgerJpaEntity` should be introduced to prevent lost-update race conditions if
  two concurrent deposits for the same customer are possible, escalating to a `409 Conflict` on version mismatch
  rather than silently corrupting balances.
