# System Design: FR-FIN-05 - Fund Withdrawal

## 1. Architectural Overview

FR-FIN-05 introduces the staff-initiated withdrawal flow: the reverse of the deposit established in FR-FIN-03. A
customer's available (non-held) `CUSTOMER_WALLET` balance is paid out via a balanced double-entry journal entry that
debits `CUSTOMER_WALLET` and credits the appropriate System Account payment-method sub-ledger (`CASH`,
`CARD_TERMINAL`, or `BANK_TRANSFER`).

The flow reuses the full persistence infrastructure introduced in FR-FIN-03 (`Transaction`, `TransactionRecord`,
`TransactionRepository`, the `finance_transactions` / `finance_transaction_records` tables, and the composite unique
index on `(idempotency_key, customer_id)`). Idempotency is guaranteed by the same mechanism as the deposit: the client
provides a `idempotencyKey` (UUID v7) in the request body; the service checks for an existing `Transaction` by that
key and `customerId` before processing; a duplicate hit returns the original result without creating additional
journal rows. A new `WithdrawalCommandController` mirrors `DepositCommandController`; a new
`RecordWithdrawalService` mirrors `RecordDepositService` with an added available-balance guard.

---

## 2. Impacted Components

* **`WithdrawalCommandController` (new — finance module API):** HTTP entry point for withdrawal operations under
  `POST /api/finance/withdrawals`. Validates the inbound request DTO (non-null `idempotencyKey`, non-null
  `customerId`, positive `amount`, non-null `payoutMethod`, non-blank `operatorId`), maps it to the use-case
  command, and delegates to `RecordWithdrawalUseCase`. Returns `201 Created` with `transactionId` and
  `recordedAt` on success.

* **`RecordWithdrawalUseCase` (new — finance module application use-case interface):** Port interface in
  `finance/application/usecase`. Declares a single `execute(RecordWithdrawalCommand)` operation returning a
  `WithdrawalResult`. Command attributes: `customerId` (UUID), `amount` (Money), `payoutMethod` (PaymentMethod),
  `operatorId` (String), `idempotencyKey` (IdempotencyKey).

* **`RecordWithdrawalService` (new — finance module application service):** Implements `RecordWithdrawalUseCase`.
  Runs within a single `@Transactional` boundary. Execution order:
    1. Look up `TransactionRepository.findByIdempotencyKeyAndCustomerId(key, customerRef)` — if found, return
       stored result immediately (no balance mutation).
    2. Load the customer `Account` via `AccountRepository.findByCustomerId(customerRef)`.
    3. Load the System Account via `AccountRepository.getSystemAccount()`.
    4. Compute **available balance** = `CUSTOMER_WALLET.balance − CUSTOMER_HOLD.balance`; reject with a domain
       exception when `amount > availableBalance`.
    5. Debit `CUSTOMER_WALLET` sub-ledger by `amount`; credit the payout sub-ledger (`CASH | CARD_TERMINAL |
     BANK_TRANSFER`) by `amount`.
    6. Persist both mutated `Account` aggregates via `AccountRepository.save(...)`.
    7. Construct and persist a `Transaction` (type `WITHDRAWAL`, with two `TransactionRecord` children) via
       `TransactionRepository.save(...)`.
    8. Return a `WithdrawalResult(transactionId, recordedAt)`.

* **`AccountRepository` (existing — no interface changes):** `findByCustomerId` and `getSystemAccount` are
  already defined. No new methods required.

* **`Account` / `SubLedger` (existing — no model changes):** `credit(Money)` and `debit(Money)` added in
  FR-FIN-03 are reused. `getSubLedger(LedgerType)` is used to resolve `CUSTOMER_WALLET`, `CUSTOMER_HOLD`, and
  the payout sub-ledger. No new domain methods required.

* **`TransactionRepository` (existing — no interface changes):**
  `findByIdempotencyKeyAndCustomerId(IdempotencyKey, CustomerRef)` already exists. No new methods required.

* **`TransactionType` (existing domain enum — extended):** The value `WITHDRAWAL` must be added alongside the
  existing `DEPOSIT`. All persistence mapping and switch expressions over `TransactionType` must handle the new
  value.

* **`PaymentMethodLedgerTypeMapper` (existing — reused without changes):** Already maps `CASH`,
  `CARD_TERMINAL`, and `BANK_TRANSFER` to their corresponding `LedgerType` values. The withdrawal flow
  resolves the **credit** sub-ledger with the same mapper.

* **`bike-rental-db` (data store — no new DDL required):** The `finance_transactions` and
  `finance_transaction_records` tables introduced in FR-FIN-03 are fully reused. The composite unique
  constraint on `(idempotency_key, customer_id)` already guarantees idempotency for both deposits and
  withdrawals without additional schema changes.

---

## 3. Abstract Data Schema Changes

No new tables or columns are required. The following change applies to an existing entity:

* **Entity: `Transaction`** (table `finance_transactions`)
    * **Attributes Modified:** `transaction_type` column gains the new enum value `WITHDRAWAL`. All existing
      records remain `DEPOSIT`; the column type and size are unchanged.

* **Relations:** Unchanged — the one-to-many relationship between `finance_transactions` and
  `finance_transaction_records` is reused as-is.

---

