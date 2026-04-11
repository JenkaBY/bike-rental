# System Design: FR-FIN-14 - Integrate holdFunds into Rental Creation

## 1. Architectural Overview

FR-FIN-14 wires the Finance module's hold operation (FR-FIN-06) atomically into the Rental creation path. Prior to
this story, `CreateRentalService` persisted the rental aggregate without reserving any funds; the hold was an entirely
separate step. After this story, the hold becomes inseparable from creation: both the rental `save()` and the
`holdFunds()` call execute inside the **same `@Transactional` boundary** in `CreateRentalService`. If the hold fails
(insufficient balance, account not found) the transaction rolls back and no rental record is persisted.

A complementary query method, `hasHold(Long rentalId)`, is added to `FinanceFacade`. It is consumed by
`UpdateRentalService` to replace the legacy `hasPrepayment` guard when a draft rental is activated — ensuring that
only rentals backed by a live hold transition to `ACTIVE` status. No new REST endpoints are introduced; the changes
are purely in-process service logic, facade contracts, and error-handling plumbing.

---

## 2. Impacted Components

* **`CreateRentalService` (existing — Rental module application service, modified):**
  After computing `totalPlannedCost` (sum of `estimatedCost` across all `RentalEquipment` items), and after
  persisting the `Rental` aggregate via `RentalRepository.save(rental)` (which assigns the database-generated
  `Long` id), the service calls `FinanceFacade.holdFunds(customerRef, new RentalRef(rental.getId()), totalPlannedCost)`
  within the **same `@Transactional` method**. If `holdFunds` throws `InsufficientBalanceException`, the
  enclosing transaction is rolled back — the rental row is not persisted. If `totalPlannedCost` evaluates to
  zero, the service rejects the creation with a `ResourceConflictException` before attempting the hold.
  The `customerRef` is derived from `rental.getCustomerId()`.

