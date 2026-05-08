# User Story: FR-001 - Virtual clock interface (shared)

## 1. Description

**As a** developer of domain logic
**I want to** have an injectable virtual time provider available from the shared infrastructure
**So that** code across modules can read a consistent, testable notion of "current time" (instead of calling system time
directly), making time-dependent logic deterministic in tests and during development.

## 2. Context & Business Rules

* **Trigger:** Any domain or application service needs the current time to compute durations, expiries, or other
  time-dependent behaviour.
* **Rules Enforced:**
    * The provider exposes a timezone-agnostic canonical instant for business logic.
    * Callers must be able to read the current instant without depending on dev-only implementation details.
    * The abstraction must not leak dev-only behaviour to production code; production must remain using a system-backed
      provider.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** `now()` must be fast and safe to call frequently by hot-path business code.
* **Security/Compliance:** N/A — read-only time provider.
* **Usability/Other:** The provider must be documented and simple to inject for other modules.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Provider available to modules (happy path)**

* **Given** the application is running under any profile
* **When** a module injects the virtual time provider and calls it for the current time
* **Then** the provider returns an Instant representing the current time (system time unless overridden in dev/test)

**Scenario 2: Dev/test override observed**

* **Given** the application runs with the `dev` or `test` profile and the virtual clock has been set by the dev-time
  controller
* **When** a module calls the virtual time provider
* **Then** the provider returns the configured virtual instant instead of real system time

## 5. Out of Scope

* How the provider is registered in the Spring context (bean configuration) — implementation detail.
* Controller endpoints that set/reset the clock (covered in a separate FR).
