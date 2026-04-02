# User Story: FR-FIN-06 - Rental Hold (Pre-Authorization)

## 1. Description

**As the** Rental module
**I want to** request Finance to freeze the planned rental cost in the customer's account at the time a rental is
created
**So that** the customer cannot spend those funds while the rental is active, and the shop is guaranteed payment

## 2. Context & Business Rules

* **Trigger:** The Rental module requests a hold when a new rental is being created and rented equipments are being
  selected.
* **Rules Enforced:**
    * The planned cost is calculated by the Tariff module based on the tariff rate and the expected rental duration; the
      Finance module consumes this value and must not re-calculate it.
    * The hold amount must be greater than zero.
    * The entire planned cost must be available as non-held balance in `CUSTOMER_WALLET` for the hold to succeed —
      partial holds are not permitted.
    * If available balance is sufficient, one double-entry journal is created: `CUSTOMER_WALLET` (debit) →
      `CUSTOMER_HOLD` (credit) for the planned cost amount.
    * If available balance is insufficient, the hold is rejected with an "insufficient funds" error and no journal entry
      is created; the rental creation is aborted.
    * Transaction type: `HOLD`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The hold check and journal creation must complete within a single transaction in under 1 second to
  avoid rental creation latency.
* **Security/Compliance:** The hold operation must be atomic — there must be no state where funds are partially frozen.
  All hold operations are logged as journal entries.
* **Usability/Other:** The error response for insufficient funds must be actionable (i.e., state how much is available
  vs. how much is required).

## 4. Acceptance Criteria (BDD)

**Scenario 1: Successful hold — sufficient funds available**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €100 and `CUSTOMER_HOLD` balance of €0
* **And** the Tariff module has computed a planned cost of €60 for the rental
* **When** the Rental module requests a hold for €60
* **Then** a journal entry is created: `CUSTOMER_WALLET` (debit €60) / `CUSTOMER_HOLD` (credit €60)
* **And** the customer's `CUSTOMER_WALLET` balance becomes €40
* **And** the customer's `CUSTOMER_HOLD` balance becomes €60
* **And** the hold is confirmed to the Rental module

**Scenario 2: Hold rejected — insufficient available funds**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €30 and `CUSTOMER_HOLD` balance of €0
* **And** the planned rental cost is €60
* **When** the Rental module requests a hold for €60
* **Then** the hold is rejected with an "insufficient funds" error
* **And** no journal entry is created
* **And** `CUSTOMER_WALLET` and `CUSTOMER_HOLD` balances are unchanged

**Scenario 3: Hold rejected — existing hold reduces available balance below required amount**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €50 and `CUSTOMER_HOLD` balance of €40
* **When** the Rental module requests a hold for €30
* **Then** the hold is rejected because available balance is €10 (€50 wallet − €40 on-hold), which is less than €30
* **And** balances are not modified

**Scenario 4: Hold rejected for zero amount**

* **Given** any customer with a valid finance account
* **When** the Rental module requests a hold of €0
* **Then** the operation is rejected with a validation error

## 5. Out of Scope

* Releasing the hold (covered by FR-FIN-07 and FR-FIN-08).
* Adjusting an existing hold amount after rental creation.
* Holds for non-rental purposes.
