# System Design: FR-02 — Rental AWAITING_SIGNATURE Status & Signing Public API

## 1. Architectural Overview

The rental lifecycle gains an intermediate `AWAITING_SIGNATURE` state between `DRAFT` and `ACTIVE`.
The existing `PATCH /api/rentals/{id}/lifecycles` endpoint is extended with two new target
statuses; no new endpoints are created. Fund holding moves to the moment the rental enters
`AWAITING_SIGNATURE` (the direct `DRAFT → ACTIVE` path keeps its own hold until FR-06 removes it —
each path holds exactly once).

For the future agreement module, rental exposes a public API in its root package (the Modulith
"module API" layer), following the `FinanceFacade` pattern: interface + package-private
implementation + record DTOs + public exceptions.

```
PATCH /lifecycles {AWAITING_SIGNATURE} → PrepareSigningService  → holdFunds + status change
PATCH /lifecycles {DRAFT}              → CancelSigningService   → releaseHold + status change
agreement (FR-05) → RentalSigningFacade.getSigningSnapshot / completeSigning (in-process)
```

---

## 2. Impacted Components

### Domain (`rental/domain/`)

* **`RentalStatus`**: new constant `AWAITING_SIGNATURE`. Transition map becomes:
  `DRAFT → {ACTIVE, CANCELLED, AWAITING_SIGNATURE}`, `AWAITING_SIGNATURE → {ACTIVE, DRAFT}`,
  others unchanged.
* **`Rental`** — three new methods, symmetric with `activate()`:
    * `prepareForSigning()` — validates transition `DRAFT → AWAITING_SIGNATURE` via
      `status.validateTransitionTo`, then reuses the readiness rules of `canBeActivated()`
      (customerId, non-empty equipments, plannedDuration, estimatedCost) throwing
      `RentalNotReadyForActivationException` with the missing fields; sets status; does NOT touch
      `startedAt`/costs.
    * `cancelSigning()` — validates `AWAITING_SIGNATURE → DRAFT`, sets status back. Equipment
      items remain `ASSIGNED` (they were never activated).
    * `completeSigning(LocalDateTime startedAt)` — requires current status `AWAITING_SIGNATURE`
      (throws `InvalidRentalStatusException` otherwise); sets `status = ACTIVE`,
      `startedAt`, `expectedReturnAt = startedAt + plannedDuration`, activates equipment items —
      mirroring the body of `activate()` without the DRAFT guard.
    * **`updateEquipments(...)` gains the missing DRAFT-only guard** (currently unchecked): first
      statement `if (status != DRAFT) throw new InvalidRentalStatusException(status, DRAFT);` —
      this closes composition editing (PUT fast path) in `AWAITING_SIGNATURE` and any other state.
      `selectCustomer`/`setPlannedDuration`/`addEquipment`/`replaceEquipments` already enforce DRAFT.

### Application (`rental/application/`)

* **`PrepareSigningUseCase`** *(new interface, `usecase/`)* — `Rental execute(PrepareSigningCommand(Long rentalId, String operatorId))`.
* **`PrepareSigningService`** *(new, `service/`)* — `@Transactional`; load rental (404 via
  `ResourceNotFoundException`), `rental.prepareForSigning()`, then if
  `rental.getEstimatedCost().isPositive()` → `financeFacade.holdFunds(customerRef, rentalRef,
  estimatedCost, operatorId)` (mirrors `ActivateRentalService`), save, return. No event published
  (the rental has not started).
* **`CancelSigningUseCase` / `CancelSigningService`** *(new)* — `@Transactional`; load rental,
  `rental.cancelSigning()`, then `if (financeFacade.hasHold(rentalRef)) financeFacade.releaseHold(rentalRef, operatorId)`, save, return.
* **`CompleteSigningService`** *(new, `service/`, package-private)* — `@Transactional`; used only
  by the facade impl. Loads rental, compares `rental.getVersion()` with `expectedVersion`
  (mismatch → `RentalSigningVersionMismatchException`), converts `signedAt` (`Instant`) to
  `LocalDateTime` via the system zone (same convention as `LocalDateTime.now(clock)` elsewhere),
  calls `rental.completeSigning(startedAt)`, saves, publishes `RentalStarted` via the existing
  `EventPublisher`/`RentalEventMapper` (exchange `rental-events`), exactly like
  `ActivateRentalService`. Does NOT hold funds (already held at prepare).
* **`RentalLifecycleService`** — switch gains:
  `case AWAITING_SIGNATURE -> prepareSigningUseCase.execute(...)`,
  `case DRAFT -> cancelSigningUseCase.execute(...)`.

### Module API (`rental/` root package)

* **`RentalSigningFacade`** *(new public interface)*:
  ```java
  RentalSigningSnapshot getSigningSnapshot(Long rentalId);
  void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt);
  ```
* **`RentalSigningSnapshot`** *(new public record)*: `Long rentalId, Long version, UUID customerId,
  Duration plannedDuration, BigDecimal estimatedCost, List<EquipmentItem> equipments` with nested
  record `EquipmentItem(Long equipmentId, String equipmentUid, String equipmentTypeSlug, BigDecimal estimatedCost)`.
