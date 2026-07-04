# User Story: FR-02 - Rental AWAITING_SIGNATURE Status & Signing Public API

## 1. Description

**As a** rental operator
**I want to** move a prepared draft rental into an `AWAITING_SIGNATURE` state (and back) through the existing lifecycle endpoint, with customer funds held at that moment
**So that** the customer can sign the rental agreement while the rental composition is frozen, and the agreement module (FR-05) can complete the activation atomically

## 2. Context & Business Rules

* **Trigger:** Operator sends `PATCH /api/rentals/{id}/lifecycles` with `{status: "AWAITING_SIGNATURE" | "DRAFT", operatorId}`.
* **Rules Enforced:**
    * New rental status `AWAITING_SIGNATURE`. Allowed transitions: `DRAFT → AWAITING_SIGNATURE`; `AWAITING_SIGNATURE → ACTIVE` (only via the signing facade), `AWAITING_SIGNATURE → DRAFT` (cancel signing). `DRAFT → ACTIVE` remains allowed until FR-06.
    * `DRAFT → AWAITING_SIGNATURE` requires the rental to be ready: customer selected, non-empty equipment list, planned duration and estimated cost present (same readiness rules as activation). Costs are NOT recalculated — they are already computed during draft editing.
    * On `DRAFT → AWAITING_SIGNATURE`, if `estimatedCost > 0`, customer funds are held via `FinanceFacade.holdFunds`. `startedAt` is NOT set at this step.
    * On `AWAITING_SIGNATURE → DRAFT`, an existing hold is released via `FinanceFacade.releaseHold`. Allowed only while no signature exists — guaranteed structurally: signing flips the status to `ACTIVE` in the same transaction as signature insertion (FR-05), and the race is resolved by optimistic locking (FR-01).
    * In `AWAITING_SIGNATURE`, editing the rental composition (customer, equipment, duration) is forbidden.
    * Rental exposes a public API (root package) for the agreement module:
        * `getSigningSnapshot(rentalId)` — returns rental data for the agreement PDF ONLY when status is `AWAITING_SIGNATURE`; otherwise a domain error (404 for missing rental, conflict error for wrong status).
        * `completeSigning(rentalId, expectedVersion, signedAt)` — verifies the fencing version and transitions `AWAITING_SIGNATURE → ACTIVE`, setting `startedAt = signedAt`; publishes the existing `RentalStarted` event.
    * The rental `version` (FR-01) acts as a one-time fencing token: the UI receives it in the lifecycle response and presents it when signing.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Same as existing lifecycle transitions (one aggregate load + save).
* **Security/Compliance:** operatorId is recorded in finance transactions as today; endpoints stay permitAll (project-wide approach).
* **Usability/Other:** Error responses follow the endpoint's existing contract: invalid transition → `422` `rental.status.invalid`; rental not ready → `422` `rental.activation.not_ready`; insufficient funds → existing finance error semantics.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Prepare signing holds funds and freezes the rental**

* **Given** a draft rental with a customer, equipment and a positive estimated cost
* **When** the operator PATCHes lifecycles with `status = AWAITING_SIGNATURE`
* **Then** the response is `200` with `status = AWAITING_SIGNATURE` and a `version`, the customer has an active hold for the estimated cost, and `startedAt` is null

**Scenario 2: Composition editing is rejected while awaiting signature**

* **Given** a rental in `AWAITING_SIGNATURE`
* **When** the operator attempts `PUT /api/rentals/{id}` with a changed equipment list
* **Then** the response is `422` with `errorCode = rental.status.invalid` and the composition is unchanged

**Scenario 3: Cancel signing returns to draft and releases the hold**

* **Given** a rental in `AWAITING_SIGNATURE` with a hold
* **When** the operator PATCHes lifecycles with `status = DRAFT`
* **Then** the response is `200` with `status = DRAFT`, the hold is released, and a subsequent `AWAITING_SIGNATURE` transition succeeds returning a higher `version`

**Scenario 4: Prepare signing rejected for incomplete draft**

* **Given** a draft rental without a customer
* **When** the operator PATCHes lifecycles with `status = AWAITING_SIGNATURE`
* **Then** the response is `422` and no hold is created

**Scenario 5: Zero-cost rental holds nothing**

* **Given** a draft rental whose estimated cost is `0`
* **When** the operator PATCHes lifecycles with `status = AWAITING_SIGNATURE`
* **Then** the response is `200` and no finance hold exists for the rental

## 5. Out of Scope

* The agreement module itself, signature persistence, PDF (FR-03..FR-05).
* REST exposure of `getSigningSnapshot` / `completeSigning` — they are consumed in-process by FR-05 and their behavior is fully covered by FR-05 component tests.
* Removing the direct `DRAFT → ACTIVE` activation (FR-06).
