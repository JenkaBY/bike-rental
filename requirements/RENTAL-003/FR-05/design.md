# System Design: FR-05 — Component Tests for the Rental Lifecycle Endpoint

## 1. Architectural Overview

This story adds Cucumber component tests under the `component-test` module that drive the full
lifecycle endpoint end-to-end against real infrastructure (PostgreSQL, Spring event bus). The
tests are intentionally kept to happy paths — request validation is already covered by FR-02
WebMvc tests.

Three new classes are introduced, all within the existing
`com.github.jenkaby.bikerental.componenttest` package tree:

| Class                             | Package         | Purpose                                          |
|-----------------------------------|-----------------|--------------------------------------------------|
| `RentalLifecycleWebSteps`         | `steps/rental/` | Build lifecycle request body; perform PATCH call |
| `RentalCancelledEventSteps`       | `steps/rental/` | Assert `RentalCancelled` event published         |
| `RentalCancelledEventTransformer` | `transformer/`  | `@DataTableType` for `RentalCancelled`           |

One new feature file is added:

| File                       | Location                              |
|----------------------------|---------------------------------------|
| `rental-lifecycle.feature` | `src/test/resources/features/rental/` |

---

## 2. Impacted Components

* **`RentalLifecycleWebSteps` (New):**
    - Step: `Given the lifecycle request status is {string}` — stores a `RentalLifecycleRequest`
      with the given status string in `ScenarioContext.requestBody`.
    - Step: `When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context`
      — reuses the generic `WebRequestSteps.requestHasBeenMadeToEndpointWithContext` step; no new
      HTTP step is needed since the URL pattern matches the existing `with context` step.

* **`RentalCancelledEventSteps` (New):**
    - Step: `Then the following rental cancelled event was published` — awaits and asserts
      `RentalCancelled` event from `MessageStore`, following the pattern of
      `RentalCompletedEventSteps`.

* **`RentalCancelledEventTransformer` (New):**
    - `@DataTableType` method `transform(Map<String, String>)` → `RentalCancelled`.
    - Handles `rentalId` (Long), `customerId` (UUID via `Aliases`), `equipmentIds` (comma-separated
      Long list).

* **`rental-lifecycle.feature` (New):**
    - `Feature: Rental Lifecycle Management`
    - `Background:` sets up customers, equipment statuses, equipment types, equipment records,
      finance accounts/sub-ledgers, tariff records — reused from `rental.feature` background.
    - Three `Scenario:` blocks covering FR-05 acceptance criteria.

* **`RentalDbSteps` (Existing — no change):**
    - Steps `a single rental exists in the database with the following data` and
      `rental equipments were persisted in database` are reused as-is.

* **`RentalEventSteps` (Existing — no change):**
    - Step `the following rental started event was published` is reused for the activation scenario.

---

## 3. Abstract Data Schema Changes

None. All tables used by these tests already exist.

---

## 4. Component Contracts & Payloads

* **Interaction: Component test → `PATCH /api/rentals/{rentalId}/lifecycles`**
    * **Protocol:** HTTP PATCH via `TestRestTemplate`
    * **Request body built by:** `RentalLifecycleWebSteps` placing `RentalLifecycleRequest` in
      `ScenarioContext`
    * **Path variable substitution:** handled by the existing `with context` step variant in
      `WebRequestSteps`

* **Interaction: `RentalCancelledEventSteps` → `MessageStore`**
    * **Protocol:** In-process event store lookup (same pattern as `RentalCompletedEventSteps`)
    * **Event type:** `com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled`
    * **Extractor:** `RentalCancelled::rentalId`

---

## 5. Updated Interaction Sequence

### Activate scenario

1. `Background` steps seed DB (customer, equipment, finance accounts, tariffs).
2. `a single rental exists in the database` seeds DRAFT rental with equipment in `ASSIGNED` status
   and estimated cost.
3. `Given the lifecycle request status is "ACTIVE"` → `ScenarioContext.requestBody` =
   `RentalLifecycleRequest(ACTIVE, "OP1")`.
4. `When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context`
   → HTTP PATCH sent; response stored.
5. `Then the response status is 200` → asserted.
6. `And rental was persisted in database` with status `ACTIVE`.
7. `And rental equipments were persisted in database` with status `ACTIVE`.
8. `And the following rental started event was published`.

### Cancel DRAFT scenario

Steps 1–2 same; rental in DRAFT status.

3. `Given the lifecycle request status is "CANCELLED"`.
   4–5. PATCH sent; 200 asserted.
6. `And rental was persisted in database` with status `CANCELLED`.
7. `And rental equipments were persisted in database` with status `RETURNED`.
8. `And the following rental cancelled event was published`.
9. Equipment records polled via Awaitility until status `AVAILABLE`.

### Cancel ACTIVE scenario

Same as cancel DRAFT but rental seeded with `ACTIVE` status and hold sub-ledger balance > 0.
Additionally asserts customer hold sub-ledger balance is `0.00` after cancellation.

---

## 6. Non-Functional Architecture Decisions

* **Isolation:** Each scenario is independent; DB truncation is handled by the existing after-hook
  in `DbSteps`.
* **Awaitility:** All event and async equipment-status assertions use `await().atMost(3s)` polling
  at 100ms intervals, consistent with other rental event steps.
