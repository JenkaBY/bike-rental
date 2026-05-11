# User Story: FR-04 — Comment Out Rental Event Listeners in Equipment Module

## 1. Description

**As a** developer performing this module boundary refactoring,
**I want to** comment out all rental lifecycle event listeners inside the equipment module,
**So that** the equipment module has zero runtime knowledge of the rental module,
enforcing the correct one-way dependency direction (rental → equipment only).

## 2. Context & Business Rules

* **Trigger:** This story is part of the refactoring phase. It must be implemented together with or after
  FR-01 through FR-03 so that the equipment module is complete before severing the event dependency.
* **Rules Enforced:**
    - The file `RentalEventListener.java` (in the equipment module's infrastructure event listener package)
      must have all `@ApplicationModuleListener`-annotated methods commented out — not deleted.
    - A `TODO` comment must be placed at the top of each commented-out method block, with the following
      content (or equivalent):
      ```
      // TODO: [REFACTORING] Re-enable after full migration of availability tracking to rental module.
      //       Ticket: EQUIP-002 / RENTAL-002 follow-up.
      ```
    - The class itself and its Spring `@Component` annotation are NOT removed; only the listener method
      bodies and their annotations are commented out.
    - No other code in the equipment module may import, reference, or publish any type from the rental
      module after this change.
    - `status_slug` in the `equipments` table will become stale (not updated on rental events) after this
      change. This is an **accepted transitional state** documented in this ticket.
    - The application must start and pass Spring Modulith module verification without reporting any
      equipment → rental dependency.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No impact — listeners are removed from the event processing path.
* **Security/Compliance:** N/A.
* **Usability/Other:**
    - All existing tests that assert equipment status changes driven by rental events must be updated or
      skipped with a `@Disabled("EQUIP-002: listener disabled during refactoring")` annotation.
    - The commenting out must be a single atomic commit so the stale-status window is minimal.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Application starts cleanly after listener disable**

* **Given** all rental event listener methods in the equipment module are commented out
* **When** the application starts
* **Then** no startup error is thrown; Spring Modulith module verification passes

**Scenario 2: No equipment → rental dependency remains**

* **Given** the listener methods are commented out
* **When** a static import / package scan analysis is performed (e.g., Spring Modulith `@ApplicationModuleTest`)
* **Then** no type from any `rental.*` package is referenced by any class in the `equipment.*` package

**Scenario 3: Rental lifecycle event no longer changes equipment status**

* **Given** an equipment in state `AVAILABLE` (status_slug)
* **When** a `RentalCreated` event is published
* **Then** the equipment's `status_slug` is NOT changed to `RESERVED` (the listener is inactive)
* **And** no exception is thrown by the application

**Scenario 4: TODO comment is present on each disabled listener method**

* **Given** the source file is reviewed
* **When** each previously active `@ApplicationModuleListener` method is inspected
* **Then** a `// TODO: [REFACTORING]` comment is present above each commented-out block

## 5. Out of Scope

* Re-enabling the listeners — this is the explicit goal of the follow-up migration ticket.
* Updating `status_slug` via an alternative mechanism — deferred to the follow-up task.
* Deleting the `RentalEventListener` class — it is preserved for easy re-enablement.
* Updating the `equipment_statuses` lookup table or its transitions — out of scope.
