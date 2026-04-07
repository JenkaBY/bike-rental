# User Story: FR-FIN-12 - Automatic DEBT Settlement on Customer Deposit

## 1. Description

**As the** Rental module
**I want to** automatically attempt to settle outstanding DEBT rentals whenever a customer makes a fund deposit
**So that** debts are recovered without staff intervention as soon as the customer's wallet balance is sufficient

## 2. Context & Business Rules

* **Trigger:** The Finance module publishes a `CustomerFundDeposited` event after a successful deposit (extends
  FR-FIN-03). The event carries at minimum `customerId` and `transactionId`.
* **Rules Enforced:**
    * Upon receiving the event, the Rental module fetches all rentals in `DEBT` status for the depositing customer.
    * If no `DEBT` rentals exist, the event is consumed silently with no further action.
    * `DEBT` rentals are processed in order of creation — oldest first.
    * For each `DEBT` rental, the existing settle-rental flow (FR-FIN-07 / FR-FIN-08) is invoked unchanged.
    * Each settlement attempt must run in an isolated transaction so that a failure on one rental does not affect
      others.
    * On successful settlement, the rental transitions from `DEBT` to `COMPLETED`.
    * If the Finance module rejects settlement due to insufficient funds (`OverBudgetSettlementException`), the rental
      remains `DEBT` and processing continues to the next rental.
    * No new settlement logic is introduced in the Finance module; only the `CustomerFundDeposited` event publication
      is added to `RecordDepositService` (minimal extension of FR-FIN-03).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Processing is asynchronous and event-driven; no synchronous response-time requirement. The listener
  handles all DEBT rentals for that customer in a single invocation.
* **Security/Compliance:** Each settlement that succeeds produces auditable journal entries via the standard
  settle-rental flow. No additional audit trail is required for the event listener itself.
* **Reliability:** Because each settlement runs in an isolated transaction, a partial system failure leaves
  successfully settled rentals committed and un-settled rentals unchanged.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Deposit fully covers a single DEBT rental**

* **Given** a customer with one rental in `DEBT` status (final cost = €100, hold = €80)
* **And** the customer deposits €30 (wallet becomes €30, total available = €110 ≥ €100)
* **When** the `CustomerFundDeposited` event is processed
* **Then** the Finance settle-rental flow is invoked for the DEBT rental
* **And** the rental transitions to `COMPLETED`
* **And** the appropriate journal entries are persisted (CAPTURE for hold + CAPTURE for wallet gap)

**Scenario 2: Deposit covers the oldest DEBT rental but not the rest**

* **Given** a customer with two rentals in `DEBT` status (rental A created first: €50 gap; rental B: €200 gap)
* **And** the customer deposits €60 (wallet becomes €60, enough for rental A only)
* **When** the `CustomerFundDeposited` event is processed
* **Then** rental A is settled first and transitions to `COMPLETED`
* **And** rental B settlement raises `OverBudgetSettlementException` and is skipped
* **And** rental B remains in `DEBT` status

**Scenario 3: Deposit is insufficient for any DEBT rental**

* **Given** a customer with one rental in `DEBT` status (total gap exceeds available funds after deposit)
* **When** the `CustomerFundDeposited` event is processed
* **Then** settlement is attempted and rejected
* **And** the rental remains in `DEBT` status
* **And** no error is propagated to the caller

**Scenario 4: No DEBT rentals exist for the customer**

* **Given** a customer with no rentals in `DEBT` status
* **When** the `CustomerFundDeposited` event is processed
* **Then** no settlement is attempted
* **And** no state changes occur

## 5. Out of Scope

* Manual DEBT resolution by staff (covered by FR-FIN-04 if needed).
* Automated retry scheduling independent of deposit events.
* Customer notifications upon DEBT settlement.
* Partial write-off or adjustment of the shortfall amount.
