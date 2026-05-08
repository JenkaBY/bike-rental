Initial user request

Date: 2026-05-08

Original request (developer-provided):

The new feature belongs to the `shared` module (or a dedicated `dev` module if you determine it is cleaner), since it is
infrastructure-level and not domain-specific.

- Configuration is done via `application.yml` with environment variables.

Feature to specify

A Dev Time Controller — a Spring MVC REST controller that exposes a virtual clock for development and testing purposes.

Endpoints

| Method | Path            | Description                                                                                                    |
|--------|-----------------|----------------------------------------------------------------------------------------------------------------|
| `PUT`  | `/api/dev/time` | Set the virtual clock to a specific point in time                                                              |
| `GET`  | `/api/dev/time` | Stream the current virtual time as SSE, updating periodically. The period is specified via the application.yml |

Key constraints to cover in the requirements

1. Profile restriction — the controller and all related beans must only be instantiated when the active Spring profile
   is `dev` or `test`. The application must start cleanly without these beans on any other profile (e.g. `prod`).
2. Virtual clock abstraction — the virtual clock must be injectable across other modules (via the `shared` module) so
   that domain logic (e.g. rental expiry checks, tariff calculations) can use it instead of `Instant.now()` or
   `LocalDateTime.now()`. Specify what the shared interface should look like.
3. PUT semantics — specify the exact request body format (field names, type, timezone handling), expected HTTP response
   codes, and validation rules (e.g. what happens if the body is missing or the date is malformed).
4. GET / SSE semantics — specify the SSE event format (field names, data shape, event interval), what `Content-Type` the
   response must use, and how the stream should behave if no virtual time has been set yet (fall back to real system
   time or return an error — make a reasoned decision).
5. Reset behaviour — specify whether there should be a way to reset the virtual clock back to real system time, and if
   so, how (separate endpoint, special sentinel value in PUT, etc.).
6. Module boundary compliance — explain how other modules should consume the virtual clock without violating Spring
   Modulith boundaries.
7. Testing considerations — note how the `test` profile activation should work in integration tests (e.g.
   `@ActiveProfiles("test")`).

Notes:

- The plan for requirements was approved by the product owner / requester during a clarifying Q&A.
