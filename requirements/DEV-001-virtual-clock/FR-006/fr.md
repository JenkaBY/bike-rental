# User Story: FR-006 - Testing and integration guidance

## 1. Description

**As a** QA engineer / developer
**I want** guidance and tests that exercise the virtual clock and dev-time controller under the `test` profile
**So that** automated tests and component tests can reliably exercise time-dependent behaviour.

## 2. Context & Business Rules

* **Trigger:** Test runs that require a stable notion of current time.
* **Rules Enforced:**
    * Tests that require the mutable clock must run with the `test` profile active (e.g., `@ActiveProfiles("test")`).
    * WebMvc tests should exercise validation and error cases for the dev-time controller.
    * Component tests should use the `component-test` Gradle module and the `test` profile to ensure the mutable clock
      is available.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Tests should be deterministic and fast; avoid waiting longer than necessary for SSE ticks in unit
  tests.
* **Security/Compliance:** N/A.
* **Usability/Other:** Provide example test snippets or a single canonical WebMvc test for the controller.

## 4. Acceptance Criteria (BDD)

**Scenario 1: WebMvc validation tests**

* **Given** a WebMvc slice test with `test` profile
* **When** PUT /api/dev/time is called with malformed payload
* **Then** the controller responds with 400 Bad Request and an error description

**Scenario 2: Integration test confirms profile behaviour**

* **Given** an integration test boots the application with `@ActiveProfiles("test")`
* **When** the test injects the virtual clock and sets a value via the controller
* **Then** injected calls to the shared provider return the set value

## 5. Out of Scope

* Full test coverage for all time-dependent business logic — this story only provides tests and guidance for the clock
  and controller itself.
