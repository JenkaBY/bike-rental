# User Story: FR-004 - DevTimeController endpoints (dev/test only)

## 1. Description

**As a** developer or QA engineer
**I want to** control a virtual clock at runtime via HTTP endpoints available in `dev` and `test` profiles
**So that** I can set, observe (via a streaming feed), and reset the application's notion of current time for debugging
and test scenarios.

## 2. Context & Business Rules

* **Trigger:** A developer or automated test calls a REST endpoint to set or observe time.
* **Rules Enforced:**
    * Controller and related beans are only available when the `dev` or `test` profile is active.
    * PUT requests validate input; malformed payloads result in a 400 Bad Request.
    * A reset mechanism must restore system time behaviour.
    * SSE stream must use `text/event-stream` content type and deliver JSON payloads at configured intervals.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** SSE interval is configurable; default should be safely low (e.g., 1 second) to avoid unnecessary
  traffic.
* **Security/Compliance:** Endpoints are dev/test-only; no additional authentication is required for local development.
* **Usability/Other:** Responses should be machine-parseable JSON to help automation.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Set virtual time (happy path)**

* **Given** the app runs under `dev` or `test` profile
* **When** a client calls PUT /api/dev/time with a valid time payload
* **Then** the virtual clock is set to the provided time and the response confirms the applied value

**Scenario 2: SSE stream provides time ticks**

* **Given** the app runs under `dev` or `test` profile
* **When** a client opens GET /api/dev/time as an SSE stream
* **Then** the server sends periodic events (JSON) with the current time at the configured interval and
  `Content-Type: text/event-stream`

**Scenario 3: Reset virtual time**

* **Given** the virtual time has been set
* **When** a client requests a reset (explicit reset operation)
* **Then** the provider returns to system time behaviour immediately

## 5. Out of Scope

* Authentication/authorization beyond profile-based restriction.
* SSE client reconnection strategies — clients are expected to reconnect as needed.
