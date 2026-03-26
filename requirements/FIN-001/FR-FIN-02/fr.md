# User Story: FR-FIN-02 - Customer Finance Account Creation

## 1. Description

**As a** system
**I want to** automatically create a finance account for every new customer upon registration
**So that** the customer can immediately make deposits and participate in rental transactions

## 2. Context & Business Rules

* **Trigger:** A new customer is registered in the Customer module.
* **Rules Enforced:**
    * Every registered customer must have exactly one customer finance account.
    * The account must contain two sub-ledgers: `CUSTOMER_WALLET` (Liability) and `CUSTOMER_HOLD` (Liability).
    * Both sub-ledgers must start at a balance of zero.
    * Account creation must be atomic with respect to the triggering registration event — a customer must never exist
      without a finance account.
    * A customer finance account is always linked to exactly one customer identity.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Account creation must complete within the same transaction or event-driven boundary as customer
  registration, adding no user-perceptible latency.
* **Security/Compliance:** Customer finance account data must be accessible only to authorized staff and the account
  owner. Account creation must be logged for audit purposes.
* **Usability/Other:** N/A — this is a system-automated operation with no direct user interaction.

## 4. Acceptance Criteria (BDD)

**Scenario 1: New customer registration creates a finance account**

* **Given** a customer does not yet have a finance account
* **When** the customer is successfully registered in the system
* **Then** a customer finance account is created and linked to that customer
* **And** a `CUSTOMER_WALLET` sub-ledger is created with balance zero
* **And** a `CUSTOMER_HOLD` sub-ledger is created with balance zero

**Scenario 2: Finance account is not created on failed registration**

* **Given** a customer registration attempt fails (e.g., validation error)
* **When** the registration is rejected
* **Then** no customer finance account is created

**Scenario 3: Duplicate account creation is prevented**

* **Given** a customer already has a finance account
* **When** an attempt is made to create a second finance account for the same customer
* **Then** the operation is rejected with an error
* **And** the existing account remains unchanged

## 5. Out of Scope

* Manual creation of customer finance accounts via an admin API.
* Adding sub-ledger types beyond `CUSTOMER_WALLET` and `CUSTOMER_HOLD` for existing customers.
* Closing or archiving a customer finance account.
