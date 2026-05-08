# System Design: FR-006 - Testing and Integration Guidance

## 1. Architectural Overview

This story defines the verification strategy for the single new production file (`DevTimeController`) and the `Clock`
bean it manages. Two test layers are involved:

1. **WebMvc slice tests** (`service` Gradle module) — isolate `DevTimeController` and verify HTTP contract, validation,
   and error responses without a running database. Follow the project's established `@ApiTest` pattern. Because
   `SettableClock` is a private inner class of the controller, there is no separate clock bean to mock; the controller's
   internal state is exercised directly by calling the endpoints under test.
2. **Component tests** (`component-test` Gradle module) — verify end-to-end behaviour with the full Spring context under
   the `test` profile, confirming the `Clock` bean from `DevTimeController` overrides the system default and that the
   SSE stream reflects mutations correctly.

No new test infrastructure is introduced; existing scaffolding (`@ApiTest`, `CucumberRunner`, Testcontainers) is reused
unchanged.

## 2. Impacted Components

* **`DevTimeControllerTest` (new, `service` test module):** WebMvc slice test covering the three `DevTimeController`
  endpoints — request validation (PUT bad payloads → 400), happy-path PUT (200 + confirmed body), DELETE (204
  idempotent), GET `Content-Type` verification. Uses `@ApiTest(controllers = DevTimeController.class)`. No
  `@MockitoBean` needed for the clock — the controller creates `SettableClock` internally and state changes are verified
  via subsequent endpoint calls.

* **Component test feature file (new, `component-test` module):** Cucumber feature covering: set virtual time via PUT,
  verify SSE emits `fixed: true`, reset via DELETE, verify SSE reverts to `fixed: false`. Activated with the `test`
  profile so `DevTimeController` and its `@Primary Clock` bean are in the context.

* **`RunComponentTests` (existing, `component-test` module):** No structural change — the new feature file is
  auto-discovered.

## 3. Abstract Data Schema Changes

No database entities are introduced or changed by this testing story.

## 4. Component Contracts & Payloads

* **Interaction: `@ApiTest` slice → `DevTimeController`**
    * **Protocol:** `MockMvc` (in-process HTTP simulation)
    * **Payload Changes (test inputs):**
        * Valid PUT body: `{"instant":"2026-05-08T12:00:00Z"}` → expect 200 with confirmed body.
        * PUT with missing `instant` field → expect 400, `ProblemDetail` with `errorCode: "CONSTRAINT_VIOLATION"`,
          `errors` array containing entry for `instant`.
        * PUT with non-parseable `instant` string (e.g., `"not-a-date"`) → expect 400.
        * PUT with body omitted entirely → expect 400.
        * DELETE → expect 204, no body.
        * GET → expect 200, `Content-Type: text/event-stream`.

* **Interaction: Component test → Running application**
    * **Protocol:** HTTP (real HTTP client over Testcontainers or local port)
    * **Payload Changes:** Same as above; SSE assertions additionally verify that the `fixed` field in emitted events
      transitions from `false` to `true` after a successful PUT.

## 5. Updated Interaction Sequence

**WebMvc test — validation error path:**

1. `@ApiTest` slice starts with `DevTimeController` under the `test` profile (no additional mocks required — the
   controller manages its own `SettableClock` state internally).
2. Test calls `PUT /api/dev/time` with `{"instant":"bad-value"}` via `MockMvc`.
3. `DevTimeController` (or Spring validation) returns `400 Bad Request`.
4. Test asserts `ProblemDetail` shape: `errorCode = CONSTRAINT_VIOLATION`, `errors[0].field = "instant"`.

**Component test — full round-trip:**

1. Application starts under `test` profile; `DevTimeController` and its `@Primary Clock` bean (`SettableClock`) are in
   context.
2. Test calls `DELETE /api/dev/time` → asserts 204 (ensure clean state).
3. Test opens SSE stream via `GET /api/dev/time`; asserts first event has `fixed: false`.
4. Test calls `PUT /api/dev/time` with `{"instant":"2026-01-01T00:00:00Z"}` → asserts 200 with
   `{"instant":"2026-01-01T00:00:00Z"}`.
5. Test reads next SSE event → asserts `fixed: true` and `instant: "2026-01-01T00:00:00Z"`.
6. Test calls `DELETE /api/dev/time` → asserts 204.
7. Test reads next SSE event → asserts `fixed: false`.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** Tests run under the `test` profile only; no authentication headers are sent because no
  `SecurityFilterChain` is configured. If security is added in a future sprint, these tests must be updated to include
  appropriate tokens.
* **Scale & Performance:** SSE assertions in component tests must use a polling / await strategy with a bounded
  timeout (e.g., Awaitility) rather than `Thread.sleep`, consistent with the project's test conventions. The SSE
  interval for test runs should be set to a low value (e.g., 200 ms via `APP_DEV_VIRTUAL_CLOCK_SSE_INTERVAL_MS=200` in
  the test environment) to keep test execution fast.
