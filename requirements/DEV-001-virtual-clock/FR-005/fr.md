# User Story: FR-005 - Configuration & application properties

## 1. Description

**As a** developer or ops engineer
**I want** configuration for the dev-time controller and SSE behaviour to be defined in `application.yml` and
overridable via environment variables
**So that** behaviour can be tuned for local development, CI, and component tests without code changes.

## 2. Context & Business Rules

* **Trigger:** Application configuration is read on startup and used by the dev-time controller and virtual clock
  implementation.
* **Rules Enforced:**
    * Configuration keys belong under a single logical prefix to avoid collisions.
    * Configuration must support environment variable overrides for CI and containerized runs.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Property access should be cached where appropriate; configuration binding must be safe at startup.
* **Security/Compliance:** No secrets or sensitive values are stored here.
* **Usability/Other:** Provide a default value to allow reasonable behaviour without extra configuration.

## 4. Acceptance Criteria (BDD)

**Scenario 1: SSE interval configured via application.yml**

* **Given** the application has a property value for the dev clock SSE interval
* **When** the dev-time controller serves an SSE stream
* **Then** it emits events at the configured interval

**Scenario 2: Environment override**

* **Given** an environment variable is set to override the SSE interval
* **When** the application starts
* **Then** the overridden value is used for the SSE interval

## 5. Out of Scope

* Complex feature flags or runtime configuration dashboards.
