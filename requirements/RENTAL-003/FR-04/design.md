# System Design: FR-04 — CancelRentalUseCase + RentalCancelled Event

## 1. Architectural Overview

This story introduces a `CancelRentalUseCase` / `CancelRentalService` pair in the `rental` module
and a new `RentalCancelled` domain event in the `shared` module. Cancellation is the first
lifecycle transition that must coordinate two write concerns within the same transaction: releasing
a financial hold (when the source status is `ACTIVE`) and marking all `RentalEquipment` records as
`RETURNED`. Equipment availability in the `equipment` module is restored asynchronously after the
transaction commits, via a new `@ApplicationModuleListener` in `RentalEventListener` that
consumes the `RentalCancelled` event.

The `RentalCancelled` event sits in the shared module alongside `RentalStarted` and
`RentalCompleted`, making it available to any module that needs to react to rental cancellations.

---

## 2. Impacted Components

* **`CancelRentalUseCase` (Application Use Case Interface — `rental/application/usecase/`):** *(new)*
  Defines a single method `execute(Long rentalId)` returning the cancelled `Rental`.

* **`CancelRentalService` (Application Service — `rental/application/service/`):** *(new)*
  Implements `CancelRentalUseCase`. Orchestrates the full cancellation flow within a single
  `@Transactional` boundary:
    1. Load `Rental` by ID; throw `ResourceNotFoundException` if absent.
    2. Invoke `currentStatus.validateTransitionTo(CANCELLED)` (FR-01 state machine).
    3. If current status is `ACTIVE`: call `FinanceFacade.releaseHold(RentalRef)`.
    4. Set all `RentalEquipment` child records to `RETURNED` status.
    5. Set `Rental` status to `CANCELLED`.
    6. Persist via `RentalRepository.save(rental)`.
    7. Publish `RentalCancelled` event via `EventPublisher`.
    8. Return the saved `Rental`.

* **`RentalCancelled` (Domain Event — `shared/domain/event/`):** *(new)*
  A Java `record` implementing `BikeRentalEvent`. Fields: `rentalId` (Long), `customerId` (UUID),
  `equipmentIds` (List of Long — IDs of all equipment in the rental, regardless of their
  `RentalEquipmentStatus` at the time of cancellation). Consistent placement and naming with the
  existing `RentalStarted` and `RentalCompleted` records.

* **`RentalEventMapper` (Application Mapper — `rental/application/mapper/`):**
  Gains a new mapping method `toRentalCancelled(Rental rental)` that produces a `RentalCancelled`
  event, extracting equipment IDs from the rental's equipment collection.

* **`RentalEventListener` (Event Consumer — `equipment/infrastructure/eventlistener/`):**
  Gains a new `@ApplicationModuleListener` method `onRentalCancelled(RentalCancelled event)`.
  On receipt: loads equipment records by `event.equipmentIds()` and sets each to `AVAILABLE`
  status via `UpdateEquipmentUseCase`. The existing `onRentalUpdated` handler for the
  draft-cancellation path is left unchanged for backward compatibility during the transition period.

---

## 3. Abstract Data Schema Changes

* **Entity: `rental_equipments`**
    * **Attributes Modified:** The `status` column is already `VARCHAR` (or equivalent); no DDL
      change required. The new code path sets all child rows to `'RETURNED'` on cancellation — a
      value already used by the return flow.
* **Relations:** No change.

---

## 4. Component Contracts & Payloads

* **Interaction: `RentalLifecycleService` → `CancelRentalUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** Passes `rentalId` (Long). Called by the lifecycle orchestrator (FR-02).
      Returns updated `Rental`.

* **Interaction: `CancelRentalService` → `FinanceFacade`**
    * **Protocol:** In-process Facade call (Spring Modulith cross-module)
    * **Payload Changes:** `releaseHold(RentalRef)` — existing method, no signature change.
      Called only when the source status is `ACTIVE`. If the call throws, the transaction rolls
      back and the `Rental` remains `ACTIVE`.

* **Interaction: `CancelRentalService` → `EventPublisher`**
    * **Protocol:** In-process `ApplicationEventPublisher`
    * **Payload Changes:** New `RentalCancelled(rentalId, customerId, equipmentIds)` event
      published after the transaction commits.

* **Interaction: `EventPublisher` → `RentalEventListener` (equipment module)**
    * **Protocol:** Spring Modulith `@ApplicationModuleListener` (asynchronous, in-process,
      transactional)
    * **Payload Changes:** `RentalCancelled` event consumed. Triggers equipment status update to
      `AVAILABLE` for all IDs in `equipmentIds`.

---

## 5. Updated Interaction Sequence

### Happy path — cancel a DRAFT rental

1. `RentalCommandController` receives `PATCH /api/rentals/42/lifecycles` with `{ "status": "CANCELLED" }`.
2. Delegates to `CancelRentalUseCase.execute(42L)`.
3. `CancelRentalService` loads `Rental` (status = `DRAFT`).
4. `RentalStatus.DRAFT.validateTransitionTo(CANCELLED)` — passes (FR-01).
5. Source status is `DRAFT` → `FinanceFacade.releaseHold` is **not** called.
6. All `RentalEquipment` records set to `RETURNED`.
7. `Rental` status set to `CANCELLED`.
8. `RentalRepository.save(rental)` — transaction commits.
9. `EventPublisher.publish("rental-events", RentalCancelled{id=42, ...})`.
10. `RentalEventListener.onRentalCancelled` fires asynchronously: equipment records set to `AVAILABLE`.
11. Updated `Rental` returned → `200 OK`.

### Happy path — cancel an ACTIVE rental

1–4. Same as above; source status is `ACTIVE`.

5. `FinanceFacade.releaseHold(RentalRef{id=42})` is called; hold returned to customer account.
6. All `RentalEquipment` records set to `RETURNED`.
7. `Rental` status set to `CANCELLED`.
8. `RentalRepository.save(rental)` — transaction commits.
9. `EventPublisher.publish("rental-events", RentalCancelled{id=42, ...})`.
10. `RentalEventListener.onRentalCancelled` fires asynchronously: equipment set to `AVAILABLE`.
11. `200 OK`.

### Unhappy path — hold release fails

1–5. `FinanceFacade.releaseHold(...)` throws an exception.

6. Transaction rolls back; `Rental` status remains `ACTIVE`; `RentalEquipment` statuses unchanged.
7. No `RentalCancelled` event is published.
8. Exception propagates to `CoreExceptionHandlerAdvice` / `RentalRestControllerAdvice`.

### Unhappy path — invalid source status (e.g., COMPLETED)

1. `CancelRentalService` loads `Rental` (status = `COMPLETED`).
2. `RentalStatus.COMPLETED.validateTransitionTo(CANCELLED)` throws `InvalidRentalStatusException`.
3. No financial call; no event; no persistence.
4. Exception mapped to HTTP error by `RentalRestControllerAdvice`.

### Unhappy path — rental not found

1. `RentalRepository.findById(rentalId)` returns empty.
2. `ResourceNotFoundException` thrown → `404 Not Found`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `releaseHold` is a synchronous cross-module call enclosed in the same
  `@Transactional` boundary as the status update and equipment-status writes. A failure in
  `releaseHold` atomically rolls back the entire cancellation, ensuring consistency between the
  financial and domain state.
* **Scale & Performance:** Equipment status restoration (`AVAILABLE`) is handled asynchronously
  via Spring Modulith's event listener after the main transaction commits. This decouples the
  cancellation response time from the equipment-module write, consistent with the existing event
  listener patterns in `RentalEventListener`.
