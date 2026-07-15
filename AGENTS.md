# BikeRental — Agent Guide

Equipment rental management system built as a **Spring Boot 4 / Spring Modulith modular monolith** with hexagonal
architecture.

---

## Architecture

8 business modules under `service/src/main/java/com/github/jenkaby/bikerental/`:

```
customer/  equipment/  tariff/   ← core, independent
users/                           ← user accounts & self-service (CRUD, password management)
identity/                        ← OAuth2/OIDC authorization server & Spring Security configs only
rental/    finance/              ← business (depends on core)
shared/                          ← cross-cutting kernel
```

**Module dependency direction: `identity → users` (never reversed).**

The `users` module owns the user domain: `User` aggregate, roles, account CRUD, self-service password change, and the
bootstrap admin runner. Its public API (root package only) exposes:
- `UserAuthFacade` — interface for identity to look up users without crossing module boundaries
- `UserAuthView` — read-only auth projection (id, username, passwordHash, roles, active, mustChangePassword)
- `SessionRevoker` — port interface implemented by `JdbcSessionRevoker` inside identity
- `JwtProperties` — `@ConfigurationProperties(prefix = "app.security.jwt")` record shared via allowed dependency

The `identity` module owns all Spring Security configuration: a Spring Authorization Server (OAuth 2.1 / OIDC) issuing
JWT access + refresh tokens (with PKCE public client support), a resource-server chain protecting `/api/**`, local
username/password + optional Google federation, and CORS on authorization server endpoints. It depends on `users` via
`UserAuthFacade` only — it never imports the `users` domain model directly. Other modules never import either auth
module — they receive the populated `SecurityContext` from the resource-server filter.

OIDC RP-initiated logout (`/connect/logout`) is **stateless**: `StatelessOidcLogoutAuthenticationConverter` feeds the
endpoint an anonymous principal so the provider skips the session-bound `sub`/`sid` checks (no `OidcSessionRegistry`,
no `sid` claim required), while the `post_logout_redirect_uri` allowlist still applies.
`StatelessOidcLogoutResponseHandler` invalidates the current session and redirects to the validated URI.

JWT access tokens include custom claims: `roles` (list), `must_change_password` (boolean), and a configurable user-id
claim (default `userId`). `must_change_password` is re-read from the DB on every token issuance via `UserAuthFacade`.

Access + ID tokens are RS256-signed. Provide a persistent RSA key pair via `app.security.jwt.private-key-location` /
`public-key-location` (otherwise an ephemeral key is generated per startup and tokens break on restart). The JWKSet
supports rotation: the active key signs (its `kid` is stamped on every token), and `app.security.jwt.previous-keys[]`
holds retired public keys so already-issued tokens still verify during the overlap window. Full instructions:
[docs/jwt-keys.md](docs/jwt-keys.md).

Module boundaries enforced by Spring Modulith. Each `package-info.java` carries `@ApplicationModule`. Cross-module
calls go through **Facade** classes only — never import another module's domain model directly.

### Layer structure per module (rental as canonical example)

```
rental/
  web/command/          ← @RestController, DTOs, MapStruct mapper
  web/query/            ← read-only controllers
  web/error/            ← @RestControllerAdvice (module-scoped)
  application/usecase/  ← interfaces (contracts)
  application/service/  ← @Service implementations
  application/mapper/   ← MapStruct mappers domain↔command/query
  domain/model/         ← pure aggregates, no framework imports
  domain/repository/    ← interfaces (ports)
  domain/service/       ← domain port interfaces + result types
  infrastructure/persistence/ ← JPA entities + adapter implements domain repo
  event/                ← domain events (records implementing BikeRentalEvent)
```

### Dependency Inversion in Domain

Domain ports are **interfaces** in `domain/service/`; the application layer provides implementations. Example:
`RentalDurationCalculator` (domain port) → `RentalDurationCalculatorImpl` (application service). Domain entities call
the port, never the implementation.

All domain events **must** implement `com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent`.

---

## Developer Workflows

### Infrastructure

```bash
docker compose -f ./docker/docker-compose.yaml up -d
```

### Run all tests

```bash
./gradlew test "-Dspring.profiles.active=test"
```

`test` profile **disables** Liquibase by default. Add `docker` profile in CI to enable it. For local runs against a
pre-migrated DB, leave `spring.liquibase.enabled=false` in `application-test.yaml`.

### Run unit tests locally

```bash
./gradlew :service:test "-Dspring.profiles.active=test"
```

Prefer to run individual test class by adding `--tests {testClassName}` for faster feedback.
Eg. `./gradlew :service:test "-Dspring.profiles.active=test" --tests TariffV2CommandControllerTest`

