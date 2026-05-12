# User Story: FR-03 — ActivateRentalUseCase: Extract Activation from UpdateRentalService

## 1. Description

**As an** operator,
**I want to** activate a rental through a dedicated use case,
**So that** rental activation has a single, explicit entry point and the generic update service is
free of lifecycle concerns.

## 2. Context & Business Rules

* **Trigger:** The lifecycle endpoint routes an `ACTIVE` status request to `ActivateRentalUseCase`.
* **Pre-condition:** The rental must be in `DRAFT` status — enforced by the `RentalStatus`
  state machine (FR-01).
* **Invariant:** No financial hold may exist for a `DRAFT` rental. A hold is placed only at
  the moment of activation.
* **Activation steps:**
    1. Validate transition via state machine.
    2. If estimated cost > 0, call `FinanceFacade.holdFunds(customerRef, rentalRef,
       estimatedCost, operatorId)` to place a hold. If the finance module rejects the hold,
       abort and propagate the exception — the rental status remains `DRAFT`.
    3. Record `startedAt` as the current clock time (`LocalDateTime.now(clock)`).
    4. Call `rental.activate(startedAt)` which sets status to `ACTIVE`.
    5. Persist the rental.
    6. Publish `RentalStarted` event via `EventPublisher`.
* **UpdateRentalService changes:**
    - The `startRental()` private method is removed.
    - The `if (patch.containsKey("status"))` block is removed entirely — `UpdateRentalService` no
      longer processes any status field from the JSON Patch payload.
    - Attempts to pass `/status` in a JSON Patch request to `PATCH /api/rentals/{id}` will result
      in the field being silently ignored (or rejected, per the existing `RentalPatchRequestValidator`
      behaviour).
* **CreateRentalService changes (fast-path):** The `holdFunds` call inside
  `CreateRentalService.execute(CreateRentalCommand)` is removed. The fast-path service must
  delegate activation to `ActivateRentalUseCase` after saving the rental, so that hold placement
  is always performed by the same code path.
* **Default status at creation:** Rental creation defaults to `DRAFT` status (unchanged).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No additional external calls beyond what the extracted logic already performs.
* **Security/Compliance:** N/A.
* **Usability/Other:** `HoldRequiredException` is no longer used in the activation path; the
  finance module's own exception propagates if hold placement fails.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Activate a DRAFT rental with positive estimated cost**

* **Given** a rental in `DRAFT` status with `estimatedCost > 0`
* **When** `ActivateRentalUseCase.execute(rentalId)` is called
* **Then** `holdFunds` is called on `FinanceFacade`, the rental status becomes `ACTIVE`,
  `startedAt` is recorded, and a `RentalStarted` event is published

**Scenario 2: Activate a DRAFT rental with zero estimated cost**

* **Given** a rental in `DRAFT` status with `estimatedCost == 0`
* **When** `ActivateRentalUseCase.execute(rentalId)` is called
* **Then** no `holdFunds` call is made and the rental is activated

**Scenario 3: Hold placement fails — activation aborted**

* **Given** a rental in `DRAFT` status with `estimatedCost > 0` and `FinanceFacade.holdFunds`
  throws an exception (e.g., insufficient funds)
* **When** `ActivateRentalUseCase.execute(rentalId)` is called
* **Then** the exception propagates, the rental status remains `DRAFT`, and no `RentalStarted`
  event is published

**Scenario 4: Activation of a non-DRAFT rental is rejected by the state machine**

* **Given** a rental in `ACTIVE` status
* **When** `ActivateRentalUseCase.execute(rentalId)` is called
* **Then** `InvalidRentalStatusException` is thrown (from FR-01 state machine)

**Scenario 5: UpdateRentalService ignores status field in JSON Patch**

* **Given** a rental in `DRAFT` status
* **When** `PATCH /api/rentals/{id}` is called with `[{"op": "replace", "path": "/status", "value": "ACTIVE"}]`
* **Then** the status field is not processed (the rental status remains unchanged or a validation
  error is returned, depending on `RentalPatchRequestValidator` configuration)

**Scenario 6: RentalStarted event is published on activation**

* **Given** a rental in `DRAFT` status with a valid hold
* **When** `ActivateRentalUseCase.execute(rentalId)` is called successfully
* **Then** a `RentalStarted` event carrying the rental ID and equipment IDs is published

## 5. Out of Scope

* Changing the `RentalStarted` event schema.
* Modifying the `RentalEventListener` in the equipment module for ACTIVE transitions (already handled
  by the existing `RentalStarted` listener).
* Adding a new fast-path endpoint — `POST /api/rentals` continues to exist; only the internal
  delegation of hold placement moves to `ActivateRentalUseCase`.
