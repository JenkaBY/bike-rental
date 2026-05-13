# System Design: FR-01 — RentalStatus Enum: State Machine Ownership

## 1. Architectural Overview

This story makes no changes to the runtime topology or persistence layer. It is a pure domain-model
refactoring: the `RentalStatus` enum, which already exists inside the `rental` module's domain
layer, is enriched with an allowed-transitions map and a `validateTransitionTo` method. All
service-layer classes that currently perform ad-hoc status checks must delegate to this method
instead of duplicating the transition rules.

The change is backward-compatible at the API level. The only observable external effect is that
previously under-validated status transitions now consistently raise `InvalidRentalStatusException`,
which is already mapped to an HTTP error response by the existing `RentalRestControllerAdvice`.

---

## 2. Impacted Components

* **`RentalStatus` (Domain Value Object — `rental/domain/model/`):**
  Gains an internal, static, immutable map of `RentalStatus → Set<RentalStatus>` representing
  allowed target states. Exposes a new method `validateTransitionTo(RentalStatus target)` that
  consults this map and throws `InvalidRentalStatusException` when the requested transition is
  absent. Self-transitions are explicitly excluded.

* **`InvalidRentalStatusException` (Domain Exception — `rental/domain/exception/`):**
  No structural change required. Existing constructor already accepts current and target status
  values; confirm the message template surfaces both values for diagnostics.

* **`UpdateRentalService` (Application Service — `rental/application/service/`):**
  Removes its own inline status-transition guard; delegates to `RentalStatus.validateTransitionTo`
  via the domain model instead. *(Full removal of status-patch handling is specified in FR-03.)*

* **`ReturnEquipmentService` (Application Service — `rental/application/service/`):**
  The `DEBT` assignment path calls `rental.completeWithStatus(cost, RentalStatus.DEBT)` directly
  and bypasses the public state machine — this is intentional and must remain unchanged. No call
  to `validateTransitionTo` is added for this path.

---

## 3. Abstract Data Schema Changes

None. The state machine is an in-memory construct on an existing enum; no new columns, tables, or
migration changesets are required.

---

## 4. Component Contracts & Payloads

* **Interaction: `ActivateRentalService` (FR-03) → `RentalStatus`**
    * **Protocol:** In-process method call
    * **Payload Changes:** `RentalStatus.DRAFT.validateTransitionTo(RentalStatus.ACTIVE)` —
      throws `InvalidRentalStatusException` if disallowed.

* **Interaction: `CancelRentalService` (FR-04) → `RentalStatus`**
    * **Protocol:** In-process method call
    * **Payload Changes:** `currentStatus.validateTransitionTo(RentalStatus.CANCELLED)` —
      throws `InvalidRentalStatusException` for any non-DRAFT/non-ACTIVE source.

---

## 5. Updated Interaction Sequence

### Happy path — valid transition

1. Caller (application service) obtains the current `RentalStatus` from the `Rental` aggregate.
2. Caller invokes `currentStatus.validateTransitionTo(targetStatus)`.
3. `RentalStatus` looks up `targetStatus` in the allowed-transitions set for `currentStatus`.
4. Target is found → method returns normally; caller proceeds with business logic.

### Unhappy path — invalid transition

1. Caller invokes `currentStatus.validateTransitionTo(targetStatus)`.
2. `RentalStatus` looks up `targetStatus` — not found in the allowed set.
3. `InvalidRentalStatusException` is thrown carrying both `currentStatus` and `targetStatus`.
4. Exception propagates to `RentalRestControllerAdvice`, which maps it to the appropriate HTTP
   error response.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Pure domain logic; no authentication surface involved.
* **Scale & Performance:** O(1) set-lookup; no I/O, no external dependencies. Zero measurable
  overhead.