* **`RentalNotAwaitingSignatureException`** *(new public exception)*: thrown by
  `getSigningSnapshot` when the rental status is not `AWAITING_SIGNATURE`; carries rentalId and
  current status (consumed by the agreement module's advice in FR-05 → 409
  `agreement.signing.rental_not_awaiting_signature`).
* **`RentalSigningVersionMismatchException`** *(new public exception)*: thrown by
  `completeSigning` on fencing mismatch (FR-05 advice → 409
  `agreement.signing.rental_version_mismatch`).
* **`RentalSigningFacadeImpl`** *(new package-private class in root)*: reads the aggregate via
  `RentalRepository`; missing rental → shared `ResourceNotFoundException` (existing global 404);
  wrong status → `RentalNotAwaitingSignatureException`; maps aggregate → snapshot (per-unit
  estimated costs from `RentalEquipment`); delegates `completeSigning` to `CompleteSigningService`.
  Exceptions live in the root package so that the agreement module may reference them without
  crossing module internals.

### Web (`rental/web/command/`)

* **`LifecycleStatus`** enum: add `AWAITING_SIGNATURE`, `DRAFT`.
* **`RentalCommandController.updateLifecycle`**: unchanged code path (`RentalStatus.valueOf` covers
  the new names); OpenAPI description updated.

---

## 3. Abstract Data Schema Changes

None (status is a plain VARCHAR without constraint; `version` added in FR-01).

---

## 4. Component Contracts & Payloads

* **HTTP Client → `PATCH /api/rentals/{id}/lifecycles`**
    * Request: `{"status": "AWAITING_SIGNATURE" | "DRAFT" | "ACTIVE" | "CANCELLED", "operatorId": "..."}`
    * `200` → `RentalResponse` (with `version` — the fencing token for signing).
    * `422 rental.status.invalid` — transition not allowed from the current status (existing
      contract of this endpoint; note: the original spec draft said 409 for a dedicated endpoint,
      but reusing lifecycles keeps its established 422 semantics).
    * `422 rental.activation.not_ready` — draft incomplete (missing customer/equipment/duration/cost).
    * `404` — rental not found.

* **agreement (FR-05) → `RentalSigningFacade`** (in-process)
    * `getSigningSnapshot`: returns `RentalSigningSnapshot`; throws `ResourceNotFoundException` |
      `RentalNotAwaitingSignatureException`.
    * `completeSigning`: void; throws `ResourceNotFoundException` |
      `RentalSigningVersionMismatchException` | `InvalidRentalStatusException` |
      `ObjectOptimisticLockingFailureException` (lost race with cancel-signing).

* **rental → `FinanceFacade`**: `holdFunds` at prepare-signing; `releaseHold` at cancel-signing
  (guarded by `hasHold`). `ActivateRentalService` (direct path) keeps its hold until FR-06.

---

## 5. Updated Interaction Sequence

### Prepare signing (happy path)

1. `PATCH /lifecycles {AWAITING_SIGNATURE, operatorId}` → `RentalLifecycleService` →
   `PrepareSigningService`.
2. Rental loaded (`version = N`); `prepareForSigning()` validates readiness and flips status.
3. `holdFunds` for `estimatedCost` (skipped when zero).
4. Save → `version = N+1` in response. `startedAt` remains null.

### Cancel signing → re-prepare

1. `PATCH /lifecycles {DRAFT}` → `CancelSigningService` → `cancelSigning()` + `releaseHold` → save
   (`version = N+2`).
2. Operator edits composition (allowed again in DRAFT), re-prepares → new hold, `version = N+3`.
3. A stale signing tab holding `version = N+1` will be rejected by FR-05's fencing check.

### Complete signing (invoked by agreement, FR-05)

1. Agreement calls `completeSigning(rentalId, expectedVersion, signedAt)` inside ITS transaction.
2. Version fencing check → mismatch → `RentalSigningVersionMismatchException`.
3. `rental.completeSigning(startedAt)` → status ACTIVE, `startedAt = signedAt`, equipment
   activated; save increments version; `RentalStarted` published.
4. Concurrent cancel-signing commits first → save throws
   `ObjectOptimisticLockingFailureException` → agreement transaction rolls back (signature is not
   persisted).

---

## 6. Non-Functional Architecture Decisions

* **Module boundaries:** all new cross-module surface lives in the rental root package
  (Modulith API layer); agreement (FR-03+) will declare `allowedDependencies = {"shared",
  "customer", "rental"}`. `ApplicationModules.verify()` stays green.
* **Exactly-once hold:** hold happens at `DRAFT → AWAITING_SIGNATURE` (new flow) or at direct
  activation (legacy flow, removed in FR-06); `completeSigning` never holds. Cancel from `ACTIVE`
  keeps releasing the hold as today.
* **Testing:** Cucumber component tests extend `features/rental/rental-lifecycle.feature` (or a
  new `rental-signing-lifecycle.feature`) covering scenarios 1–5 of the FR; finance hold
  assertions reuse existing finance steps. `getSigningSnapshot`/`completeSigning` are covered
  end-to-end by FR-05's signing component tests (no REST surface in this FR; project rule forbids
  unit tests on domain/service classes). WebMvc test updates for the widened `LifecycleStatus`
  validation.
