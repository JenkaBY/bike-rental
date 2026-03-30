# System Design: FR-FIN-04 - Manual Balance Adjustment

## 1. Architectural Overview

FR-FIN-04 adds an administrator-driven balance correction operation to the `finance` bounded-context. The feature
builds directly on the `Account` / `SubLedger` aggregate and the `Transaction` / `TransactionRecord` journal
infrastructure established in FR-FIN-01 through FR-FIN-03. No new cross-module facade calls are required — the
adjustment is self-contained within the finance module.

The operation is handled as a standalone synchronous command: an admin submits a signed, reason-bearing adjustment
(positive for top-up, negative for deduction) against a specific customer's wallet. The application service
resolves both ledger participants — the customer's `CUSTOMER_WALLET` sub-ledger and the System Account's
`ADJUSTMENT` control sub-ledger — constructs a balanced two-legged journal entry, validates the deduction
floor constraint (wallet may not go below zero), updates both sub-ledger balances, and persists everything
atomically in a single database transaction. On a top-up the `ADJUSTMENT` sub-ledger is the debit side; on a
deduction it is the credit side. The `CUSTOMER_HOLD` sub-ledger is never touched by this operation.
Every adjustment is recorded with a mandatory, non-blank reason and the identity of the admin who initiated it,
forming an immutable audit trail.

---

## 2. Impacted Components

* **`AdjustmentCommandController` (new — finance module API):** HTTP entry point for manual balance adjustments
  under `POST /api/finance/adjustments`. Validates the inbound request DTO — non-null `customerId`, non-zero
  `amount`, non-blank `reason`, non-blank `operatorId` — via Bean Validation before delegating to
  `ApplyAdjustmentUseCase`. Returns `201 Created` with the transaction identifier and new wallet balance on
  success.

* **`ApplyAdjustmentUseCase` (new — finance module application use-case interface):** Port interface in
  `finance/application/usecase`. Declares a single `execute(ApplyAdjustmentCommand)` method that returns an
  `AdjustmentResult`. Command attributes: `customerId` (UUID), `amount` (signed `Money`, non-zero), `reason`
  (String, non-blank), `operatorId` (String, non-blank).

* **`ApplyAdjustmentService` (new — finance module application service):** Implements `ApplyAdjustmentUseCase`.
  Orchestrates the full adjustment flow within a single `@Transactional` boundary: resolve both sub-ledgers,
  validate the deduction floor constraint, mutate sub-ledger balances in-memory, construct the `Transaction`
  aggregate, and delegate persistence to `AccountRepository` and `TransactionRepository`.

* **`TransactionType` (existing — finance module domain enum — extended):** Add `ADJUSTMENT` to the existing
  set of values (`DEPOSIT`, …). No other types are affected.

* **`Transaction` (existing — finance module domain model — extended):** Add a nullable `reason` field
  (`String`) to the domain entity to carry the mandatory adjustment explanation. The field is populated
  exclusively by `ADJUSTMENT`-type transactions; all other transaction types leave it null. The domain model
  must enforce that `reason` is non-null and non-blank whenever `type == ADJUSTMENT`.

* **`AccountRepository` (existing — finance module domain port — unmodified):** The two existing methods —
  `findByCustomerId(UUID)` and `getSystemAccount()` — are reused without modification to resolve both
  participating accounts. The `save(Account)` operation (already present) flushes mutated sub-ledger balances.

* **`TransactionRepository` (existing — finance module domain port — unmodified):** The existing `save(Transaction)`
  method persists the new adjustment journal entry. No new query methods are needed for this story.

* **`Account` / `SubLedger` (existing — finance module domain model — unmodified):** The existing
  `getSubLedger(LedgerType)`, `credit(Money)`, and `debit(Money)` methods are reused without signature changes.
  `ApplyAdjustmentService` calls `getSubLedger(ADJUSTMENT)` on the System Account and
  `getSubLedger(CUSTOMER_WALLET)` on the Customer Account.

* **`CoreExceptionHandlerAdvice` (existing — shared — extended):** Must handle the new
  `InsufficientBalanceException` thrown when a deduction would reduce `CUSTOMER_WALLET` below zero.
  Maps to `422 Unprocessable Entity` with `errorCode: INSUFFICIENT_BALANCE`.

* **`PaymentMethod` (existing — finance module domain enum — extended):** Add `INTERNAL_TRANSFER` to the
  existing set of values. This value is used exclusively by `ADJUSTMENT`-type transactions to signal an
  internal ledger correction with no external payment instrument. Any existing exhaustive switch or MapStruct
  mapper over `PaymentMethod` must be updated to handle this new variant; the
  `mapstruct.unmappedTargetPolicy=ERROR` flag will produce a build failure until all mappers are aligned.

