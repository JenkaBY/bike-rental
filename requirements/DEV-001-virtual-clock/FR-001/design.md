# System Design: FR-001 - Clock Port (shared)

## 1. Architectural Overview

The `shared` module is the cross-cutting kernel of the modular monolith (`@Modulithic(sharedModules = "shared")`),
making its public Spring beans visible to every bounded-context module. The clock abstraction for this feature is *
*`java.time.Clock`** — a JDK abstract class that is already designed as a testable, injectable time source. No custom
interface is introduced.

A `Clock` bean is registered in the shared infrastructure configuration so that all modules can inject it. By calling
`clock.instant()` instead of `Instant.now()`, or `LocalDateTime.now(clock)` instead of `LocalDateTime.now()`, business
services become deterministic under tests and controllable in dev environments. This story covers only the decision that
`Clock` is the shared port and that a default system-time bean is provided; the mutable implementation and its mutation
endpoints are described in FR-003 and FR-004. **No new production source file is created by this story.**

## 2. Impacted Components

* **`shared` (Shared Kernel Module):** An existing shared infrastructure configuration class gains a single
  `@Bean Clock clock()` method returning `Clock.systemUTC()`. This is the default, always-available bean. It is
  overridden in `dev`/`test` profiles by the primary `Clock` bean declared in `DevTimeController` (FR-004). No new file
  is created.

## 3. Abstract Data Schema Changes

No persistent entities are added or modified. `Clock` holds no persisted state.

## 4. Component Contracts & Payloads

* **Interaction: Business Service → `Clock` bean**
    * **Protocol:** In-process Java method call (Spring dependency injection)
    * **Payload:** Consumers call the standard JDK `Clock` API:
        * `clock.instant()` — returns the current `Instant` (UTC).
        * `LocalDateTime.now(clock)` / `ZonedDateTime.now(clock)` — returns the current date-time in the desired zone.
    * No new methods or types are introduced.

## 5. Updated Interaction Sequence

1. A business service (e.g., `ReturnEquipmentService`) declares a `Clock` constructor parameter.
2. At runtime the Spring context supplies the active `Clock` bean — system-backed on non-dev/test profiles, mutable on
   `dev`/`test` (FR-003, FR-004).
3. The service calls `clock.instant()` to obtain the current time.
4. The clock implementation satisfies the call; the service continues unchanged.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `Clock` is a read-only port from the perspective of business services. Mutation is exclusively
  through the profile-gated HTTP controller (FR-004).
* **Scale & Performance:** `Clock.systemUTC().instant()` is a constant-time native call with no shared mutable state.
  The mutable implementation (FR-003) uses a lock-free atomic reference, keeping the read path equivalent in cost.
