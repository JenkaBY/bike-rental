# System Design: FR-FIN-02 - Customer Finance Account Creation

## 1. Architectural Overview

FR-FIN-02 introduces the automated creation of a Customer Finance Account as a direct consequence of successful
customer registration. The topology change spans two bounded-context modules: the `customer` module gains the
responsibility of publishing a `CustomerRegistered` domain event after persisting a new customer, and the `finance`
module gains a new event consumer that creates the corresponding Account and two sub-ledgers
(`CUSTOMER_WALLET`, `CUSTOMER_HOLD`) within the same database transaction.

The core architectural constraint is _transactional atomicity_: a customer must never exist in the system without an
accompanying finance account. This is satisfied by having the `finance` module listener execute its writes using
`@TransactionalEventListener(phase = BEFORE_COMMIT)`, which binds the account creation to the open database
transaction of the customer registration before that transaction is committed. A failure in either step rolls back
both, leaving the database in a consistent state.

No new HTTP endpoints, inter-service HTTP calls, or external message brokers are introduced. The `AccountRepository`
domain port (established in FR-FIN-01) is extended with a customer-oriented lookup to support the duplicate-prevention
check. The `LedgerType` discriminator set is extended to include the two customer-specific sub-ledger types.

---

## 2. Impacted Components

* **`CreateCustomerService` (customer application service — modified):** After successfully persisting the Customer
  aggregate, must publish a `CustomerRegistered` domain event via the shared `EventPublisher` port. This is the only
  change to the customer module.

* **`CustomerRegistered` (new — customer module domain event):** An immutable event record carrying the newly
  registered customer's identity (`customerId` as UUID). Implements `BikeRentalEvent`. Published inside the customer
  registration transaction boundary.

* **`FinanceCustomerEventListener` (new — finance module event consumer):** A `@TransactionalEventListener(phase =
  BEFORE_COMMIT)` component within the `finance` module that listens for `CustomerRegistered` events. Routes the
  event payload to `CreateCustomerAccountUseCase`. Running inside `BEFORE_COMMIT` ensures the account write shares
  the same transaction; any failure propagates as a rollback signal to the caller.

* **`CreateCustomerAccountUseCase` (new — finance module application use-case interface):** Port interface in
  `finance/application/usecase`. Declares a single `execute(UUID customerId)` operation.

* **`CreateCustomerAccountService` (new — finance module application service):** Implements
  `CreateCustomerAccountUseCase`. Checks the `AccountRepository` for an existing Customer Account tied to the given
  `customerId`; throws `ResourceConflictException` if one is found. Otherwise constructs an `Account` domain object
  of type `CUSTOMER` with the provided `customerId` and two `SubLedger` children (`CUSTOMER_WALLET` and
  `CUSTOMER_HOLD`, both at balance zero), then delegates to `AccountRepository.save()`.

* **`AccountRepository` (existing finance domain port — extended):** Gains one new query operation:
  `findByCustomerId(UUID customerId) : Optional<Account>`. Used exclusively by `CreateCustomerAccountService` for the
  duplicate-prevention guard.

* **`AccountRepositoryAdapter` (existing finance infrastructure — extended):** Implements the new
  `findByCustomerId(UUID)` operation by delegating to `AccountJpaRepository`.

* **`AccountJpaRepository` (existing Spring Data repository — extended):** Gains a derived query method
  `findByCustomerId(UUID customerId)` to back the new repository port operation.

* **`LedgerType` (existing finance domain enum — extended):** Two new constants added:
  `CUSTOMER_WALLET` (Liability — customer's available spendable balance) and `CUSTOMER_HOLD` (Liability — reserved /
  pre-authorized funds). These join the existing system-account constants (`CASH`, `CARD_TERMINAL`,
  `BANK_TRANSFER`, `REVENUE`, `ADJUSTMENT`).

* **`bike-rental-db` (data store):** No DDL changes required — `finance_accounts` and `finance_sub_ledgers` tables
  were established in FR-FIN-01. The new `LedgerType` values must be added to the enum column's check constraint (or
  allowed value set) if one was created in FR-FIN-01's migration; otherwise the column accepts any string and no
  migration is needed beyond documenting the new values.

---

## 3. Abstract Data Schema Changes

* **Entity: `Account`**
    * **Attributes Added/Modified:** None — table structure is unchanged. New rows of `account_type = 'CUSTOMER'` will
      be inserted at runtime with `customer_id` set to the registered customer's UUID.

* **Entity: `SubLedger`**
    * **Attributes Added/Modified:** None — table structure is unchanged. Each new Customer Account receives exactly
      two `SubLedger` rows: one with `ledger_type = 'CUSTOMER_WALLET'` and one with `ledger_type = 'CUSTOMER_HOLD'`,
      both with `balance = 0`.

* **Enum: `LedgerType`**
    * **Values Added:** `CUSTOMER_WALLET`, `CUSTOMER_HOLD`. If the `finance_sub_ledgers.ledger_type` column is backed
      by a database `CHECK` constraint enumerating allowed values, a Liquibase DDL changeset must extend that
      constraint to include the two new values.

* **Relations:** Unchanged. The existing one-to-many relationship between `finance_accounts` and
  `finance_sub_ledgers` naturally accommodates Customer Account rows.

* **Uniqueness:** The existing unique constraint `uq_finance_sub_ledgers_account_ledger` on `(account_id,
  ledger_type)` prevents duplicate sub-ledger types per account at the database level, providing an additional safety
  net for Scenario 3.

---

## 4. Component Contracts & Payloads

