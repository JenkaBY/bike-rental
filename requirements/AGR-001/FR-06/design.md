# System Design: FR-06 — Activation Only Through Agreement Signing (Breaking)

## 1. Architectural Overview

Removes the legacy direct `DRAFT → ACTIVE` path. After this change the state machine is strictly
`DRAFT → AWAITING_SIGNATURE → ACTIVE → …`, and the only writer of the `AWAITING_SIGNATURE →
ACTIVE` transition is `CompleteSigningService` invoked via `RentalSigningFacade` (FR-02/FR-05).
This is a deletion-heavy change: no new capabilities, only closing the old door and migrating
tests that relied on it.

---

## 2. Impacted Components

* **`RentalStatus`**: transition map entry for `DRAFT` becomes `{AWAITING_SIGNATURE, CANCELLED}`
  (ACTIVE removed).
* **`Rental.activate(LocalDateTime)`**: deleted (superseded by `completeSigning`); `canBeActivated()`
  is kept only if still referenced by `prepareForSigning()` readiness logic — otherwise inline/rename
  to `isReadyForSigning()` (task author decides based on actual references).
* **`ActivateRentalService` / `ActivateRentalUseCase`**: deleted. `RentalLifecycleService` drops the
  `case ACTIVE` branch (falls into the existing `default` → error) and its constructor dependency.
* **`LifecycleStatus`** (web enum): remove `ACTIVE` → Jackson rejects `"ACTIVE"` at deserialization
  → standard 400 validation ProblemDetail (documented breaking change). OpenAPI text updated.
* **Deprecated JSON-Patch path (`UpdateRentalService` / patch mapper)**: remove/deactivate the
  `/status = ACTIVE` patch capability so activation cannot be smuggled through the deprecated
  endpoint (task author inspects `UpdateRentalService` for the status-patch branch and the
  `rental.activate` call sites).
* **`RentalEventMapper.toRentalStarted`**: retained — still used by `CompleteSigningService`.
* **WebMvc tests**: update lifecycle controller tests (ACTIVE no longer a valid enum value —
  becomes a bad-request case).
* **Component tests**: every scenario that activated a rental via
  `PATCH /lifecycles {ACTIVE}` (return, debt-settlement, add-equipment-to-active, cancel-active
  flows) migrates to a shared activation-through-signing step sequence:
  a reusable background/common step chain "an ACTIVE agreement template exists" (FR-03 steps) +
  "the rental is moved to AWAITING_SIGNATURE" (FR-02 step) + "the rental is signed" (FR-05 step).
  Prefer ONE shared common step (e.g. `the rental is activated via signing`) placed in
  `steps/common/` that composes existing FR-02..FR-05 steps' logic, so feature files change by a
  single line per scenario.

---

## 3. Abstract Data Schema Changes

None.

---

## 4. Component Contracts & Payloads

* `PATCH /api/rentals/{id}/lifecycles` with `{"status": "ACTIVE"}` → `400` standard validation
  body (unknown enum value). Allowed values: `AWAITING_SIGNATURE`, `DRAFT`, `CANCELLED`.
* `POST /api/rentals/awaiting-signature` (new) — request body `RentalForSigningRequest` (mirrors
  `RentalRequest` but `equipmentIds` is `@NotEmpty`). Creates the rental and moves it to
  `AWAITING_SIGNATURE` atomically (init-draft validation + `prepareForSigning` hold in one
  transaction). Responses: `201` `RentalResponse` in `AWAITING_SIGNATURE` carrying `version`;
  `400` validation (incl. empty `equipmentIds`); `404` customer/equipment; `409` equipment
  unavailable; `422` not ready / funds cannot be held. On any failure nothing is persisted (no
  orphan draft). Implemented by `InitRentalForSigningService` orchestrating
  `CreateOrUpdateDraftRentalUseCase` + `PrepareSigningUseCase`.
* No other payload changes.

---

## 5. Updated Interaction Sequence

1. Operator prepares rental → `AWAITING_SIGNATURE` (hold taken, FR-02). Either the two-step path
   (`POST /api/rentals` draft, then `PATCH /lifecycles {AWAITING_SIGNATURE}`) or, on the happy path,
   a single `POST /api/rentals/awaiting-signature` call that does both atomically.
2. Customer signs → agreement module persists signature and calls `completeSigning` → ACTIVE
   (FR-05).
3. Return/cancel/debt flows proceed exactly as before from ACTIVE.

---

## 6. Non-Functional Architecture Decisions

* **Sequencing:** merged strictly after FR-05 is deployed and the frontend has adopted
  prepare-signing + signing; the API break is limited to the lifecycle `ACTIVE` value.
* **Test strategy:** the suite must remain green purely by swapping the activation step in
  affected scenarios; any scenario asserting hold-at-activation semantics moves the assertion to
  the prepare-signing step (hold timing changed in FR-02, but the direct path kept old semantics
  until now — this FR is where those assertions migrate).
