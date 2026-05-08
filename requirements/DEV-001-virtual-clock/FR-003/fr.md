# User Story: FR-003 - Mutable virtual clock for dev/test (shared)

## 1. Description

**As a** developer or QA engineer
**I want to** be able to set and reset a mutable virtual clock in `dev` and `test` environments
**So that** tests and development scenarios can exercise time-dependent behaviour deterministically.

## 2. Context & Business Rules

* **Trigger:** A developer or automated test instructs the virtual clock to a specific instant.
* **Rules Enforced:**
    * Mutable behaviour is available only when the application runs under `dev` or `test` profile.
    * Setting the virtual clock affects subsequent calls to the shared virtual time provider across modules.
    * A reset operation restores behaviour to real system time.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Setting or resetting the clock is not performance-critical but must be immediately visible to
  callers.
* **Security/Compliance:** Access to the controller that mutates the clock must be restricted by profile only (not by
  auth).
* **Usability/Other:** Clear behaviour for concurrent callers: a set() call affects all subsequent reads globally.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Set virtual time (happy path)**

* **Given** the app runs under `dev` or `test` and the mutable clock provider is active
* **When** a client sets the virtual time to `T`
* **Then** subsequent calls to the shared provider return `T` (or advance deterministically if configured to tick)

**Scenario 2: Reset to system time**

* **Given** the virtual time is set
* **When** the client resets the clock
* **Then** subsequent calls return real system time

## 5. Out of Scope

* How tests should assert time-dependent behaviour — only the provider and observable behaviour are specified here.