* **`RentalRestControllerAdvice` (existing — Rental module web error handler, extended):**
  Gains a new `@ExceptionHandler` for `InsufficientBalanceException`. The handler maps it to
  `422 Unprocessable Entity` with `errorCode = INSUFFICIENT_FUNDS` and adds two extra `ProblemDetail`
  properties: `availableBalance` (the customer's current wallet balance at attempt time) and
  `requiredAmount` (the `totalPlannedCost` that could not be satisfied). A second new handler is added for
  `HoldRequiredException` → `409 Conflict` with `errorCode = HOLD_REQUIRED`.

* **`HoldRequiredException` (new — Rental module domain exception):**
  A typed unchecked exception in `rental/domain/` that `UpdateRentalService` throws when attempting to
  activate a draft rental for which `hasHold(rentalId)` returns `false`. Carries the rental identifier as
  context. Handled exclusively by `RentalRestControllerAdvice`.

* **`FinanceFacade` (existing — Finance module cross-module interface, extended):**
  Gains one new query method:
  ```
  boolean hasHold(Long rentalId)
  ```
  Returns `true` when at least one persisted `HOLD`-type transaction exists for the given rental reference.
  Used by `UpdateRentalService` as the activation guard. No existing `FinanceFacade` methods are modified by
  this story (deprecated methods are retained until FR-FIN-15).

* **`FinanceFacadeImpl` (existing — Finance module facade implementation, extended):**
  Implements the new `hasHold(Long rentalId)` method. Delegates to a new `TransactionRepository` query:
  `existsBySourceTypeAndSourceIdAndTransactionType("RENTAL", rentalId, HOLD)`. Returns the boolean result
  directly without additional logic.

* **`TransactionRepository` (existing — Finance module domain repository port, extended):**
  Gains one new query method:
  ```
  boolean existsBySourceTypeAndSourceIdAndTransactionType(String sourceType, Long sourceId, TransactionType type)
  ```
  Used exclusively by `FinanceFacadeImpl.hasHold`. No other repository methods are modified.

* **`UpdateRentalService` (existing — Rental module application service, modified):**
  In the rental activation path (`startRental` / status-patch to `ACTIVE`), the call to
  `FinanceFacade.hasPrepayment(rentalId)` is replaced with `FinanceFacade.hasHold(rental.getId())`. If
  `hasHold` returns `false`, `HoldRequiredException` is thrown. The rest of the activation logic (status
  transition, event publishing) is unchanged. Note: `hasPrepayment` remains present on `FinanceFacade`
  during this transition period and is removed in FR-FIN-15.

---

## 3. Abstract Data Schema Changes

No new tables, columns, or indexes are required.

* **Entity: `Transaction`** (table `finance_transactions`)
  The `HOLD` transaction type was introduced in FR-FIN-06 and is already stored in the `transaction_type`
  VARCHAR column. The `source_type = "RENTAL"` and `source_id = rentalId` columns already exist and are used
  to link hold transactions to rentals. No schema change is needed.

* **Relations:** Unchanged.

---

## 4. Component Contracts & Payloads

* **Interaction: `CreateRentalService` → `FinanceFacade`**
    * **Protocol:** In-process synchronous call (Spring Modulith cross-module facade), same `@Transactional`
      boundary as the rental `save()`.
    * **Method:** `HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money totalPlannedCost)`
      *(contract defined in FR-FIN-06, consumed here)*
    * **Payload Changes:** `rentalRef` is now populated from the DB-assigned `Long` id of the just-persisted
      `Rental` aggregate. `totalPlannedCost` is the aggregated sum of `estimatedCost` across all
      `RentalEquipment` items.
    * **Success:** `HoldInfo(transactionRef, recordedAt)` — creation proceeds; `201 Created` returned.
    * **Failure — insufficient balance:** `InsufficientBalanceException(availableBalance, requiredAmount)` →
      transaction rolls back → no rental row → `422 Unprocessable Entity` with `errorCode = INSUFFICIENT_FUNDS`,
      `availableBalance`, `requiredAmount`.
    * **Failure — zero planned cost:** rejected **before** calling `holdFunds` (`totalPlannedCost ≤ 0`) →
      `409 Conflict` with `errorCode = RESOURCE_CONFLICT`.

* **Interaction: `UpdateRentalService` → `FinanceFacade`**
    * **Protocol:** In-process synchronous call (Spring Modulith cross-module facade)
    * **Method (new):** `boolean hasHold(Long rentalId)`
    * **Payload Changes:** Replaces the legacy `hasPrepayment(rentalId)` call at the activation guard site.
    * **Success:** `true` → activation proceeds normally.
    * **Failure:** `false` → `HoldRequiredException` thrown → `409 Conflict` with `errorCode = HOLD_REQUIRED`.

* **Interaction: External HTTP client → `RentalCommandController` (POST /api/rentals)**
    * **Protocol:** REST
    * **Payload Changes (error response):** On insufficient balance, the `ProblemDetail` response gains two
      extra properties:
        * `availableBalance` (Decimal, scale 2) — customer's current spendable wallet balance.
        * `requiredAmount` (Decimal, scale 2) — total planned cost that could not be funded.

---

## 5. Updated Interaction Sequence

### Happy path — rental creation with sufficient balance

1. `RentalCommandController` receives `POST /api/rentals` and delegates to `CreateRentalService.execute(command)`.
2. `CreateRentalService` validates customer (via `CustomerFacade`), equipment availability (via `EquipmentFacade`
    + `RequestedEquipmentValidator`), and selects tariffs and calculates estimated costs (via `TariffFacade`) — as
      per the existing flow.
3. `CreateRentalService` computes `totalPlannedCost` as the sum of `estimatedCost` for all `RentalEquipment` items.
4. `CreateRentalService` guards: if `totalPlannedCost ≤ 0`, throws `ResourceConflictException` → `409`.
5. `CreateRentalService` calls `RentalRepository.save(rental)` — the `Rental` aggregate is persisted and receives
   its database-assigned `Long` id.
6. `CreateRentalService` calls `FinanceFacade.holdFunds(customerRef, new RentalRef(rental.getId()), totalPlannedCost)`.
7. `FinanceFacadeImpl` delegates to `RecordRentalHoldService` (FR-FIN-06 service) — balance guard passes, journal
   entry `CUSTOMER_WALLET` (debit) / `CUSTOMER_HOLD` (credit) is created, `Transaction[HOLD]` is persisted.
8. `CreateRentalService` publishes `RentalCreated` domain event.
9. Transaction commits — both the rental row and the hold transaction row are durable.
10. `RentalCommandController` returns `201 Created` with the rental response payload.

### Unhappy path — insufficient balance

1–4. Same as above.

5. `CreateRentalService` calls `RentalRepository.save(rental)` → rental row provisionally written in active
   transaction.
6. `CreateRentalService` calls `FinanceFacade.holdFunds(...)`.
7. `RecordRentalHoldService` detects `availableBalance < totalPlannedCost` → throws
   `InsufficientBalanceException(availableBalance, totalPlannedCost)`.
8. The exception propagates out of `CreateRentalService` → Spring `@Transactional` marks the transaction for
   rollback.
9. Both the rental row and any partial state are rolled back — no data is persisted.
10. `RentalRestControllerAdvice.handleInsufficientBalance(...)` maps the exception to `422 Unprocessable Entity`
    with `errorCode = INSUFFICIENT_FUNDS`, `availableBalance`, and `requiredAmount` in the `ProblemDetail`.

### Unhappy path — draft activation without a hold (legacy or corrupted record)

1. `RentalCommandController` receives `PATCH /api/rentals/{id}` with `status = ACTIVE`.
2. `UpdateRentalService` loads the `Rental` aggregate.
3. `UpdateRentalService` calls `FinanceFacade.hasHold(rental.getId())`.
4. `FinanceFacadeImpl` calls
   `TransactionRepository.existsBySourceTypeAndSourceIdAndTransactionType("RENTAL", id, HOLD)` → `false`.
5. `UpdateRentalService` throws `HoldRequiredException(rentalId)`.
6. `RentalRestControllerAdvice.handleHoldRequired(...)` maps to `409 Conflict` with `errorCode = HOLD_REQUIRED`.

### Happy path — draft activation with an existing hold

1–3. Same as above.

4. `TransactionRepository` query returns `true`.
5. `UpdateRentalService` proceeds with status transition to `ACTIVE`, records `startedAt`, publishes
   `RentalStarted`.
6. `200 OK` returned to client.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:**
  No authentication changes. The Rental module does not expose the customer's balance in any success response —
  balance information is surfaced only in the `422` error body to assist staff in informing the customer. The
  error body must not expose other customers' financial data; `InsufficientBalanceException` must carry only
  the balance associated with the rental's customer reference.

* **Scale & Performance:**
  The hold and the rental persist inside a single transaction. If `holdFunds` completes within its own 1-second
  NFR (FR-FIN-06) and tariff computation is fast, the overall creation path comfortably fits the 2-second NFR
  stated in this story. No caching, queueing, or additional concurrency control is required beyond the existing
  database transaction isolation.

* **Atomicity Guarantee:**
  Placing both `RentalRepository.save(rental)` and `FinanceFacade.holdFunds(...)` inside the same
  `@Transactional` method in `CreateRentalService` ensures that a committed rental always has a corresponding
  `HOLD` journal entry and a failed hold never leaves an orphaned rental record. This is the core invariant
  this story establishes.

* **Transition Safety:**
  `hasPrepayment` is intentionally left on `FinanceFacade` during this story's lifecycle and removed only in
  FR-FIN-15. This prevents compilation failures in any codebase state that has FR-FIN-14 partially applied.
  `UpdateRentalService` switches its activation guard from `hasPrepayment` to `hasHold` as part of this story,
  so both methods exist on the interface simultaneously until FR-FIN-15 cleans them up.

* **Idempotency:**
  `holdFunds` inherits the idempotency guarantee from FR-FIN-06 — the idempotency key is derived
  deterministically from `rentalRef.id()`. If `CreateRentalService` is retried (e.g., after a transient
  failure where the transaction was actually committed), the hold call is a no-op rather than a double-debit.
