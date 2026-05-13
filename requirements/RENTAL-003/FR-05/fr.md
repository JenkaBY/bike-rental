# User Story: FR-05 — Component Tests for the Rental Lifecycle Endpoint

## 1. Description

**As a** developer,
**I want to** have component (integration) tests covering the `PATCH /api/rentals/{rentalId}/lifecycles`
endpoint,
**So that** correctness of the full activation and cancellation flows is verified end-to-end
against real infrastructure (database, event bus).

## 2. Context & Business Rules

* **Trigger:** FR-01 through FR-04 are implemented. Component tests are the final verification
  gate for RENTAL-003.
* **Scenarios to cover:**
    - Activate a rental (`DRAFT` → `ACTIVE`): hold is placed, status persisted, `RentalStarted`
      event published, equipment status updated to `RENTED`.
    - Cancel a draft rental (`DRAFT` → `CANCELLED`): no hold release, status persisted,
      `RentalCancelled` event published, equipment status updated to `AVAILABLE`.
    - Cancel an active rental (`ACTIVE` → `CANCELLED`): hold is released, status persisted,
      `RentalCancelled` event published, equipment status updated to `AVAILABLE`.
* **Out of scope for component tests:** request-validation negative cases (null status, invalid
  enum values) — those are covered by WebMvc tests in FR-02.
* **Reuse existing infrastructure:** `WebRequestSteps`, `RentalDbSteps`, `RentalEventSteps`,
  and `RentalWebSteps` steps must be reused wherever possible.
* **New artifacts required:**
    - `RentalLifecycleWebSteps` — steps to build the lifecycle request body and perform the
      `PATCH` call.
    - `RentalCancelledEventSteps` — steps to assert the `RentalCancelled` event.
    - `RentalCancelledEventTransformer` — `@DataTableType` transformer for `RentalCancelled`.
    - `rental-lifecycle.feature` — Gherkin feature file under `features/rental/`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Tests run against a real (Docker) database; scenarios must be independent and
  clean up state via the existing `DbSteps` after-hook.
* **Security/Compliance:** N/A.
* **Usability/Other:** Feature file must follow project Gherkin conventions (no JSON in steps,
  descriptive scenario names, `Background:` for shared setup).

## 4. Acceptance Criteria (BDD)

**Scenario 1: Activate a DRAFT rental**

* **Given** a rental in `DRAFT` status with full data and sufficient customer wallet balance
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `status = ACTIVE`
* **Then** the response status is `200`
* **And** the rental status is `ACTIVE` in the database
* **And** all rental equipment statuses are `ACTIVE` in the database
* **And** a `RentalStarted` event is published

**Scenario 2: Cancel a DRAFT rental**

* **Given** a rental in `DRAFT` status with reserved equipment
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `status = CANCELLED`
* **Then** the response status is `200`
* **And** the rental status is `CANCELLED` in the database
* **And** all rental equipment statuses are `RETURNED` in the database
* **And** a `RentalCancelled` event is published
* **And** the equipment records become `AVAILABLE` (via event listener)

**Scenario 3: Cancel an ACTIVE rental**

* **Given** a rental in `ACTIVE` status with rented equipment and a financial hold
* **When** `PATCH /api/rentals/{rentalId}/lifecycles` is called with `status = CANCELLED`
* **Then** the response status is `200`
* **And** the rental status is `CANCELLED` in the database
* **And** all rental equipment statuses are `RETURNED` in the database
* **And** a `RentalCancelled` event is published
* **And** the customer hold sub-ledger balance is `0.00` (hold released)
* **And** the equipment records become `AVAILABLE` (via event listener)

## 5. Out of Scope

* Negative validation scenarios (covered by FR-02 WebMvc tests).
* Pagination or query endpoint changes.
* Testing intermediate states (e.g. partial holds).
