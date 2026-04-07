# System Design: FR-FIN-12 - Automatic DEBT Settlement on Customer Deposit

## 1. Architectural Overview

FR-FIN-12 introduces a reactive, event-driven recovery loop that runs entirely within the existing modular monolith.
When a customer deposit is committed, the Finance module publishes a new `CustomerFundDeposited` domain event. The
Rental module listens for this event asynchronously and, for each rental in `DEBT` status belonging to that customer,
re-invokes the standard settle-rental flow already established by FR-FIN-07 and FR-FIN-08. No new settlement logic is
introduced anywhere — the only structural additions are the new event record, its publication inside
`RecordDepositService`, and a new `@ApplicationModuleListener` in the Rental module backed by an isolated-transaction
service.

The critical transaction design is: the event is consumed **after** the deposit transaction commits (Spring Modulith
guarantees this for `@ApplicationModuleListener`). Each DEBT rental's settlement attempt runs in its **own
independent transaction**, so a successful settlement on rental A is committed even if rental B subsequently fails.
The `OverBudgetSettlementException` from the Finance module is caught and silently discarded so that the event
listener never propagates a failure from one rental attempt to the others — or back to the event infrastructure.

## 2. Impacted Components

* **`RecordDepositService` (Finance — Application Service):**
  Must publish a `CustomerFundDeposited` event after a successful deposit transaction is persisted. The publication
  is appended to the end of the existing `execute()` method using the shared `EventPublisher` port, matching the
  pattern already used in `RecordPaymentService`.

* **`SpringApplicationEventPublisher` (Shared — Infrastructure):**
  No structural change. Already implements the `EventPublisher` port; delivers the new event to any registered
  `@ApplicationModuleListener` after the producing transaction commits.

* **`SettleDebtUseCase` (Rental — Application Use Case, new):**
  New interface in the Rental module's `application/usecase` package, following the project's existing
  use-case contract pattern (see `ReturnEquipmentUseCase`). Declares:
    * Nested record `SettleDebtCommand(CustomerRef customerRef, RentalRef rentalRef, String operatorId)`
    * Nested record `SettleDebtResult(boolean settled)`
    * Single method: `SettleDebtResult execute(SettleDebtCommand command)`

* **`DebtSettlementEventListener` (Rental — Infrastructure, new):**
  New `@Component` in the Rental module's `infrastructure/eventlistener` package. Carries a single
  `@ApplicationModuleListener` method consuming `CustomerFundDeposited`. Responsible for:
    1. Calling `RentalRepository.getCustomerDebtRentals(CustomerRef)` to retrieve all `DEBT` rentals for
       the depositing customer, ordered by `createdAt` ascending.
    2. Looping over the results and calling `SettleDebtUseCase.execute(SettleDebtCommand)` per rental.

* **`SettleDebtRentalsService` (Rental — Application Service, new):**
  New `@Service` in the Rental module's `application/service` package. Implements `SettleDebtUseCase`.
  The `execute` method is annotated `@Transactional(propagation = REQUIRES_NEW)` so each rental
  settlement runs in its own isolated database transaction, independent of any outer transaction held
  by the event listener. Within a single `execute` call:
    * Loads the `Rental` aggregate by `command.rentalRef()`.
    * Calls `FinanceFacade.settleRental(customerRef, rentalRef, totalFinalCost, operatorId)`.
    * On success: calls `rental.completeWithStatus(totalFinalCost, COMPLETED)`, saves via
      `RentalRepository`, and returns `SettleDebtResult(settled = true)`.
    * On `OverBudgetSettlementException`: catches the exception, does not update the rental, and
      returns `SettleDebtResult(settled = false)`. The `REQUIRES_NEW` transaction is committed cleanly
      with no side-effects (Finance writes nothing before throwing).

* **`RentalRepository` (Rental — Domain Port):**
  Needs one new query method: `getCustomerDebtRentals(CustomerRef)` returning a list of `Rental` aggregates
  in `DEBT` status for the given customer, ordered by creation date ascending.

* **`RentalRepositoryAdapter` (Rental — Infrastructure):**
  Provides the JPA implementation for the new query method, delegating to `RentalJpaRepository`.

* **`RentalJpaRepository` (Rental — Infrastructure):**
  Needs one new Spring Data derived query: `findAllByCustomerIdAndStatusOrderByCreatedAtAsc`.

* **`CustomerFundDeposited` (Finance — Domain Event, new):**
  New record in the `finance` module's public API package (alongside `PaymentReceived`), implementing
  `BikeRentalEvent`. Carries `customerId`, `transactionId`, and `operatorId` — the staff member who
  recorded the deposit, forwarded to any downstream money operation.

## 3. Abstract Data Schema Changes

* **Entity: `Rental`**
    * **Attributes Added/Modified:** None — `status` (`DEBT` → `COMPLETED`) and `totalCost` are already present.
    * **Relations:** No change.

* **Entity: `CustomerFundDeposited` (new event record — in-memory only, no new table)**
    * **Attributes:** `customerId` (customer identity), `transactionId` (UUID of the deposit transaction for
      correlation / idempotency reference), `operatorId` (identity of the staff member who recorded the deposit),
      `depositedAt` (timestamp).

