# User Story: FR-FIN-14 - Integrate holdFunds into Rental Creation

## 1. Description

**As the** Rental module
**I want to** atomically lock the customer's planned rental cost at the moment a rental is created
**So that** the funds check and the rental record are inseparable — a rental that exists always has funds reserved,
and a funds reservation always corresponds to a real rental

## 2. Context & Business Rules

* **Trigger:** Staff submits a `POST /api/rentals` request with a fully specified rental (customer, equipment list,
  duration). After the Tariff module computes the planned cost per equipment item, the Rental module calls
  `FinanceFacade.holdFunds()` before persisting the rental record.
* **Rules Enforced:**
    * `holdFunds` is called within the same database transaction as the rental `save()`. If the hold fails, the entire
      rental creation rolls back — no partial state is persisted.
    * The `customerRef` is derived from `rental.getCustomerId()` and the `rentalRef` wraps the rental's `Long` id.
    * The `totalPlannedCost` passed to `holdFunds` is the sum of `estimatedCost` across all `RentalEquipment` items.
    * If `holdFunds` throws `InsufficientBalanceException`, the Rental module translates it to an
      `INSUFFICIENT_FUNDS` error response (`422 Unprocessable Entity`) including the available balance and the
      required amount.
    * The `totalPlannedCost` must be greater than zero; creation with a zero-cost rental is rejected.
    * `FinanceFacade` gains a query method `hasHold(Long rentalId)` that returns `true` when at least one `HOLD`
      transaction exists for the given rental reference.
    * `UpdateRentalService.startRental()` replaces the `hasPrepayment(rentalId)` guard with `hasHold(rentalId)`.
      If `hasHold` returns `false`, a `HoldRequiredException` is thrown and the activation is rejected with `409`.
    * The old `PrepaymentRequiredException` remains in place only during the transition period until FR-FIN-15 is
      applied.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The entire creation path (cost computation + hold + save) must complete within a single
  transaction in under 2 seconds.
* **Security/Compliance:** The hold result (transaction reference, recorded timestamp) is stored on the Rental
  aggregate or logged so the audit trail links the rental to the hold journal entry.
* **Usability/Other:** The insufficient-funds error response must state the available wallet balance and the
  required amount so staff can immediately inform the customer.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Rental created and funds held — sufficient balance**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €100
* **And** a rental request for one item with a planned cost of €60
* **When** `POST /api/rentals` is submitted
* **Then** the rental is persisted in `DRAFT` status
* **And** a `HOLD` journal entry is created: `CUSTOMER_WALLET` (debit €60) / `CUSTOMER_HOLD` (credit €60)
* **And** the customer's available balance is now €40
* **And** the response is `201 Created` with the rental payload

**Scenario 2: Rental creation rejected — insufficient balance**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €30
* **And** a rental request for one item with a planned cost of €60
* **When** `POST /api/rentals` is submitted
* **Then** the response is `422 Unprocessable Entity` with `errorCode = INSUFFICIENT_FUNDS`
* **And** the error body includes `availableBalance = 30` and `requiredAmount = 60`
* **And** no rental record is persisted
* **And** no journal entry is created

**Scenario 3: Activation succeeds when hold exists**

* **Given** a rental in `DRAFT` status with a corresponding `HOLD` journal entry
* **When** `PATCH /api/rentals/{id}` with `status = ACTIVE` is submitted
* **Then** `hasHold(rentalId)` returns `true`
* **And** the rental transitions to `ACTIVE` status

**Scenario 4: Activation rejected when hold is missing**

* **Given** a rental in `DRAFT` status with no `HOLD` journal entry (e.g., legacy record)
* **When** `PATCH /api/rentals/{id}` with `status = ACTIVE` is submitted
* **Then** `hasHold(rentalId)` returns `false`
* **And** the response is `409 Conflict` with `errorCode = HOLD_REQUIRED`

**Scenario 5: Rental with multiple equipment items — total cost held**

* **Given** a customer with a `CUSTOMER_WALLET` balance of €150
* **And** a rental request for two items with planned costs of €60 and €50 respectively
* **When** `POST /api/rentals` is submitted
* **Then** `holdFunds` is called with `totalPlannedCost = 110`
* **And** the customer's available balance is €40

## 5. Out of Scope

* Releasing or adjusting the hold after creation (covered by FR-FIN-07 and FR-FIN-08).
* Editing an existing rental's equipment list or duration after creation (hold recalculation on update is a
  separate story).
* Removal of the deprecated `hasPrepayment`/`recordPrepayment` APIs (covered by FR-FIN-15).
* Any UI changes.
