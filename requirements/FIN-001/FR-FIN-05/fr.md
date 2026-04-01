# User Story: FR-FIN-05 - Fund Withdrawal

## 1. Description

**As a** staff member
**I want to** record a cash or cashless payout to a customer at the counter
**So that** the customer receives their available balance and the shop's corresponding asset account is updated

## 2. Context & Business Rules

* **Trigger:** Staff initiates a withdrawal for a customer at the counter.
* **Rules Enforced:**
    * Supported payout methods: `CASH`, `CARD_TERMINAL`, and `BANK_TRANSFER`.
    * Withdrawal amount must be greater than zero.
    * Only available (non-held) balance may be withdrawn — the `CUSTOMER_HOLD` balance is excluded from the withdrawable
      amount.
    * Partial withdrawals are allowed: any positive amount up to and including the full available balance.
    * The withdrawal must not reduce `CUSTOMER_WALLET` below zero.
    * The withdrawal is recorded as a balanced double-entry journal:
        * `CUSTOMER_WALLET` (debit) → `CASH` (credit) for cash payouts.
        * `CUSTOMER_WALLET` (debit) → `CARD_TERMINAL` (credit) for card terminal payouts.
        * `CUSTOMER_WALLET` (debit) → `BANK_TRANSFER` (credit) for bank transfer payouts.
    * Transaction type is `WITHDRAWAL`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Withdrawal recording must complete and confirm to staff within 2 seconds under normal load.
* **Security/Compliance:** Only authenticated staff members may record withdrawals. Each withdrawal is persisted as a
  journal entry and available for audit querying.
* **Usability/Other:** Staff must explicitly select the payout method; no default is assumed.
* **Idempotency:** The withdrawal endpoint must be idempotent for staff-initiated requests. Clients must supply an
  `idempotencyKey` (persisted by the server) with the request. Repeated requests with the same `idempotencyKey`
  within the retention window MUST return the original successful response and MUST NOT create duplicate journal
  entries.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Successful partial cash withdrawal**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €80 and `CUSTOMER_HOLD` balance of €20
* **When** staff records a cash withdrawal of €30
* **Then** a journal entry is created: `CUSTOMER_WALLET` (debit €30) / `CASH` (credit €30)
* **And** the customer's `CUSTOMER_WALLET` balance becomes €50
* **And** the `CASH` system sub-ledger decreases by €30
* **And** the transaction is stored with type `WITHDRAWAL` and payout method `CASH`

**Scenario 2: Full available balance withdrawal**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €50 and `CUSTOMER_HOLD` balance of €30
* **When** staff records a withdrawal of €50 (the full available balance)
* **Then** the journal entry is created successfully
* **And** `CUSTOMER_WALLET` balance becomes zero

**Scenario 3: Withdrawal rejected when amount exceeds available balance**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €40 (with €20 on hold)
* **When** staff attempts to withdraw €50
* **Then** the operation is rejected with an "insufficient available balance" error
* **And** no journal entry is created

**Scenario 4: Held funds are excluded from withdrawable balance**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €10 and `CUSTOMER_HOLD` balance of €60
* **When** staff attempts to withdraw €20
* **Then** the operation is rejected because only €10 is available
* **And** the held €60 is not accessible for withdrawal

**Scenario 5: Withdrawal rejected for zero or negative amount**

* **Given** a valid customer account with a positive balance
* **When** staff attempts to record a withdrawal with an amount of zero
* **Then** the operation is rejected with a validation error

**Scenario 6: Duplicate submission is idempotent**

* **Given** the staff submits a withdrawal request with `idempotencyKey = "abc-123"` and it succeeds
* **When** the staff (or client) re-sends the same withdrawal request with the same `idempotencyKey = "abc-123"`
* **Then** the server returns the same successful response as for the original request (same status and body)
* **And** no additional journal entry is created (only one journal entry exists for that logical withdrawal)
* **And** the operation is auditable and traceable to the original request

## 5. Out of Scope

* Withdrawals paid from `CUSTOMER_HOLD` directly.
* Automated or scheduled withdrawals.
* Splitting a payout across multiple payment methods in a single operation.
