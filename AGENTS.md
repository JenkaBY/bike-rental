# BikeRental — Agent Guide

Equipment rental management system built as a **Spring Boot 4 / Spring Modulith modular monolith** with hexagonal
architecture. Read `memory-bank/` files before starting any task (see `.github/copilot-instructions.md`).
Follow [the instructions](.github/instructions/agent-skills.instructions.md) to use agents for specific tasks.

---

## Architecture

6 business modules under `service/src/main/java/com/github/jenkaby/bikerental/`:

```
customer/  equipment/  tariff/   ← core, independent
rental/    finance/              ← business (depends on core)
shared/                          ← cross-cutting kernel
```

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
./gradlew test -Dspring.profiles.active=test
```

`test` profile **disables** Liquibase by default. Add `docker` profile in CI to enable it. For local runs against a
pre-migrated DB, leave `spring.liquibase.enabled=false` in `application-test.yaml`.

### Run unit tests locally

```bash
./gradlew :service:test -Dspring.profiles.active=test
```

### Run component tests locally

```bash
./gradlew :component-test:test -Dspring.profiles.active=test
```

Assume that DB is spun up already and accepts connections.

### Run application locally

```bash
./gradlew :service:bootRun --args='--spring.profiles.active=local'
```

Requires `service/src/main/resources/application-local.properties` (not committed) with `DATASOURCE_URL`,
`DATASOURCE_USER`, `DATASOURCE_SECRET`.

### MapStruct compiler flags (set in `service/build.gradle`)

```
-Amapstruct.defaultComponentModel=spring
-Amapstruct.defaultInjectionStrategy=constructor
-Amapstruct.unmappedTargetPolicy=ERROR   ← build fails on unmapped fields
```

---

## Testing Conventions

### Unit / WebMvc tests — `service` module

WebMvc tests use the `@ApiTest` meta-annotation (`support/web/ApiTest.java`):

```java

@ApiTest(controllers = RentalCommandController.class)
class RentalCommandControllerTest { ...
}
```

`@ApiTest` activates profile `test`, imports `TestingAppConfig` (which registers `CorsProperties` via
`@EnableConfigurationProperties` and imports `UuidCreatorAdapter` + `BaseValidationErrorMapper`). Always use
`@ApiTest` for controller slice tests — never bare `@WebMvcTest`.

Use `@MockitoBean` (not `@MockBean`) for Spring-managed dependencies in WebMvc tests.

### Component (integration) tests — `component-test` module

Cucumber features in `component-test/src/test/resources/features/`. Runner: `RunComponentTests`. Tag `@skip` to
suppress; tag `@run` + swap the filter line to run a single scenario during development.

Component tests cover happy paths only; request-validation negative cases belong in WebMvc tests.

### Assertions

**AssertJ only** — `assertThat(actual).as("description")...`

---

## Key Patterns & Conventions

### Error Responses

All error handlers return `ProblemDetail` with two mandatory extra properties:

- `correlationId` — from `MDC.get("correlationId")` (set by `CorrelationIdFilter`), fallback to generated UUID
- `errorCode` — string constant from `ErrorCodes` (e.g. `CONSTRAINT_VIOLATION`, `RESOURCE_NOT_FOUND`)

Validation errors additionally include an `errors` array of `{field, message}` objects.

Module-scoped advice classes (e.g. `EquipmentRestControllerAdvice`) use
`@RestControllerAdvice(basePackages = "...")` with `@Order(Ordered.LOWEST_PRECEDENCE - 1)`.

### Time Types

| Type            | Usage                                          |
|-----------------|------------------------------------------------|
| `Instant`       | Audit fields: `createdAt`, `updatedAt` (UTC)   |
| `LocalDateTime` | Business time: `startedAt`, `expectedReturnAt` |
| `LocalDate`     | Date-only: `birthDate`, `validFrom`            |

`InstantMapper` (shared) handles conversion between layers automatically via MapStruct.

### Configuration Properties

Typed config via `@ConfigurationProperties` records, auto-scanned from `BikeRentalApplication`. Key prefixes:
`app.rental`, `app.cors`, `app` (default locale). Always inject via constructor, never `@Value`.

### No Comments in Code

The project convention is **zero inline comments**. Use expressive naming and self-documenting structures.

---

## Key Files

| File                                                      | Purpose                                      |
|-----------------------------------------------------------|----------------------------------------------|
| `memory-bank/activeContext.md`                            | Current sprint focus and next priority tasks |
| `memory-bank/tasks/_index.md`                             | All task statuses                            |
| `memory-bank/systemPatterns.md`                           | Full pattern catalogue with code examples    |
| `docs/backend-architecture.md`                            | Module diagram and data flow                 |
| `service/src/test/java/.../support/web/ApiTest.java`      | WebMvc test base annotation                  |
| `shared/web/advice/CoreExceptionHandlerAdvice.java`       | Global exception handler reference           |
| `component-test/src/test/java/.../RunComponentTests.java` | Cucumber suite runner                        |

