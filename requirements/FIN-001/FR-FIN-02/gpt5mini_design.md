# System Design: FR-FIN-02 - Customer Finance Account Creation

## 1. Architectural Overview

A new Finance component will own customer finance accounts and ledgering. To satisfy the atomicity requirement the
Customer component will create the customer's finance account within the same registration transaction boundary by
invoking a Finance component contract (synchronous, in-process call). The Finance component will provision a single
System Account at startup and persist customer accounts with two sub-ledgers (`CUSTOMER_WALLET`, `CUSTOMER_HOLD`) all
with fixed-point monetary precision. Audit entries are recorded for account creation.

## 2. Impacted Components

* **`customer` (Customer Module):**
    * Must invoke Finance to create a customer finance account as part of the registration flow.
    * Must surface errors from Finance back to the caller so registration can roll back when account creation fails.
* **`finance` (Finance Module):**
    * New component responsible for account provisioning, sub-ledger management, journal entries, and system account
      provisioning.
    * Provides idempotent create-account contract and audit recording.

## 3. Abstract Data Schema Changes

* **Entity: `CustomerFinanceAccount`**
    * **Attributes Added/Modified:** `accountId` (PK), `customerId` (FK to Customer), `createdAt`, `createdBy`.
* **Entity: `CustomerSubLedger`**
    * **Attributes:** `ledgerId` (PK), `accountId` (FK), `ledgerType` (ENUM: CUSTOMER_WALLET | CUSTOMER_HOLD),
      `balance` (fixed-point, precision 2), `updatedAt`.
* **Entity: `JournalEntry`**
    * **Attributes:** `entryId`, `timestamp`, `debitAccountRef`, `creditAccountRef`, `amount` (fixed-point 2),
      `transactionType` (DEPOSIT|WITHDRAWAL|RENTAL_CAPTURE|REFUND|ADJUSTMENT), `referenceId`, `description`,
      `createdBy`.
* **Entity: `SystemAccount`**
    * Single-row owner of system-ledgers: `CASH`, `CARD_TERMINAL`, `BANK_TRANSFER`, `REVENUE`, `ADJUSTMENT`.

Relations:

- One-to-one: `Customer` → `CustomerFinanceAccount` (enforced unique constraint on `customerId`).
- One-to-many: `CustomerFinanceAccount` → `CustomerSubLedger` (two required sub-ledgers per account).
- Many JournalEntry rows link `debitAccountRef`/`creditAccountRef` to either a `CustomerSubLedger` or a `SystemAccount`
  sub-ledger.

## 4. Component Contracts & Payloads

* **Interaction: `customer` -> `finance`**
    * **Protocol:** Synchronous in-process contract (facade) invoked within registration transaction.
    * **Payload (CreateAccount):** `{ customerId, initialBalances: { CUSTOMER_WALLET: 0.00, CUSTOMER_HOLD: 0.00 },
    correlationId }`
    * **Response:** `success { accountId }` or `error { code: DUPLICATE_ACCOUNT | VALIDATION_ERROR | INTERNAL_ERROR }`.
* **Interaction: `finance` -> `audit/log`**
    * **Protocol:** Internal audit/logging (append-only); emits `AccountCreated` event containing `customerId`,
      `accountId`, and `correlationId` for observability.

## 5. Updated Interaction Sequence

1. External client submits customer registration to `customer` component with registration payload.
2. `customer` validates input and begins registration transaction.
3. Before persisting the new Customer, `customer` calls `finance.CreateAccount(customerId, initialBalances)`.
4. `finance` checks for existing account; if none found:
    1. Creates `CustomerFinanceAccount` row.
    2. Creates two `CustomerSubLedger` rows with zero balances (`CUSTOMER_WALLET`, `CUSTOMER_HOLD`).
    3. Records an audit `AccountCreated` entry and returns success.
5. If `finance` returns success, `customer` persists the Customer entity and the transaction commits, making both
   Customer and Finance account durable.
6. Happy path: API returns registration success with linked account id.

Unhappy paths:

- If `finance` reports `DUPLICATE_ACCOUNT` → `customer` aborts registration and returns a conflict error.
- If `finance` fails with validation or internal error → entire transaction rolls back; no Customer nor Finance
  account is created.
- If `customer` persist fails after finance success (rare) — the synchronous transactional approach prevents commit in
  either side; both operations are rolled back by the transaction manager.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Finance records and account balances are accessible only via authorized staff APIs and the
  owning customer. All create-account calls are authenticated internally (component-to-component) and logged with a
  `correlationId` for audit.
* **Scale & Performance:** Account creation is low-cost and performed synchronously; ensure DB indices on
  `CustomerFinanceAccount.customerId` and uniqueness constraint. Use fixed-point decimal with `scale=2` for all money
  values and round at the service boundary.
* **Atomicity & Resilience:** The chosen synchronous-in-transaction contract provides the required atomicity. If the
  runtime later moves to an event-driven model, Finance must expose an idempotent account-creation endpoint and the
  Customer component must use an exactly-once / deduplicated event delivery or compensating transaction to ensure the
  same atomic outcome.
* **Auditability & Compliance:** All account creations and sub-ledger initializations write a `JournalEntry`-style
  audit record. Retain audit entries immutably and expose them to staff for dispute resolution.
* **Data Model Constraints:** Monetary columns must use fixed-point numeric with precision for cents (e.g.,
  `NUMERIC(19,2)`).
