# User Story: FR-FIN-04 - Manual Balance Adjustment

## 1. Description

**As an** admin
**I want to** apply a positive or negative manual adjustment to a customer's wallet
**So that** I can correct balance discrepancies that have no corresponding real-world transaction

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Adjustment must persist and confirm within 2 seconds.
* **Security/Compliance:** Only admin-level users may perform adjustments. Every adjustment, including reason, operator
  ID, amount, and direction, must be persisted as an auditable journal entry.
* **Usability/Other:** The reason field is mandatory and must not be blank; the UI/API must enforce this before
  submission.

## 2. Context & Business Rules

* **Trigger:** An admin initiates a manual balance correction for a specific customer.
* **Rules Enforced:**
    * Amount must be non-zero (positive for top-up, negative for deduction).
    * A reason text is mandatory; empty or blank reasons are rejected.
    * The adjustment is recorded as a balanced double-entry journal:
        * Top-up: `ADJUSTMENT` (debit) → `CUSTOMER_WALLET` (credit).
        * Deduction: `CUSTOMER_WALLET` (debit) → `ADJUSTMENT` (credit).
    * A deduction must not reduce the customer's `CUSTOMER_WALLET` below zero; on-hold funds (`CUSTOMER_HOLD`) are
      unaffected.
    * Transaction type is `ADJUSTMENT`.
    * Adjustment is always a separate operation — it cannot be combined with a deposit, withdrawal, or rental
      settlement.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Successful top-up adjustment**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €40
* **When** an admin applies a +€10 adjustment with reason "Compensation for system error"
* **Then** a journal entry is created: `ADJUSTMENT` (debit €10) / `CUSTOMER_WALLET` (credit €10)
* **And** the customer's `CUSTOMER_WALLET` balance becomes €50
* **And** the transaction is stored with type `ADJUSTMENT` and the provided reason

**Scenario 2: Successful deduction adjustment**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €40 and no funds on hold
* **When** an admin applies a -€15 adjustment with reason "Overcharge correction"
* **Then** a journal entry is created: `CUSTOMER_WALLET` (debit €15) / `ADJUSTMENT` (credit €15)
* **And** the customer's `CUSTOMER_WALLET` balance becomes €25

**Scenario 3: Deduction rejected when wallet balance would go negative**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €10
* **When** an admin attempts a -€20 deduction
* **Then** the operation is rejected with an "insufficient balance" error
* **And** no journal entry is created

**Scenario 4: Adjustment rejected when reason is blank**

* **Given** a valid customer account
* **When** an admin submits an adjustment with an empty reason
* **Then** the operation is rejected with a validation error
* **And** no journal entry is created

## 5. Out of Scope

* Adjustments targeting `CUSTOMER_HOLD` directly.
* Bulk adjustments across multiple customers in a single operation.
* Admin approval workflows or four-eyes checks before an adjustment is applied.