No new database tables are required. Spring Modulith persists unpublished domain events to its own
`event_publication` table (already provisioned), which handles delivery guarantees automatically.

## 4. Component Contracts & Payloads

* **Interaction: `RecordDepositService` → `SpringApplicationEventPublisher`**
    * **Protocol:** In-process method call (Spring `ApplicationEventPublisher`)
    * **Payload Changes:** New `CustomerFundDeposited` event published after transaction commit, carrying
      `customerId`, `transactionId`, `operatorId`, and `depositedAt`.

* **Interaction: `SpringApplicationEventPublisher` → `DebtSettlementEventListener`**
    * **Protocol:** Spring Modulith in-process async event, delivered post-commit via `@ApplicationModuleListener`
    * **Payload:** `CustomerFundDeposited` record

* **Interaction: `DebtSettlementEventListener` → `RentalRepository`**
    * **Protocol:** In-process repository call (domain port)
    * **Payload Changes:** New query `getCustomerDebtRentals(customerRef)` — encapsulates `DEBT` filter and
      `createdAt ASC` ordering; returns `List<Rental>`

* **Interaction: `DebtSettlementEventListener` → `SettleDebtRentalsService` (per rental)**
    * **Protocol:** In-process synchronous method call via `SettleDebtUseCase`
    * **Payload Changes:** Full `SettleDebtCommand(customerRef, rentalRef, operatorId)` constructed per
      rental; `operatorId` sourced from the `CustomerFundDeposited` event

* **Interaction: `SettleDebtRentalsService` → `RentalRepository`**
    * **Protocol:** In-process repository call (domain port) — read rental by ref, then save on success
    * **Payload Changes:** Uses existing `findById` / `save` methods; no new methods needed here

* **Interaction: `SettleDebtRentalsService` → `FinanceFacade`**
    * **Protocol:** In-process synchronous Facade call (existing `settleRental` operation)
    * **Payload Changes:** `operatorId` forwarded from `SettleDebtCommand`, preserving the identity of the
      staff member who originally recorded the deposit.

## 5. Updated Interaction Sequence

**Happy path — deposit covers at least one DEBT rental:**

1. `DepositCommandController` receives `POST /api/deposits` and delegates to `RecordDepositService`.
2. `RecordDepositService` persists the deposit transaction and updates `CUSTOMER_WALLET` balance within a
   database transaction, then publishes `CustomerFundDeposited` **before the transaction commits** (Spring
   Modulith records it to `event_publication`; delivery happens post-commit).
3. The deposit transaction commits. Spring Modulith delivers `CustomerFundDeposited` asynchronously to
   `DebtSettlementEventListener`.
4. `DebtSettlementEventListener.onCustomerFundDeposited(event)` calls
   `RentalRepository.getCustomerDebtRentals(CustomerRef.of(event.customerId()))` to load all `DEBT`
   rentals in creation order.
5. If the result is empty → processing ends silently.
6. For each `DEBT` rental, the listener constructs
   `new SettleDebtCommand(customerRef, RentalRef.of(rental.getId()), event.operatorId())` and calls
   `settleDebtUseCase.execute(command)`.
7. Inside each `execute` call (`@Transactional(propagation = REQUIRES_NEW)`):
    1. Loads the `Rental` aggregate by `command.rentalRef()`.
    2. Calls `FinanceFacade.settleRental(customerRef, rentalRef, totalFinalCost, command.operatorId())`.
    3. Finance executes the normal (FR-FIN-07) or overtime (FR-FIN-08) settlement path.
    4. On success: `rental.completeWithStatus(totalFinalCost, COMPLETED)` → `RentalRepository.save(rental)`.
       The `REQUIRES_NEW` transaction commits. `SettleDebtResult(settled = true)` is returned.
    5. On `OverBudgetSettlementException`: caught inside `execute`; no rental update; the `REQUIRES_NEW`
       transaction commits with no side-effects. `SettleDebtResult(settled = false)` is returned.
       The listener continues to the next rental.
8. After all rentals are processed, `DebtSettlementEventListener` returns normally; Spring Modulith marks the
   `event_publication` entry as consumed.

**Unhappy path — system error mid-settlement:**

- Steps 1–6 as above.
- If an unexpected exception occurs inside a rental's `REQUIRES_NEW` transaction (not
  `OverBudgetSettlementException`), that transaction rolls back; the rental remains `DEBT`. The outer listener
  transaction is not affected. Depending on Spring Modulith configuration, the `event_publication` record
  may be retried on restart.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The settlement triggered by this flow is system-initiated but always traceable to a
  human actor — the `operatorId` from the originating deposit is carried through the event and recorded on every
  Finance journal entry produced during recovery. This ensures all auto-settled transactions are auditable and
  attributable to the staff member who triggered the deposit.

* **Scale & Performance:** The event listener is asynchronous and post-commit; it does not block the HTTP
  response to the depositing staff member. The number of `DEBT` rentals per customer is expected to be very small
  in practice (typically one), so no cursor pagination is required on the query. Each settlement runs serially in
  its own transaction to avoid concurrent modification conflicts on the shared Finance account.
