# User Story: FR-01 — RentalStatus Enum: State Machine Ownership

## 1. Description

**As a** system,
**I want to** enforce all allowed rental lifecycle transitions inside the `RentalStatus` enum,
**So that** no service layer can bypass the state machine rules and transitions are validated in a
single, authoritative place.

## 2. Context & Business Rules

* **Trigger:** Any component that needs to transition a rental to a new status calls the state
  machine on `RentalStatus` before applying the change.
* **Allowed transitions (complete list):**
    - `DRAFT` → `ACTIVE`
    - `DRAFT` → `CANCELLED`
    - `ACTIVE` → `CANCELLED`
* **All other transitions are invalid,** including but not limited to:
    - `ACTIVE` → `DRAFT`
    - `CANCELLED` → `ACTIVE`
    - `DEBT` → `CANCELLED`
    - `COMPLETED` → `ACTIVE`
* **`DEBT` is set exclusively by the return flow** (`ReturnEquipmentService`) when settlement exceeds
  prepayment. It is not part of the lifecycle endpoint transitions and must not be included as a
  lifecycle target.
* **Validation method:** `RentalStatus` exposes a method (e.g., `validateTransitionTo(RentalStatus target)`)
  that throws `InvalidRentalStatusException` when the requested transition is not in the allowed set
  for the current status.
* **`RentalStatus` owns the allowed-transitions map** — no service class should contain its own
  transition logic.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No external calls; pure in-memory enum logic. No measurable overhead.
* **Security/Compliance:** Prevents illegal state manipulation at the domain level, independent of
  the HTTP layer.
* **Usability/Other:** `InvalidRentalStatusException` message should include both the current and
  requested target status for clear diagnostics.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Valid transition DRAFT → ACTIVE succeeds**

* **Given** a `RentalStatus` of `DRAFT`
* **When** `validateTransitionTo(ACTIVE)` is called
* **Then** no exception is thrown

**Scenario 2: Valid transition DRAFT → CANCELLED succeeds**

* **Given** a `RentalStatus` of `DRAFT`
* **When** `validateTransitionTo(CANCELLED)` is called
* **Then** no exception is thrown

**Scenario 3: Valid transition ACTIVE → CANCELLED succeeds**

* **Given** a `RentalStatus` of `ACTIVE`
* **When** `validateTransitionTo(CANCELLED)` is called
* **Then** no exception is thrown

**Scenario 4: Invalid transition ACTIVE → DRAFT is rejected**

* **Given** a `RentalStatus` of `ACTIVE`
* **When** `validateTransitionTo(DRAFT)` is called
* **Then** `InvalidRentalStatusException` is thrown containing both `ACTIVE` and `DRAFT` in the message

**Scenario 5: Invalid transition CANCELLED → ACTIVE is rejected**

* **Given** a `RentalStatus` of `CANCELLED`
* **When** `validateTransitionTo(ACTIVE)` is called
* **Then** `InvalidRentalStatusException` is thrown

**Scenario 6: Invalid transition DEBT → CANCELLED is rejected**

* **Given** a `RentalStatus` of `DEBT`
* **When** `validateTransitionTo(CANCELLED)` is called
* **Then** `InvalidRentalStatusException` is thrown

**Scenario 7: ACTIVE → ACTIVE (self-transition) is rejected**

* **Given** a `RentalStatus` of `ACTIVE`
* **When** `validateTransitionTo(ACTIVE)` is called
* **Then** `InvalidRentalStatusException` is thrown

## 5. Out of Scope

* Adding new statuses to the `RentalStatus` enum.
* Changing how `DEBT` is assigned — it is set directly by `ReturnEquipmentService` and is not
  part of the lifecycle state machine.
* Database schema changes.
* HTTP error mapping for `InvalidRentalStatusException` (already handled by `RentalRestControllerAdvice`).
