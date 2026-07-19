# User Story: FR-FIN-17 — Transaction Details

## 1. Description

**As a** back-office administrator
**I want to** retrieve a single transaction by its id, with the full double-entry breakdown, per-leg
signed movement and running balances, plus the customer-side deltas and resulting bucket balances
**So that** I can see and explain exactly where the money came from and went to, and what the
customer's balances were after the transaction.

## 2. Context & Business Rules

* **Trigger:** `GET /api/finance/transactions/{transactionId}`.
* **Rules Enforced:**
  * The response carries every double-entry leg of the transaction (`entries`), each with its
    `ledgerType`, `direction`, absolute `amount`, `signedDelta` (how that ledger's balance moved) and
    `balanceAfter` (the persisted running balance for that leg), plus a `systemLedger` flag.
  * `signedDelta` follows the ledger accounting convention: for **asset** ledgers (`CASH`,
    `CARD_TERMINAL`, `BANK_TRANSFER`) a `DEBIT` increases the balance and a `CREDIT` decreases it; for
    all other ledgers a `CREDIT` increases and a `DEBIT` decreases. So a deposit's cash leg shows a
    positive `signedDelta`, consistent with its `balanceAfter`.
  * Only the balance **after** the transaction is exposed, everywhere. The balance before is
    `balanceAfter − signedDelta` and is left to the client.
  * The customer block (`deltas{wallet, hold, external}`, `balances{wallet, hold}`) is identical in
    shape and semantics to the per-customer history endpoint, so the frontend reuses its rendering.
  * `balances` is always fully populated for both customer buckets. When the transaction does not touch
    a bucket (e.g. a deposit has no `CUSTOMER_HOLD` leg), that bucket's balance is seeded from the most
    recent prior balance rather than returned as null.
  * A missing transaction id is a `404`; a malformed id is a `400`.
  * No data from other modules is added (no customer name, no rental details) — identifiers only.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Single primary-key lookup with the legs fetched via an entity graph; the untouched
  bucket balance reuses the existing `findLatestLedgerBalancesBefore` query. No new index required.
* **Security/Compliance:** Authorization is out of scope for this FR; the endpoint follows the repo
  default (`permitAll`). Restricting it to administrators is tracked separately.
* **Usability/Other:** Response is a flat JSON object with nested `deltas`, `balances` and `entries`,
  fully described via OpenAPI `@Schema`.

## 4. Acceptance Criteria (BDD)

**Scenario 1: A deposit exposes both the customer and system legs (component test)**
* **Given** a `DEPOSIT` transaction with a `CUSTOMER_WALLET` credit and a `CASH` debit
* **When** the client GETs the transaction by id
* **Then** both legs are returned, the `CASH` leg shows a positive `signedDelta`, and `balances.hold`
  is present even though the deposit has no hold leg.

**Scenario 2: A hold moves money between customer buckets (component test)**
* **Given** a `HOLD` transaction with a `CUSTOMER_WALLET` debit and a `CUSTOMER_HOLD` credit
* **When** the client GETs the transaction
* **Then** `deltas` show wallet negative, hold positive, external zero, and both legs carry their
  running balances.

**Scenario 3: A capture releases the hold into revenue (component test)**
* **Given** a `CAPTURE` transaction with a `CUSTOMER_HOLD` debit and a `REVENUE` credit
* **When** the client GETs the transaction
* **Then** the wallet balance is carried forward from the prior balance (the transaction does not touch
  the wallet), and the revenue leg is flagged as a system ledger.

**Scenario 4: Unknown id returns 404 (component test)**
* **When** the client GETs a non-existent transaction id
* **Then** the response is `404 ProblemDetail` with `errorCode = shared.resource.not_found`.

**Scenario 5: Invalid id returns 400 (WebMvc test)**
* **When** the client GETs a malformed (non-UUID) id
* **Then** the response is `400 ProblemDetail`.

**Scenario 6: Signed delta respects the asset-ledger convention (unit test)**
* **Given** a leg on each ledger family and direction
* **Then** `signedBalanceDelta` returns a value whose sign matches the persisted running-balance
  convention.

## 5. Out of Scope

* The summary listing endpoint `GET /api/finance/transactions` and the per-customer history endpoint —
  both unchanged.
* Authorization/role restriction.
* Any cross-module enrichment (customer name, rental details).
* Exposing balance-before as a distinct field.
