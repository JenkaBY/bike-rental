# BikeRental — Agent Guide

Equipment rental management system built as a **Spring Boot 4 / Spring Modulith modular monolith** with hexagonal
architecture.

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

### Time Types

| Type            | Usage                                                                    |
|-----------------|--------------------------------------------------------------------------|
| `Instant`       | Audit fields: `createdAt`, `updatedAt` (UTC). Use only for API requests. |
| `LocalDateTime` | Business time: `startedAt`, `expectedReturnAt`                           |
| `LocalDate`     | Date-only: `birthDate`, `validFrom`. Use only for API requests.          |

`InstantMapper` (shared) handles conversion between layers automatically via MapStruct.

### Configuration Properties

Typed config via `@ConfigurationProperties` records, auto-scanned from `BikeRentalApplication`. Key prefixes:
`app.rental`, `app.cors`, `app` (default locale). Always inject via constructor, never `@Value`.

### Java Style

Hard constraints (zero inline comments, records for DTOs, constructor injection, immutability) are maintained in
[.claude/rules/java-style.md](.claude/rules/java-style.md).

---

## Key Files

| File                                                      | Purpose                                                                                                |
|-----------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `overview.md`                                             | a structured component overview document for this project                                              |
| `architecture.md`                                         | Machine parsable an architecture of the solution                                                       |
| `requirements/`                                           | A folder containing requirements folders/files with functional requirements and detailed implemntation |
| `service/src/test/java/.../support/web/ApiTest.java`      | WebMvc test base annotation                                                                            |
| `shared/web/advice/CoreExceptionHandlerAdvice.java`       | Global exception handler reference                                                                     |
| `component-test/src/test/java/.../RunComponentTests.java` | Cucumber suite runner                                                                                  |
