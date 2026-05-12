# FR-05 Checklist — Component Tests for the Rental Lifecycle Endpoint

## Files to Create

- [ ] `component-test/src/test/java/.../transformer/RentalCancelledEventTransformer.java`
    - [ ] `@DataTableType` method handles `rentalId`, `customerId` (via `Aliases`), `equipmentIds` (comma-separated)
    - [ ] Compiles without errors

- [ ] `component-test/src/test/java/.../steps/rental/RentalCancelledEventSteps.java`
    - [ ] Step `@Then("the following rental cancelled event was published")` registered
    - [ ] Uses `MessageStore.getEventsFor(RentalCancelled.class, ...)`
    - [ ] `Awaitility` with `atMost(3s)`, `pollInterval(100ms)`
    - [ ] Asserts `rentalId`, `customerId`, `equipmentIds` with `SoftAssertions`

- [ ] `component-test/src/test/java/.../transformer/RentalLifecycleRequestTransformer.java`
    - [ ] `@DataTableType` method handles `status` (→ `RentalLifecycleStatus` enum) and `operatorId`
    - [ ] Compiles without errors

- [ ] `component-test/src/test/java/.../steps/rental/RentalLifecycleWebSteps.java`
    - [ ] Step `@Given("the lifecycle request is")` registered
    - [ ] Accepts `RentalLifecycleRequest` directly as method parameter (no `ObjectMapper`)
    - [ ] Calls `scenarioContext.setRequestBody(request)`

- [ ] `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
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
- [ ] `RentalCancelled` event record exists in `shared/domain/event/` (created by FR-04 task-002)
  before implementing this FR
- [ ] `RentalLifecycleUseCase` and its `execute` method exist (created by FR-02) before implementing
  this FR
