# System Design: EQUIP-002/FR-04 — Disable Rental Event Listeners in Equipment Module

## 1. Architectural Overview

This story severs the only remaining runtime dependency from the equipment module back to the rental
module. Currently, `RentalEventListener` (inside the equipment module) consumes `RentalCreated`,
`RentalStarted`, `RentalCompleted`, and `RentalUpdated` domain events to keep `status_slug` in the
`equipments` table in sync with rental lifecycle state.

After this change, all four listener methods are commented out (not deleted). The `equipment` module
will have zero imports, zero event subscriptions, and zero runtime references to any type from the
`rental` module. Spring Modulith's module boundary verification will confirm a strictly one-way
dependency graph: rental → equipment.

As an accepted transitional consequence, the `equipments.status_slug` column becomes stale — it will
not be updated when rentals are created, started, or completed. This is explicitly documented as a
known state until the follow-up migration fully removes `status_slug` from the equipment module's
domain responsibility.

---

## 2. Impacted Components

* **`RentalEventListener` (equipment module — infrastructure/eventlistener):**
  All four `@ApplicationModuleListener`-annotated methods are commented out. The class and its
  `@Component` annotation are preserved so the listener can be re-enabled in a follow-up task without
  rewriting the file from scratch. A `TODO: [REFACTORING]` comment is placed above each disabled block.

* **`EquipmentFacadeImpl` / equipment module (indirect):**
  After this change, no class in the `equipment.*` package references any type from `rental.*` or
  `shared.domain.event.Rental*`. Spring Modulith `@ApplicationModuleTest` will verify this.

* **`status_slug` column (data store — transitional):**
  Becomes read-only / stale. Existing `GET /api/equipments` responses will continue to return
  `statusSlug` values but they will reflect the last-updated state prior to listener disable, not the
  current rental state.

---

## 3. Abstract Data Schema Changes

No schema changes. The `status_slug` column is not dropped or altered in this story.

* **Known transitional inconsistency:** `equipments.status_slug` will no longer track rental lifecycle
  after this story is deployed. The column retains its last written value indefinitely until the
  follow-up migration (separate ticket) removes the column or replaces its semantics.

---

## 4. Component Contracts & Payloads

* **Interaction removed: `SpringApplicationEventPublisher` → `RentalEventListener`**
    * **Protocol:** Spring Modulith `@ApplicationModuleListener` (in-process event)
    * **Change:** All four subscriptions (`RentalCreated`, `RentalStarted`, `RentalCompleted`,
      `RentalUpdated`) are disabled. The events continue to be published by the rental module
      and consumed by other modules (e.g., `finance`), but the equipment module no longer
      receives or acts on them.

No new contracts are introduced by this story.

---

## 5. Updated Interaction Sequence

**Before this story — rental creation triggers equipment status update:**

1. `CreateRentalService` publishes `RentalCreated` event.
2. `RentalEventListener.onRentalCreated` receives the event.
3. Listener calls `UpdateEquipmentUseCase` to set `status_slug = 'RESERVED'` for each equipment ID.

**After this story — equipment status is no longer updated by rental events:**

1. `CreateRentalService` publishes `RentalCreated` event.
2. Spring Modulith dispatches the event; no listener in the equipment module responds.
3. `equipments.status_slug` for affected rows remains unchanged (stale).

The same suppression applies to `RentalStarted`, `RentalCompleted`, and `RentalUpdated` events.

**Module verification sequence (startup):**

1. Application context initialises.
2. Spring Modulith scans all modules for cross-module type references.
3. Equipment module: no import of `rental.*` types found → verification passes.
4. Application starts normally.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** N/A.
* **Scale & Performance:** Removing four event listener invocations from every rental lifecycle
  transition reduces transactional work per rental operation. Minor positive performance impact.
* **Data consistency:** `status_slug` staleness is an accepted, documented transitional state.
  Any UI or client logic that currently relies on `status_slug` to determine equipment availability
  must be aware of this limitation during the transition period. Availability queries must use
  the new `GET /rentals/available-equipments` endpoint (RENTAL-002 FR-06) instead.