* **`bike-rental-db` (data store — schema extended):** The `finance_transactions` table gains a new nullable
  `reason` column. The `payment_method` column remains NOT NULL; no DDL change is required for that column.
  The `payment_method` enum type is extended with the `INTERNAL_TRANSFER` value to accommodate `ADJUSTMENT`
  transactions.

---

## 3. Abstract Data Schema Changes

* **Entity: `Transaction`**
    * **Attributes Added/Modified:**
        * `reason` (Varchar, nullable — non-null and non-blank for `ADJUSTMENT` type; null for all other types).
      * `payment_method` (Enum string — remains NOT NULL. The enum type is extended with the new
        `INTERNAL_TRANSFER` value, which is assigned to all `ADJUSTMENT` transactions. Existing `DEPOSIT`
        and `WITHDRAWAL` rows are unaffected; no data migration is required).
    * **Constraint note:** A check constraint (or application-layer invariant) must enforce:
      `IF transaction_type = 'ADJUSTMENT' THEN reason IS NOT NULL AND reason <> ''`.

* **Entity: `SubLedger`** (existing table `finance_sub_ledgers` — unmodified DDL)
    * `balance` is mutated in-place on each adjustment. No DDL change; the column already exists.

* **Relations:** No new relations. The existing one-to-many from `finance_transactions` to
  `finance_transaction_records` carries the two new adjustment legs exactly as it does for deposits.

---

## 4. Component Contracts & Payloads

* **Interaction: External HTTP client → `AdjustmentCommandController`**
    * **Protocol:** REST — `POST /api/finance/adjustments`
    * **Request Payload:**
        ```
        {
          customerId:  UUID    (not-null)
          amount:      Decimal (non-zero; positive = top-up, negative = deduction)
          reason:      String  (not-null, not-blank)
          operatorId:  String  (not-null, not-blank)
        }
        ```
    * **Success Response:** `201 Created`
        ```
        {
          transactionId:    UUID
          newWalletBalance: Decimal
          recordedAt:       Timestamp
        }
        ```
    * **Error Responses:**
        * `400 Bad Request` — validation failure (amount = 0, blank reason, missing field).
        * `404 Not Found` — customer finance account does not exist.
        * `422 Unprocessable Entity` — deduction would reduce wallet balance below zero
          (`errorCode: INSUFFICIENT_BALANCE`).

* **Interaction: `ApplyAdjustmentService` → `AccountRepository`**
    * **Protocol:** In-process synchronous call (within existing transaction)
    * **Payload Changes:** Two existing methods consumed without modification:
        * `findByCustomerId(UUID)` — resolves the Customer Account including `CUSTOMER_WALLET` sub-ledger.
        * `getSystemAccount()` — resolves the System Account including `ADJUSTMENT` sub-ledger.
    * `save(Account)` flushes both mutated sub-ledger balances in the same transaction.

* **Interaction: `ApplyAdjustmentService` → `TransactionRepository`**
    * **Protocol:** In-process synchronous call (within existing transaction)
    * **Payload Changes:** Existing `save(Transaction)` is called with a `Transaction` constructed as:
        * `type = ADJUSTMENT`
      * `paymentMethod = INTERNAL_TRANSFER`
        * `amount = |adjustmentAmount|` (absolute value stored; direction is captured in `TransactionRecord.direction`)
        * `reason = <admin-provided reason>`
        * `customerId`, `operatorId`, `recordedAt = now`
        * `sourceType = null`, `sourceId = null`
        * Two `TransactionRecord` children describing the balanced entry (see sequence below).

---

## 5. Updated Interaction Sequence

### Happy Path 1: Admin applies a top-up adjustment of +€10

1. Admin submits `POST /api/finance/adjustments` with `{ customerId, amount: +10, reason: "Compensation for
   system error", operatorId }`.
2. `AdjustmentCommandController` validates the request DTO (amount ≠ 0, reason non-blank, customerId
   non-null).
3. `AdjustmentCommandController` invokes `ApplyAdjustmentUseCase.execute(ApplyAdjustmentCommand)`.
4. `ApplyAdjustmentService` opens a `@Transactional` boundary.
5. `ApplyAdjustmentService` calls `AccountRepository.findByCustomerId(customerId)` — Customer Account
   resolved including `CUSTOMER_WALLET` sub-ledger.
6. `ApplyAdjustmentService` calls `AccountRepository.getSystemAccount()` — System Account resolved including
   `ADJUSTMENT` sub-ledger.
7. Amount is positive (top-up): no floor-balance check needed.
8. `ApplyAdjustmentService` mutates balances in-memory:
    * `adjustmentSubLedger.debit(10)` → `ADJUSTMENT.balance += 10`
    * `walletSubLedger.credit(10)` → `CUSTOMER_WALLET.balance += 10`
