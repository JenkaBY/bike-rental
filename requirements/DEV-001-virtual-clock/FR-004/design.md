# System Design: FR-004 - DevTimeController Endpoints (dev/test only)

## 1. Architectural Overview

`DevTimeController` is the **only new production source file** introduced by this feature. It is a Spring MVC REST
controller that also acts as a `@Configuration` class, allowing it to declare the mutable `Clock` bean within its own
compilation unit. The class is restricted to the `dev` and `test` Spring profiles; on any other profile (e.g., `prod`)
the class is never instantiated and the `Clock` bean it declares is never registered.

The class structure in a single file:

- A private static inner class `SettableClock extends Clock` holds the atomic mutable state (described in FR-003).
- The constructor initialises the `SettableClock` instance.
- A `@Primary @Bean Clock clock()` method exposes the `SettableClock` as the active `Clock` bean, overriding the
  system-default bean from FR-002.
- Three HTTP handler methods implement `PUT /api/dev/time`, `GET /api/dev/time` (SSE), and `DELETE /api/dev/time`.
- The SSE emission interval is supplied via constructor injection from the property
  `app.dev.virtual-clock.sse-interval-ms` (default 1000 ms).

The GET endpoint streams Server-Sent Events using Spring MVC's `SseEmitter`. If no virtual time has been set the stream
falls back to real system time, so the endpoint is safe to open before any PUT call. All three endpoints are only
registered when the profile condition is met.

## 2. Impacted Components

* **`DevTimeController` (new file, `shared` module):** The sole new production file. Annotated
  `@Profile({"dev","test"})`, `@Configuration`, `@RestController`, `@RequestMapping("/api/dev/time")`. Contains the
  `SettableClock` inner class and the `@Primary @Bean Clock clock()` declaration. Depends on nothing beyond the JDK
  `java.time.Clock` API and Spring MVC.

## 3. Abstract Data Schema Changes

No database entities are introduced or changed. All state managed by these endpoints is held in `MutableVirtualClock`'s
in-memory atomic reference (FR-003).

## 4. Component Contracts & Payloads

* **Interaction: External HTTP Client → `DevTimeController` (PUT)**
    * **Protocol:** REST / HTTP PUT
    * **Path:** `PUT /api/dev/time`
    * **Payload Changes (Request):**
      ```
      Content-Type: application/json
      {
        "instant": "<ISO-8601 string, e.g. 2026-05-08T12:00:00Z>"
      }
      ```
        * `instant` — Required. An ISO-8601 datetime string. Must be parseable as an `Instant` (i.e., includes timezone
          offset or `Z`). An offset-local datetime without timezone designator must be rejected.
        * Missing body or missing `instant` field → `400 Bad Request` with standard `ProblemDetail` response (error code
          `CONSTRAINT_VIOLATION`, `errors` array listing the missing field).
        * Malformed datetime string → `400 Bad Request` (same format).
    * **Payload Changes (Response, happy path):**
      ```
      HTTP 200 OK
      Content-Type: application/json
      {
        "instant": "<the applied ISO-8601 instant, normalised to UTC, e.g. 2026-05-08T12:00:00Z>"
      }
      ```

* **Interaction: External HTTP Client → `DevTimeController` (GET / SSE)**
    * **Protocol:** HTTP GET with SSE streaming response
    * **Path:** `GET /api/dev/time`
    * **Response:**
      ```
      HTTP 200 OK
      Content-Type: text/event-stream
      ```
      Each SSE event payload:
      ```
      data: {"instant":"2026-05-08T12:00:01.123Z","fixed":true}
      ```
        * `instant` — current virtual or system time as ISO-8601 UTC string.
        * `fixed` — boolean; `true` when the clock is currently pinned by a prior PUT; `false` when falling back to
          system time.
        * Events are emitted at the interval configured by `app.dev.virtual-clock.sse-interval-ms` (default 1000 ms).
        * If no virtual time has been set, `fixed: false` and `instant` reflects real system time — the stream does not
          error.

* **Interaction: External HTTP Client → `DevTimeController` (DELETE)**
    * **Protocol:** REST / HTTP DELETE
    * **Path:** `DELETE /api/dev/time`
    * **Payload Changes (Request):** No request body.
    * **Payload Changes (Response):**
      ```
      HTTP 204 No Content
      ```
      Resets the virtual clock to system time. Idempotent — calling DELETE when no virtual time is set still returns
      `204`.

## 5. Updated Interaction Sequence

**Scenario A — Set virtual time:**

1. Client sends `PUT /api/dev/time` with `{"instant":"2026-05-08T12:00:00Z"}`.
2. `DevTimeController` validates the request body (field presence, ISO-8601 parseability).
3. Controller parses the string to `Instant`, calls `settableClock.setFixed(instant)`.
4. Controller responds `200 OK` with `{"instant":"2026-05-08T12:00:00Z"}`.
5. Any subsequent `clock.instant()` call anywhere in the application returns `2026-05-08T12:00:00Z`.

**Scenario B — Observe time via SSE:**

1. Client opens a long-lived GET to `/api/dev/time`.
2. `DevTimeController` creates an `SseEmitter` and schedules periodic emissions.
3. At each interval, the controller reads `settableClock.instant()` and `settableClock.isFixed()`.
4. Controller emits: `data: {"instant":"...","fixed":true/false}`.
5. Client receives the stream; the connection remains open until closed by client or server timeout.
6. If no virtual time was set, `fixed: false` and the instant reflects real system time.

**Scenario C — Reset virtual time:**

1. Client sends `DELETE /api/dev/time`.
2. `DevTimeController` calls `settableClock.reset()`.
3. Controller responds `204 No Content`.
4. Any subsequent `clock.instant()` call returns real system time.

**Error path — malformed PUT:**

1. Client sends `PUT /api/dev/time` with missing or unparseable `instant` field.
2. `DevTimeController` (or Spring validation) rejects the payload.
3. Controller returns `400 Bad Request` with a `ProblemDetail` body containing `errorCode: "CONSTRAINT_VIOLATION"` and
   an `errors` array describing the invalid field.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The controller is profile-gated (`dev` or `test` only). It must not be registered in production.
  No additional authentication is configured for these endpoints — profile isolation is the sole guard. This is
  consistent with the project's current security posture (no `SecurityFilterChain` configured).
* **Scale & Performance:** The SSE emitter must not block the servlet thread between emissions. Emissions should use an
  async scheduler (e.g., a background thread or Spring's `TaskScheduler`) so the server thread pool is not exhausted by
  idle SSE connections. The number of concurrent SSE consumers in `dev`/`test` is expected to be very low (one developer
  or one test suite at a time), so no advanced back-pressure is required.
