# System Design: FR-03 — ActivateRentalUseCase: Extract Activation from UpdateRentalService

## 1. Architectural Overview

This story consolidates all rental activation logic — including financial hold placement — into a
single new application use case `ActivateRentalUseCase` / `ActivateRentalService`. Currently this
logic is split across two services: `UpdateRentalService` (status-patch path) and
`CreateRentalService` (fast-path `POST /api/rentals`). Both are modified to delegate to the new
use case, establishing one authoritative activation code path.

The `UpdateRentalService` loses its entire `/status` patch-field branch. `CreateRentalService`
loses its inline `holdFunds` call and instead invokes `ActivateRentalUseCase` after the initial
`DRAFT` save. No new HTTP endpoints are introduced by this story; the activation surface visible
to callers is the lifecycle endpoint from FR-02.

---

## 2. Impacted Components

* **`ActivateRentalUseCase` (Application Use Case Interface — `rental/application/usecase/`):** *(new)*
  Defines a single method `execute(Long rentalId)` returning the activated `Rental`. Carries a
  nested command/result type if needed for the `operatorId` parameter required by `holdFunds`.

* **`ActivateRentalService` (Application Service — `rental/application/service/`):** *(new)*
  Implements `ActivateRentalUseCase`. Orchestrates:
    1. Load `Rental` by ID from `RentalRepository`; throw `ResourceNotFoundException` if absent.
    2. Invoke `RentalStatus.validateTransitionTo(ACTIVE)` on the current status (FR-01 state machine).
    3. If `rental.getEstimatedCost().isPositive()`, call `FinanceFacade.holdFunds(customerRef,
       rentalRef, estimatedCost, operatorId)`. Propagate any finance exception without catching.
    4. Record `startedAt = LocalDateTime.now(clock)` and call `rental.activate(startedAt)`.
    5. Persist via `RentalRepository.save(rental)`.
    6. Build and publish `RentalStarted` event via `EventPublisher`.
    7. Return the saved `Rental`.

* **`UpdateRentalService` (Application Service — `rental/application/service/`):**
  The `startRental()` private method is deleted. The entire `if (patch.containsKey("status"))`
  block is removed. `UpdateRentalService` no longer depends on `FinanceFacade` for activation;
  the `FinanceFacade` dependency may be removed entirely from this class if no other patch field
  requires it.

* **`CreateRentalService` (Application Service — `rental/application/service/`):**
  The inline `if (saved.getEstimatedCost().isPositive()) { financeFacade.holdFunds(...) }` block
  is removed from `execute(CreateRentalCommand)`. After `repository.save(rental)`, if the command
  targets an immediately-active rental, `ActivateRentalUseCase.execute(saved.getId())` is called
  instead. `CreateDraftCommand` path is not affected.

* **`RentalCommandController` (API — `rental/web/command/`):**
  The existing `PATCH /api/rentals/{id}` endpoint continues to exist and delegates to
  `UpdateRentalUseCase`. No routing change is needed here; the lifecycle endpoint (FR-02) routes
  to `ActivateRentalUseCase` directly.

---

## 3. Abstract Data Schema Changes

None. The `startedAt` column already exists on the `rentals` table and is populated by the
existing `rental.activate()` domain method.

---

## 4. Component Contracts & Payloads

* **Interaction: `RentalLifecycleService` → `ActivateRentalUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** New `ActivateRentalUseCase.ActivateCommand(rentalId, operatorId)` (or
      equivalent). Called by the lifecycle orchestrator (FR-02). Returns the updated `Rental` aggregate.

* **Interaction: `ActivateRentalService` → `FinanceFacade`**
    * **Protocol:** In-process Facade call (Spring Modulith cross-module)
    * **Payload Changes:** `holdFunds(CustomerRef, RentalRef, Money estimatedCost, UUID operatorId)`
      — same signature as the call previously in `CreateRentalService`. Returns `HoldInfo`.
      Called only when `estimatedCost > 0`.

* **Interaction: `ActivateRentalService` → `EventPublisher`**
    * **Protocol:** In-process `ApplicationEventPublisher`
    * **Payload Changes:** Publishes `RentalStarted` event (existing record, schema unchanged).

* **Interaction: `CreateRentalService` → `ActivateRentalUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** `CreateRentalService` now delegates to `ActivateRentalUseCase` after the
      initial save, passing the new rental's ID and the operator ID from the original command.

---

## 5. Updated Interaction Sequence

### Fast-path rental creation (POST /api/rentals) — updated

1. `RentalCommandController` receives `POST /api/rentals` command.
2. `CreateRentalService.execute(CreateRentalCommand)` runs:
   a. Validates customer, equipment, tariff; builds `Rental` in `DRAFT` status.
   b. Saves the `DRAFT` rental; publishes `RentalCreated` event.
   c. Calls `ActivateRentalUseCase.execute(savedId, operatorId)` (steps below).
3. Returns activated `Rental` to controller.

### Lifecycle activation (PATCH /api/rentals/{id}/lifecycles with status=ACTIVE)

1. `RentalCommandController` receives request; delegates to `ActivateRentalUseCase.execute(rentalId, operatorId)`.
2. `ActivateRentalService` loads `Rental` from `RentalRepository`.
3. `RentalStatus.DRAFT.validateTransitionTo(ACTIVE)` — passes (FR-01).
4. `estimatedCost > 0` → `FinanceFacade.holdFunds(...)` called; `HoldInfo` returned.
5. `rental.activate(LocalDateTime.now(clock))` sets `startedAt` and status to `ACTIVE`.
6. `RentalRepository.save(rental)`.
7. `EventPublisher.publish("rental-events", RentalStarted)`.
8. Updated `Rental` returned → controller maps to `RentalResponse` → `200 OK`.

### Unhappy path — hold placement fails

1. Steps 1–3 succeed.
2. `FinanceFacade.holdFunds(...)` throws a finance exception.
3. Transaction rolls back; `Rental` status remains `DRAFT`.
4. Exception propagates to `RentalRestControllerAdvice` / `CoreExceptionHandlerAdvice`.
5. Appropriate HTTP error returned to client.

### Unhappy path — non-DRAFT rental

1. `ActivateRentalService` loads `Rental` with status `ACTIVE` (or other).
2. `currentStatus.validateTransitionTo(ACTIVE)` throws `InvalidRentalStatusException`.
3. No hold is attempted; no event published.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `holdFunds` is a synchronous, transactional cross-module call within the
  same JVM. Atomicity is guaranteed by the enclosing `@Transactional` on `ActivateRentalService`.
  If hold placement fails, the `Rental` save is rolled back.
* **Scale & Performance:** Hold placement is a synchronous, blocking call — same behaviour as the
  existing code in `CreateRentalService`. No concurrency model change.