## 4. Component Contracts & Payloads

* **Interaction: External HTTP client → `WithdrawalCommandController`**
    * **Protocol:** REST — `POST /api/finance/withdrawals`
    * **Request Payload:**
      ```
      {
        idempotencyKey: UUID       (not-null) ← client-generated UUID v7; ensures exactly-once
        customerId:     UUID       (not-null)
        amount:         Decimal    (> 0, scale ≤ 2)
        payoutMethod:   Enum       (CASH | CARD_TERMINAL | BANK_TRANSFER)
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
        * `400 Bad Request` — validation failure (amount ≤ 0, missing field, unknown payout method, missing
          `idempotencyKey`).
        * `404 Not Found` — customer finance account does not exist.
        * `422 Unprocessable Entity` — insufficient available balance (amount exceeds
          `CUSTOMER_WALLET − CUSTOMER_HOLD`).

* **Interaction: `RecordWithdrawalService` → `TransactionRepository`**
    * **Protocol:** In-process synchronous call (same `@Transactional` context)
    * **Idempotency check:** `findByIdempotencyKeyAndCustomerId(idempotencyKey, customerRef)` executed first.
      Hit → return existing `WithdrawalResult` without balance mutation. Miss → proceed with full processing.

* **Interaction: `RecordWithdrawalService` → `AccountRepository`**
    * **Protocol:** In-process synchronous call (same `@Transactional` context)
    * **Payload:** Identical to the deposit flow. `findByCustomerId(customerRef)` for the customer account;
      `getSystemAccount()` for the System Account. Both are saved after balance mutations.

---

## 5. Updated Interaction Sequence

**Happy path — first submission:**

1. `External Client` sends `POST /api/finance/withdrawals` with
   `{idempotencyKey, customerId, amount, payoutMethod, operatorId}`.
2. `WithdrawalCommandController` validates the request DTO; on failure returns `400`.
3. `WithdrawalCommandController` maps the DTO to `RecordWithdrawalCommand` and calls
   `RecordWithdrawalUseCase.execute(command)`.
4. `RecordWithdrawalService` calls `TransactionRepository.findByIdempotencyKeyAndCustomerId(key, customerRef)` → empty.
5. `RecordWithdrawalService` loads the customer `Account` via `AccountRepository.findByCustomerId(customerRef)` → not
   found → `404`.
6. `RecordWithdrawalService` loads the `System Account` via `AccountRepository.getSystemAccount()`.
7. `RecordWithdrawalService` computes `availableBalance = CUSTOMER_WALLET.balance − CUSTOMER_HOLD.balance`.
8. If `amount > availableBalance` → throw insufficient-balance domain exception → `422`.
9. `CUSTOMER_WALLET` sub-ledger is debited by `amount`; payout sub-ledger is credited by `amount` (both in memory).
10. `AccountRepository.save(customerAccount)` and `AccountRepository.save(systemAccount)` persist the balance mutations.
11. A `Transaction(type=WITHDRAWAL, …)` with two `TransactionRecord` children is constructed and persisted via
    `TransactionRepository.save(transaction)`.
12. `RecordWithdrawalService` returns `WithdrawalResult(transactionId, recordedAt)`.
13. `WithdrawalCommandController` maps the result to `TransactionResponse` and returns `201 Created`.

**Happy path — duplicate submission (idempotent retry):**

1–4. Same as above.

5. `TransactionRepository.findByIdempotencyKeyAndCustomerId` → existing `Transaction` found.
6. `RecordWithdrawalService` returns `WithdrawalResult(t.getId(), t.getRecordedAt())` immediately — no balance
   mutation, no new journal rows.
7. `WithdrawalCommandController` returns `201 Created` with the original `transactionId` and `recordedAt`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `operatorId` (the authenticated staff identifier) is persisted in the `Transaction`
  row for audit traceability. Authentication enforcement at the HTTP boundary is reserved for the security
  activation sprint (consistent with the architecture's current state, per FR-FIN-03 precedent).

* **Scale & Performance:** The idempotency lookup is a primary-key–indexed read on `finance_transactions` by
  the composite unique index `(idempotency_key, customer_id)` — effectively a single B-tree lookup. No
  additional locking or caching strategy is required for the expected counter-based throughput. The full
  critical path remains within the 2-second performance budget (reads two `Account` rows + inserts one
  `Transaction` + two `TransactionRecord` rows within a single transaction).

* **Concurrency & Atomicity:** Concurrent duplicate submissions are handled by the DB's unique constraint on
  `(idempotency_key, customer_id)`. The second concurrent insert fails with a constraint violation; the caller
  retries the idempotency check and reads the first writer's committed row. `SubLedgerJpaEntity` uses
  `@Version`-based optimistic locking (inherited from FR-FIN-03); concurrent balance mutations on the same
  sub-ledger that are not protected by the idempotency key will surface an `OptimisticLockingFailureException`
  which callers may retry.

* **Audit:** Each withdrawal is permanently stored as a `Transaction` row (type `WITHDRAWAL`) linked to two
  `TransactionRecord` rows (the debit and credit legs). `operatorId`, `customerId`, `payoutMethod`, `amount`,
  and `recordedAt` are all persisted, satisfying the audit-querying requirement stated in the NFR.
