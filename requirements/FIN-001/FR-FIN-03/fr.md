# User Story: FR-FIN-03 - Fund Deposit

## 1. Description

**As a** staff member
**I want to** record a customer fund deposit at the counter
**So that** the customer's wallet balance is increased and the shop's corresponding asset account is updated

## 2. Context & Business Rules

* **Trigger:** Staff initiates a deposit for a customer at the counter.
* **Rules Enforced:**
    * Supported payment methods: `CASH`, `CARD_TERMINAL`, and `BANK_TRANSFER`.
    * Deposit amount must be greater than zero.
    * The deposit is recorded as a balanced double-entry journal:
        * `CASH` (debit) → `CUSTOMER_WALLET` (credit) for cash deposits.
        * `CARD_TERMINAL` (debit) → `CUSTOMER_WALLET` (credit) for card terminal deposits.
        * `BANK_TRANSFER` (debit) → `CUSTOMER_WALLET` (credit) for bank transfer deposits.
    * Transaction type is `DEPOSIT`; the payment method is stored as an attribute on the transaction.
    * Deposit, refund, and rental are always separate operations — a deposit cannot be combined with another operation
      type.
    * The customer's finance account must exist before a deposit can be recorded.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Deposit recording must complete and confirm to the staff within 2 seconds under normal load.
* **Security/Compliance:** Only authenticated staff members may record deposits. Each deposit is persisted as a journal
  entry and available for audit querying.
* **Usability/Other:** The payment method must be explicitly selected by staff; no default payment method is assumed.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Successful cash deposit**

* **Given** a customer with an existing finance account and a CASH system sub-ledger
* **When** staff records a cash deposit of €50 for the customer
* **Then** a journal entry is created: `CASH` (debit €50) / `CUSTOMER_WALLET` (credit €50)
* **And** the customer's `CUSTOMER_WALLET` balance increases by €50
* **And** the `CASH` system sub-ledger balance increases by €50
* **And** the transaction is stored with type `DEPOSIT` and payment method `CASH`

**Scenario 2: Successful card terminal deposit**

* **Given** a customer with an existing finance account
* **When** staff records a card terminal deposit of €100
* **Then** a journal entry is created: `CARD_TERMINAL` (debit €100) / `CUSTOMER_WALLET` (credit €100)
* **And** the transaction is stored with type `DEPOSIT` and payment method `CARD_TERMINAL`

**Scenario 3: Deposit rejected for unknown customer**

* **Given** a customer ID that has no registered finance account
* **When** staff attempts to record a deposit for that customer
* **Then** the operation is rejected with a "customer account not found" error
* **And** no journal entry is created

**Scenario 4: Deposit rejected for zero or negative amount**

* **Given** a valid customer account
* **When** staff attempts to record a deposit with an amount of zero or a negative value
* **Then** the operation is rejected with a validation error
* **And** no journal entry is created

## 5. Out of Scope

* Automated or scheduled deposits (e.g., direct-debit integrations).
* Combining a deposit with a refund or rental settlement in a single operation.
* Splitting a deposit across multiple payment methods.
