# FR-05 Checklist — Component Tests for the Rental Lifecycle Endpoint

## Files to Create

- [x] `component-test/src/test/java/.../transformer/RentalCancelledEventTransformer.java`
    - [x] `@DataTableType` method handles `rentalId`, `customerId` (via `Aliases`), `equipmentIds` (comma-separated)
    - [x] Compiles without errors

- [x] `component-test/src/test/java/.../steps/rental/RentalCancelledEventSteps.java`
    - [x] Step `@Then("the following rental cancelled event was published")` registered
    - [x] Uses `MessageStore.getEventsFor(RentalCancelled.class, ...)`
    - [x] `Awaitility` with `atMost(3s)`, `pollInterval(100ms)`
    - [x] Asserts `rentalId`, `customerId`, `equipmentIds` with `SoftAssertions`

- [x] `component-test/src/test/java/.../transformer/RentalLifecycleRequestTransformer.java`
    - [x] `@DataTableType` method handles `status` (→ `RentalLifecycleStatus` enum) and `operatorId`
    - [x] Compiles without errors

- [x] `component-test/src/test/java/.../steps/rental/RentalLifecycleWebSteps.java`
    - [x] Step `@Given("the lifecycle request is")` registered
    - [x] Accepts `RentalLifecycleRequest` directly as method parameter (no `ObjectMapper`)
    - [x] Calls `scenarioContext.setRequestBody(request)`

- [x] `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
    - [ ] `@ReinitializeSystemLedgers` tag present
    - [ ] `Background:` seeds customers, equipment statuses, equipment types, equipment records,
      accounts, sub-ledgers, transactions, tariff pricing params, tariffs
    - [ ] Scenario 1: Activate DRAFT → asserts HTTP 200, rental status ACTIVE, equipment ACTIVE,
      hold sub-ledger balance `16.00`, wallet sub-ledger balance `284.00`, `RentalStarted` event published
    - [ ] Scenario 2: Insufficient balance on activation → asserts HTTP 422,
      title `Insufficient funds`, errorCode `rental.insufficient_funds`
    - [ ] Scenario Outline (Cancel without hold): two examples — DRAFT+ASSIGNED and ACTIVE+ACTIVE
      (special tariff) → asserts HTTP 200, status CANCELLED, equipment RETURNED,
      `CUSTOMER_HOLD` balance remains `0.00`, `RentalCancelled` event published
    - [ ] Scenario (Cancel ACTIVE with hold): seeds hold sub-ledger `16.00` + HOLD transaction
      → asserts HTTP 200, status CANCELLED, equipment RETURNED,
      `CUSTOMER_HOLD` balance `0.00` (hold released), `RentalCancelled` event published

## Integration Checks

- [ ] All three scenarios pass: `./gradlew :component-test:test "-Dspring.profiles.active=test"`
- [ ] No step definition ambiguity warnings in test output
- [x] `RentalCancelled` event record exists in `shared/domain/event/` (created by FR-04 task-002)
  before implementing this FR
- [x] `RentalLifecycleUseCase` and its `execute` method exist (created by FR-02) before implementing
  this FR