9. `ApplyAdjustmentService` constructs a `Transaction(type=ADJUSTMENT, paymentMethod=INTERNAL_TRANSFER, amount=10, reason=…,
   customerId, operatorId, sourceType=null, sourceId=null, recordedAt=now)` with two `TransactionRecord` children:
    * `TransactionRecord(subLedgerId=adjustmentSubLedger.id, ledgerType=ADJUSTMENT, direction=DEBIT, amount=10)`
    * `TransactionRecord(subLedgerId=walletSubLedger.id, ledgerType=CUSTOMER_WALLET, direction=CREDIT,
      amount=10)`
10. `AccountRepository.save(systemAccount)` flushes the mutated `ADJUSTMENT` sub-ledger balance.
11. `AccountRepository.save(customerAccount)` flushes the mutated `CUSTOMER_WALLET` sub-ledger balance.
12. `TransactionRepository.save(transaction)` inserts one row in `finance_transactions` and two rows in
    `finance_transaction_records`.
13. `@Transactional` boundary commits — all three writes land atomically.
14. `ApplyAdjustmentService` returns `AdjustmentResult { transactionId, newWalletBalance, recordedAt }`.
15. `AdjustmentCommandController` returns `201 Created` with the result.

### Happy Path 2: Admin applies a deduction of -€15 (sufficient balance)

Steps 1–7 are identical to Happy Path 1 with `amount = -15`.

7. Amount is negative (deduction): `ApplyAdjustmentService` checks
   `walletSubLedger.balance >= 15` (absolute value). Balance is €40 — check passes.
8. `ApplyAdjustmentService` mutates balances in-memory:
    * `walletSubLedger.debit(15)` → `CUSTOMER_WALLET.balance -= 15`
    * `adjustmentSubLedger.credit(15)` → `ADJUSTMENT.balance += 15`
9. `Transaction` constructed with two `TransactionRecord` children:
    * `TransactionRecord(subLedgerId=walletSubLedger.id, ledgerType=CUSTOMER_WALLET, direction=DEBIT, amount=15)`
    * `TransactionRecord(subLedgerId=adjustmentSubLedger.id, ledgerType=ADJUSTMENT, direction=CREDIT, amount=15)`

Steps 10–15 identical to Happy Path 1.

### Unhappy Path 1: Deduction rejected — insufficient wallet balance

1. Admin submits `POST /api/finance/adjustments` with `{ customerId, amount: -20, reason: "…", operatorId }`.
   2–6. `AdjustmentCommandController` validates; `ApplyAdjustmentService` resolves both accounts.
7. Amount is negative: `walletSubLedger.balance (€10) < 20` — constraint violated.
8. `ApplyAdjustmentService` throws `InsufficientBalanceException`.
9. `@Transactional` rolls back; no balance mutation or journal row is written.
10. `CoreExceptionHandlerAdvice` maps `InsufficientBalanceException` → `422 Unprocessable Entity` with
    `errorCode: INSUFFICIENT_BALANCE` and `correlationId` from MDC.

### Unhappy Path 2: Adjustment rejected — blank reason

1. Admin submits `POST /api/finance/adjustments` with `reason = ""`.
2. `AdjustmentCommandController` Bean Validation (`@NotBlank` on `reason`) rejects the request before
   reaching the service.
3. `CoreExceptionHandlerAdvice` returns `400 Bad Request` with `errorCode: CONSTRAINT_VIOLATION` and an
   `errors` array identifying the `reason` field.
4. No account lookup or persistence occurs.

### Unhappy Path 3: Customer finance account not found

1. Admin submits a valid request for a `customerId` with no registered finance account.
2. `ApplyAdjustmentService` calls `AccountRepository.findByCustomerId(customerId)` — result is empty.
3. `ApplyAdjustmentService` throws `ResourceNotFoundException` (`errorCode: RESOURCE_NOT_FOUND`).
4. `@Transactional` rolls back; no writes occur.
5. `CoreExceptionHandlerAdvice` maps the exception → `404 Not Found`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `POST /api/finance/adjustments` must be guarded by an `ADMIN` role check at the HTTP
  boundary once the `SecurityFilterChain` is activated (currently deferred per existing architecture notes).
  Until then, the endpoint is structurally open. Regardless of authentication state, every persisted
  `Transaction` record carries an immutable `operatorId` string (the authenticated identity of the admin who
  submitted the request) and a non-blank `reason`, providing a tamper-evident audit trail that satisfies the
  compliance requirement. The `CUSTOMER_HOLD` sub-ledger is never a participant in this operation — its
  isolation is enforced at the application service level.

* **Scale & Performance:** The entire operation — balance check, two sub-ledger balance mutations, and journal
  entry persistence — must execute within a single database transaction to guarantee atomicity. For deduction
  flows, the `CUSTOMER_WALLET` sub-ledger row must be read with a pessimistic lock (`SELECT FOR UPDATE`) to
  prevent a concurrent deposit or rental hold from racing against the balance check. The operation must reach
  the commit point within the NFR target of 2 seconds under normal load. No caching is involved; the balance
  read is always live to enforce the floor constraint correctly.