* **Interaction: `CreateCustomerService` → `SpringApplicationEventPublisher`**
    * **Protocol:** In-process Spring event publication (synchronous, transactional)
    * **Payload Changes:** New event `CustomerRegistered { customerId: UUID }` is published after the Customer
      aggregate is persisted.

* **Interaction: `SpringApplicationEventPublisher` → `FinanceCustomerEventListener`**
    * **Protocol:** In-process Spring `@TransactionalEventListener(phase = BEFORE_COMMIT)` dispatch
    * **Payload:** `CustomerRegistered { customerId: UUID }`
    * **Transactional Contract:** The listener runs within the open transaction of the customer registration. A
      `ResourceConflictException` thrown here causes the enclosing transaction to roll back, preventing both the
      customer row and any partial account row from being committed.

* **Interaction: `FinanceCustomerEventListener` → `CreateCustomerAccountUseCase`**
    * **Protocol:** In-process method call (synchronous, same transaction)
    * **Payload Changes:** Passes `customerId` (UUID) extracted from the event.

* **Interaction: `CreateCustomerAccountService` → `AccountRepository`**
    * **Protocol:** In-process JPA / SQL (same transaction)
    * **Reads:** `findByCustomerId(customerId)` — returns `Optional<Account>`. If present, throws
      `ResourceConflictException`.
    * **Writes:** `save(account)` — cascades to two `SubLedger` children.

---

## 5. Updated Interaction Sequence

### Happy Path: New customer registered → finance account created

1. External client sends `POST /api/customers` with valid customer data.
2. `CustomerCommandController` routes the request to `CreateCustomerUseCase`.
3. `CreateCustomerService` validates input and persists the new `Customer` aggregate to `bike-rental-db` within a
   database transaction.
4. `CreateCustomerService` publishes `CustomerRegistered { customerId }` via `SpringApplicationEventPublisher`.
5. Spring dispatches the event to `FinanceCustomerEventListener` in `BEFORE_COMMIT` phase (still within the same
   transaction).
6. `FinanceCustomerEventListener` calls `CreateCustomerAccountService.execute(customerId)`.
7. `CreateCustomerAccountService` calls `AccountRepository.findByCustomerId(customerId)` — result is empty; no
   duplicate.
8. `CreateCustomerAccountService` constructs an `Account(type=CUSTOMER, customerId)` with two `SubLedger` children:
   `(CUSTOMER_WALLET, balance=0)` and `(CUSTOMER_HOLD, balance=0)`.
9. `AccountRepository.save(account)` inserts one row in `finance_accounts` and two rows in `finance_sub_ledgers`
   within the open transaction.
10. The `BEFORE_COMMIT` phase completes successfully; the enclosing transaction commits, persisting all three rows
    together with the `Customer` row.
11. `CustomerCommandController` returns `201 Created` with the new customer's resource representation to the client.

### Unhappy Path 1: Customer registration fails validation

1. External client sends `POST /api/customers` with invalid data.
2. `CustomerCommandController` / `CreateCustomerService` raises a validation exception before any persistence.
3. No `CustomerRegistered` event is published; `FinanceCustomerEventListener` is never invoked.
4. No finance account is created. Response is `400 Bad Request`.

### Unhappy Path 2: Duplicate account creation attempt

1. A `CustomerRegistered` event arrives for a `customerId` that already has a Customer Finance Account (e.g., due to
   a replayed event).
2. `CreateCustomerAccountService` calls `AccountRepository.findByCustomerId(customerId)` — result is non-empty.
3. `CreateCustomerAccountService` throws `ResourceConflictException`.
4. The exception propagates through `FinanceCustomerEventListener` into the `BEFORE_COMMIT` phase, causing the
   enclosing transaction to roll back.
5. The calling registration (or replay attempt) is treated as a conflict. Existing account data is unchanged.

### Unhappy Path 3: Account persistence fails mid-write

1. Steps 1–8 complete successfully.
2. `AccountRepository.save(account)` raises a data-integrity exception (e.g., unique constraint violation on the
   account table).
3. The exception propagates through the listener, rolling back the entire transaction.
4. Neither the `Customer` row nor any `finance_accounts` / `finance_sub_ledgers` rows are committed.
5. `CustomerCommandController` returns a `409 Conflict` (if `ResourceConflictException`) or `500 Internal Server
   Error` (unexpected data error).

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Customer finance accounts are not exposed via any new HTTP endpoint in this story. All writes
  are performed internally by `CreateCustomerAccountService`, which is package-private within the `finance` module and
  not reachable from other modules except through the scoped event listener. `customerId` values are UUIDs generated
  by the `customer` module — no external input is accepted for account creation. Audit logging of account creation is
  satisfied by the existing `created_at` / `updated_at` columns on `finance_accounts` and `finance_sub_ledgers`,
  plus Spring's standard INFO-level transaction logging.

* **Atomicity & Consistency:** Transactional atomicity is enforced by binding the finance-side write to
  `TransactionPhase.BEFORE_COMMIT`. This is the strongest in-process atomicity guarantee available without a
  distributed transaction: both the customer row and the account rows land in the same database commit or neither
  does. The application-level duplicate check in `CreateCustomerAccountService` combined with the database-level
  unique constraint on `(account_id, ledger_type)` provides defence-in-depth against duplicate sub-ledger creation.

* **Scale & Performance:** Each customer registration triggers exactly three additional INSERT statements (one account
  row + two sub-ledger rows), all within the already-open registration transaction. There is no additional round-trip,
  no messaging broker, and no asynchronous queue. At the anticipated scale of a single-shop rental system this
  overhead is imperceptible.