### Run component tests locally

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test" 
```

Assume that DB is spun up already and accepts connections.

### Run application locally

```bash
./gradlew :service:bootRun "-spring.profiles.active=local"
```

Requires `service/src/main/resources/application-local.properties` which referenced on the `nocommit.properties`(not
committed) with `DATASOURCE_URL`,
`DATASOURCE_USER`, `DATASOURCE_SECRET`.

### MapStruct compiler flags (set in `service/build.gradle`)

```
-Amapstruct.defaultComponentModel=spring
-Amapstruct.defaultInjectionStrategy=constructor
-Amapstruct.unmappedTargetPolicy=ERROR   ← build fails on unmapped fields
```

---

## Testing Conventions

Maintained as path-scoped rules — single source of truth:

- Unit / WebMvc tests (`service` module): [.claude/rules/unit-tests.md](.claude/rules/unit-tests.md)
- Component tests (`component-test` module): [.claude/rules/component-tests.md](.claude/rules/component-tests.md)

---

## Key Patterns & Conventions

### Error Responses

All error handlers return `ProblemDetail` with two mandatory extra properties:

- `correlationId` — from `MDC.get("correlationId")` (set by `CorrelationIdFilter`), fallback to generated UUID
- `errorCode` — string constant from `ErrorCodes` (e.g. `CONSTRAINT_VIOLATION`, `RESOURCE_NOT_FOUND`)

Validation errors additionally include an `errors` array of `ValidationError` records — `{field, code, params}`:

- `field` — offending property/parameter name (`null` for class-level/global constraints)
- `code` — `validation.<snake_case>` derived **automatically** from the constraint annotation's simple name (e.g.
  `@Size` → `validation.size`); the frontend maps it to localized text
- `params` — the constraint annotation's declared attributes minus `message`/`groups`/`payload` (e.g.
  `@Size(min=2, max=5)` → `{min: 2, max: 5}`), auto-extracted from the `ConstraintDescriptor` — no per-constraint code

`BaseValidationErrorMapper` (shared) does this centrally for all three Jakarta entry points
(`MethodArgumentNotValidException`, `HandlerMethodValidationException`, `ConstraintViolationException`). Every
`errorCode` is catalogued (description + example payload per code) in [docs/error-codes.md](docs/error-codes.md). When
adding a custom constraint or a new error code, follow
[.claude/rules/error-responses.md](.claude/rules/error-responses.md) — no handler/mapper changes are needed for
validation.

Module-scoped advice classes (e.g. `EquipmentRestControllerAdvice`) use
`@RestControllerAdvice(basePackages = "...")` with `@Order(Ordered.LOWEST_PRECEDENCE - 1)`.

### Cost Breakdown Codes

Each equipment line in a rental cost calculation carries a `calculationBreakdown` with a
`breakdownPatternCode` (i18n key the frontend localizes), a fallback `message`, and typed `params`. Every code
(description + `params` schema + example per pattern) is catalogued in
[docs/breakdown-codes.md](docs/breakdown-codes.md) — keep it in sync when changing a `BreakdownCostDetails`
subtype or a tariff's `calculateCost` message template.

### Time Types

| Type            | Usage                                                                    |
|-----------------|--------------------------------------------------------------------------|
| `Instant`       | Audit fields: `createdAt`, `updatedAt` (UTC). Use only for API requests. |
| `LocalDateTime` | Business time: `startedAt`, `expectedReturnAt`                           |
| `LocalDate`     | Date-only: `birthDate`, `validFrom`. Use only for API requests.          |

`InstantMapper` (shared) handles conversion between layers automatically via MapStruct.

### Configuration Properties

Typed config via `@ConfigurationProperties` records, auto-scanned from `BikeRentalApplication`. Key prefixes:
`app.rental`, `app.cors`, `app.security.jwt` (JWT signing keys / issuer / token TTLs, see `users.JwtProperties` and
[docs/jwt-keys.md](docs/jwt-keys.md)), `app` (default locale). Always inject via constructor, never `@Value`.

### Java Style

Hard constraints (zero inline comments, records for DTOs, constructor injection, immutability) are maintained in
[.claude/rules/java-style.md](.claude/rules/java-style.md).

---

## Key Files

| File                                                                | Purpose                                                                                                |
|---------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `overview.md`                                                       | a structured component overview document for this project                                              |
| `architecture.md`                                                   | Machine parsable an architecture of the solution                                                       |
| `requirements/`                                                     | A folder containing requirements folders/files with functional requirements and detailed implemntation |
| `service/src/test/java/.../support/web/ApiTest.java`                | WebMvc test base annotation                                                                            |
| `shared/web/advice/CoreExceptionHandlerAdvice.java`                 | Global exception handler reference                                                                     |
| `component-test/src/test/java/.../RunComponentTests.java`           | Cucumber suite runner                                                                                  |
| `users/UserAuthFacade.java`, `UserAuthView.java`, `SessionRevoker.java`, `JwtProperties.java` | Public API of the users module (root package only) |
| `identity/infrastructure/security/AuthorizationServerConfig.java`   | OAuth2/OIDC authorization server configuration                                                         |
| `identity/infrastructure/security/WebSecurityConfig.java`           | Resource server & login security filter chains                                                         |
