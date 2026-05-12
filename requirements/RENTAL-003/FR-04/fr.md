# User Story: FR-04 — CancelRentalUseCase + RentalCancelled Event

## 1. Description

**As an** operator,
**I want to** cancel a rental that is in `DRAFT` or `ACTIVE` status,
**So that** reserved or rented equipment is immediately freed and any hold placed during activation
is returned to the customer's account.

## 2. Context & Business Rules

* **Trigger:** The lifecycle endpoint routes a `CANCELLED` status request to `CancelRentalUseCase`.
* **Valid source statuses:** `DRAFT` and `ACTIVE` — enforced by the `RentalStatus` state machine
  (FR-01). Any other current status results in `InvalidRentalStatusException`.
* **Cancellation steps:**
    1. Load the rental by ID; throw `ResourceNotFoundException` if absent.
    2. Validate transition via state machine (FR-01).
    3. **If current status is `ACTIVE`:** the hold was placed during activation (FR-03). Return
       it to the customer's account by calling `FinanceFacade.releaseHold(rentalRef)`.
    4. **If current status is `DRAFT`:** no hold exists (holds are only placed on activation),
       so no financial side-effect.
    5. Set the status of **all `RentalEquipment` records** within the rental to `RETURNED`.
    6. Set rental status to `CANCELLED`.
    7. Persist the rental.
    8. Publish `RentalCancelled` event.
* **New `RentalCancelled` domain event:**
    - A Java `record` implementing `BikeRentalEvent`.
    - Carries at minimum: `rentalId` (Long), `customerId` (UUID), and `equipmentIds` (list of Long
      IDs of all equipment in the rental regardless of their `RentalEquipmentStatus`).
    - Located in `shared/domain/event/RentalCancelled.java` (shared module, consistent with
      `RentalStarted` and `RentalCompleted`).
* **`RentalEventListener` update (equipment module):**
    - Add a new `@ApplicationModuleListener` method that handles `RentalCancelled`.
    - On `RentalCancelled`: load the equipment records by `equipmentIds` and set each to `AVAILABLE`
      status (same pattern as the existing cancelled-draft branch in `onRentalUpdated`).
    - The existing `RentalUpdated`-based handling for draft-cancellation remains **unchanged** for
      backward compatibility during the transition period.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** `FinanceFacade.releaseHold` is called only when the prior status is `ACTIVE`.
  The equipment status update is handled asynchronously via the event listener.
* **Security/Compliance:** Hold release must be attempted atomically within the same transaction
  as the status update. If `releaseHold` fails, the cancellation must not be persisted and the
  rental remains `ACTIVE`.
* **Usability/Other:** `InvalidRentalStatusException` message includes both current and target
  status (handled by state machine — FR-01).

## 4. Acceptance Criteria (BDD)

**Scenario 1: Cancel a DRAFT rental**

* **Given** a rental in `DRAFT` status with reserved equipment
* **When** `CancelRentalUseCase.execute(rentalId)` is called
* **Then** all `RentalEquipment` statuses are set to `RETURNED`, the rental status is `CANCELLED`,
  no hold-release call is made, and a `RentalCancelled` event is published

**Scenario 2: Cancel an ACTIVE rental — hold is returned to customer**

* **Given** a rental in `ACTIVE` status with a hold placed during activation
* **When** `CancelRentalUseCase.execute(rentalId)` is called
* **Then** all `RentalEquipment` statuses are set to `RETURNED`, `FinanceFacade.releaseHold` is
  called to return the held amount to the customer, the rental status becomes `CANCELLED`, and a
  `RentalCancelled` event is published

**Scenario 3: Equipment is marked AVAILABLE after cancellation event**

* **Given** a `RentalCancelled` event is published with equipment IDs `[1, 2]`
* **When** `RentalEventListener.onRentalCancelled` processes the event
* **Then** equipment `1` and `2` are set to `AVAILABLE` status

**Scenario 4: Cancellation of a COMPLETED rental is rejected**

* **Given** a rental in `COMPLETED` status
* **When** `CancelRentalUseCase.execute(rentalId)` is called
* **Then** `InvalidRentalStatusException` is thrown and the rental is not modified

**Scenario 5: Cancellation of a DEBT rental is rejected**

* **Given** a rental in `DEBT` status
* **When** `CancelRentalUseCase.execute(rentalId)` is called
* **Then** `InvalidRentalStatusException` is thrown

**Scenario 6: Cancellation of a non-existent rental — 404**

* **Given** a rental ID that does not exist
* **When** `CancelRentalUseCase.execute(rentalId)` is called
* **Then** `ResourceNotFoundException` is thrown

**Scenario 7: Hold-release failure rolls back the cancellation**

* **Given** a rental in `ACTIVE` status and `FinanceFacade.releaseHold` throws an exception
* **When** `CancelRentalUseCase.execute(rentalId)` is called
* **Then** the rental status remains `ACTIVE` and no `RentalCancelled` event is published

## 5. Out of Scope

* Refunding prepayment amounts to the customer (release of hold is the only financial operation).
* Cancelling individual equipment items (cancellation applies to the entire rental).
* Modifying the `RentalUpdated` event or its existing listener logic.
* Changing the DEBT status flow in `ReturnEquipmentService`.
