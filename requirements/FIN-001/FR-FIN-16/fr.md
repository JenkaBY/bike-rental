# User Story: FR-FIN-16 — Cross-Customer Transaction Listing

## 1. Description

**As a** back-office administrator
**I want to** list business transactions across any set of customers, filtered by date range, source and
affected ledger types, with each transaction's full double-entry breakdown
**So that** I can audit both customer-side and system-side money movement in one place.

## 2. Context & Business Rules

* **Trigger:** `GET /api/finance/transactions` with optional query parameters.
* **Rules Enforced:**
  * Every transaction in `finance_transactions` carries both a customer leg and a system leg in
    `finance_transaction_records`; there is no customer-less transaction. "System transactions" are surfaced by
    exposing **all** double-entry legs of each transaction (`entries`), including `CASH`, `CARD_TERMINAL`,
    `BANK_TRANSFER`, `REVENUE`, `ADJUSTMENT`.
  * Filters combine with AND: `customerIds` (any-of), `fromDate`/`toDate` (recorded-at range), `sourceId`,
    `sourceType`, `ledgerTypes` (transaction touched any of the given ledgers).
  * `fromDate` is inclusive from start-of-day; `toDate` is inclusive to end-of-day — both resolved in the configured
    business time zone (identical semantics to the per-customer history endpoint).
  * Each transaction appears **exactly once** regardless of how many of its legs match the filter.
  * Default sort is `recordedAt` descending; `amount` and `type` are also sortable.
  * An empty result is a valid `200` with an empty page — never a `404`.
  * The existing per-customer endpoint `GET /api/finance/customers/{customerId}/transactions` is unchanged.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Backed by an index on `finance_transactions(recorded_at)` and on
  `finance_transaction_records(ledger_type)` for the ledger-type existence check.
* **Security/Compliance:** Authorization is out of scope for this FR; the endpoint follows the repo default
  (`permitAll`). Restricting it to administrators is tracked separately.
* **Usability/Other:** Response is the project's standard `Page<T>` wrapper; list values (`customerIds`,
  `ledgerTypes`) accept both repeated and comma-separated forms.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Unfiltered listing returns all customers' transactions with all legs (component test)**
* **Given** transactions exist for several customers
* **When** the client GETs `/api/finance/transactions`
* **Then** every transaction is returned once, newest first, each carrying its customer and system legs.

**Scenario 2: Filter by a list of customers (component test)**
* **Given** transactions for customers A and B
* **When** the client filters `customerIds=A,B`
* **Then** only those customers' transactions are returned.

**Scenario 3: Filter by system ledger type (component test)**
* **When** the client filters `ledgerTypes=REVENUE`
* **Then** only transactions with a `REVENUE` leg are returned.

**Scenario 4: A HOLD with two customer legs is listed once (component test)**
* **Given** a `HOLD` transaction with both a `CUSTOMER_WALLET` and a `CUSTOMER_HOLD` leg
* **When** the client lists that customer's transactions
* **Then** the transaction appears exactly once and `totalItems` counts it once.

**Scenario 5: Empty result (component test)**
* **When** the client filters by a customer with no transactions
* **Then** the response is `200` with `totalItems = 0`.

**Scenario 6: Invalid request parameters (WebMvc test)**
* **When** `customerIds`, `sourceType` or `ledgerTypes` contain an unparseable value
* **Then** the response is `400 ProblemDetail`.

## 5. Out of Scope

* Running balances and per-bucket deltas (served by the future transaction-details endpoint).
* Authorization/role restriction.
* Any change to the per-customer history endpoint or to how transactions are written.
