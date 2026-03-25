# BikeRental — Equipment Rental Management System

---

## Summary

- Single-deployable modular monolith that manages the full lifecycle of equipment rental operations: customer profiles,
  equipment catalogue, tariff configuration, rental activation, prepayment, return, and payment recording.
- Two runtime components: one Spring Boot application (`bike-rental-api`) and one PostgreSQL database (
  `bike-rental-db`).
- Communication style: all business logic is synchronous in-process; the application is the sole external HTTP API
  surface; no inter-service HTTP calls or external message broker.
- Internal cross-module communication uses Spring Modulith Facade interfaces (synchronous, in-process) and Spring
  `ApplicationEventPublisher` (asynchronous, in-process, transactional).
- Deployment model: Docker container on Render.com (free tier) with a Render-managed PostgreSQL instance; local stack
  via Docker Compose.

---

## Design Approach

- **Domain-Driven Design (DDD):** The system is structured around explicit bounded contexts (customer, equipment,
  tariff, rental, finance), each modelled as an independent Spring Modulith module. Each module owns its domain
  aggregates, repositories (ports), domain services, and application use-cases. Cross-context dependencies are resolved
  exclusively through published Facade interfaces — direct access to another module's domain model is forbidden.
- **Hexagonal Architecture (Ports & Adapters):** Each module separates its domain and application layers from
  infrastructure concerns. Domain ports are interfaces; adapters (JPA, REST controllers) implement or consume those
  ports and live outside the domain layer.
- **Domain Events:** State changes that cross bounded-context boundaries are propagated via domain events (records
  implementing `BikeRentalEvent`) published through the application event bus, keeping modules loosely coupled.

---

## Technology Stack

- CATEGORY: Runtime
  TECHNOLOGY: Java 21 (Eclipse Temurin / Amazon Corretto in CI)
  USED_BY: bike-rental-api

- CATEGORY: API Framework
  TECHNOLOGY: Spring Boot 4+
  USED_BY: bike-rental-api

- CATEGORY: Modularity
  TECHNOLOGY: Spring Modulith 2+
  USED_BY: bike-rental-api

- CATEGORY: Persistence / ORM
  TECHNOLOGY: Spring Data JPA + Hibernate (bundled with Spring Boot)
  USED_BY: bike-rental-api

- CATEGORY: Database Migration
  TECHNOLOGY: Liquibase (bundled via spring-boot-starter-liquibase)
  USED_BY: bike-rental-api

- CATEGORY: Database
  TECHNOLOGY: PostgreSQL 15
  USED_BY: bike-rental-api

- CATEGORY: API Documentation
  TECHNOLOGY: SpringDoc OpenAPI (springdoc-openapi-starter-webmvc-ui 3.0.2)
  USED_BY: bike-rental-api

- CATEGORY: Object Mapping
  TECHNOLOGY: MapStruct 1.6.3
  USED_BY: bike-rental-api

- CATEGORY: Code Generation
  TECHNOLOGY: Lombok (bundled with Spring Boot)
  USED_BY: bike-rental-api

- CATEGORY: UUID Generation
  TECHNOLOGY: uuid-creator 6.1.1 (UUID v7)
  USED_BY: bike-rental-api

- CATEGORY: Container
  TECHNOLOGY: Docker (multi-stage build, eclipse-temurin:21-jre-alpine runtime image)
  USED_BY: bike-rental-api

- CATEGORY: Container Orchestration (local)
  TECHNOLOGY: Docker Compose (Compose v2)
  USED_BY: bike-rental-api, postgres

- CATEGORY: Cloud Platform
  TECHNOLOGY: Render.com (Web Service + managed PostgreSQL, free tier)
  USED_BY: bike-rental-api, bike-rental-db

- CATEGORY: CI/CD
  TECHNOLOGY: GitHub Actions
  USED_BY: bike-rental-api

- CATEGORY: Test Framework
  TECHNOLOGY: JUnit 6+, Cucumber 7+, Testcontainers 2+, Mockito
  USED_BY: service (unit + WebMVC tests), component-test (BDD integration tests)

- CATEGORY: Auth
  TECHNOLOGY: None active — spring-boot-starter-security and spring-boot-starter-oauth2-resource-server are declared as
  dependencies but no SecurityFilterChain is configured; all endpoints are open.
  USED_BY: NONE

---

## Services

SERVICE_NAME: bike-rental-api
TYPE: API
PURPOSE: Exposes REST endpoints for all rental management operations and hosts the full business domain as a Spring
Modulith modular monolith.
OVERVIEW_REF: `overview.md`
ENTRY_POINT: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java`
EXPOSES:

- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: POST /api/rentals, POST /api/rentals/draft, PATCH /api/rentals/{id}, POST
  /api/rentals/{id}/prepayments, POST /api/rentals/return, GET /api/rentals, GET /api/rentals/{id}
  DESCRIPTION: Full rental lifecycle management — creation, update, prepayment, return, and query
- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: POST /api/equipments, PUT /api/equipments/{id}, PATCH /api/equipments/{id}/status, GET
  /api/equipments, GET /api/equipments/{id}
  DESCRIPTION: Equipment catalogue management — registration, update, status transition, and search
- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: POST /api/customers, PUT /api/customers/{id}, GET /api/customers, GET /api/customers/{id}
  DESCRIPTION: Customer profile management — creation, update, search by phone, and lookup
- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: GET /api/tariffs, GET /api/tariffs/{id}, GET /api/tariffs/v2, GET /api/tariffs/v2/{id}, GET
  /api/tariffs/pricing-types
  DESCRIPTION: Tariff catalogue, active tariff query by equipment type, and tariff auto-selection
- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: POST /api/payments, GET /api/payments
  DESCRIPTION: Standalone payment recording and query
- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: GET /actuator/health, GET /actuator/info, GET /actuator/metrics
  DESCRIPTION: Spring Boot Actuator operational endpoints
- PROTOCOL: HTTP
  ENDPOINT_OR_TOPIC: GET /swagger-ui/index.html, GET /v3/api-docs
  DESCRIPTION: SpringDoc OpenAPI UI and machine-readable API spec
  CONSUMES:
- PROTOCOL: TCP
  ENDPOINT_OR_TOPIC: jdbc:postgresql://postgres:5432/bikerental (local) / jdbc:postgresql://<render-host>:<port>/<db> (
  production)
  FROM_SERVICE: bike-rental-db
  DESCRIPTION: Reads and writes all persistent domain state via Spring Data JPA

---

SERVICE_NAME: bike-rental-db
TYPE: Database
PURPOSE: Stores all persistent state for the BikeRental system: rentals, equipment, customers, tariffs, and payments.
OVERVIEW_REF: NONE
ENTRY_POINT: NONE (managed service — PostgreSQL 15 container or Render-managed instance)
EXPOSES:

- PROTOCOL: TCP
  ENDPOINT_OR_TOPIC: port 5432 (local Docker) / Render internal endpoint (production)
  DESCRIPTION: PostgreSQL wire protocol for JDBC connections
  CONSUMES:
- PROTOCOL: NONE

---

## Service Communication Map

INTERACTION_ID: 1
FROM_SERVICE: bike-rental-api
TO_SERVICE: bike-rental-db
PROTOCOL: TCP
CHANNEL: JDBC — jdbc:postgresql://postgres:5432/bikerental (local) / Render internal URL (production)
DIRECTION: Request-Response
PURPOSE: Persists and retrieves all domain aggregates (Rental, Equipment, Customer, Tariff, Payment) through Spring Data
JPA repositories
CONTRACT_REF: `service/src/main/resources/db/changelog/db.changelog-master.xml` (Liquibase schema)

---

## Shared Infrastructure

INFRA_NAME: bike-rental-db
TYPE: Database
USED_BY_SERVICES: bike-rental-api
PURPOSE: Sole persistent store for all bounded-context data across the modular monolith.
CONFIG_REF: `docker/docker-compose.yaml` (local), `deployment.md` (Render setup)

INFRA_NAME: GitHub Actions CI/CD
TYPE: Other
USED_BY_SERVICES: bike-rental-api
PURPOSE: Runs build, unit tests, and component tests on every push; promotes to `dev-deploy` branch on success to
trigger Render deployment.
CONFIG_REF: `.github/workflows/build.yml`, `.github/workflows/deploy.yml`

---

## Folder Structure

- PATH: `service/`
  ROLE: Service
  PURPOSE: Main Spring Boot modular monolith — all six business modules (customer, equipment, tariff, rental, finance,
  shared) and their infrastructure adapters.

- PATH: `component-test/`
  ROLE: Test
  PURPOSE: Cucumber BDD integration test suite that runs against a live PostgreSQL database; depends on the `service`
  Gradle project directly.

- PATH: `docker/`
  ROLE: Infrastructure
  PURPOSE: Docker Compose stack for local development — defines `postgres` and `bike-rental-app` services.

- PATH: `service/src/main/resources/db/changelog/`
  ROLE: Config
  PURPOSE: Liquibase changeset files that version and apply the database schema.

- PATH: `gradle/`
  ROLE: Tool
  PURPOSE: Gradle version catalog (`libs.versions.toml`) declaring all dependency versions and the Gradle wrapper.

- PATH: `.github/workflows/`
  ROLE: Config
  PURPOSE: GitHub Actions pipelines — CI test (`build.yml`), CD deploy (`deploy.yml`), and dependency graph (
  `dependency-graph.yml`).

- PATH: `.github/skills/`
  ROLE: Tool
  PURPOSE: Bundled AI agent skill definitions for project-specific tasks (Liquibase, Cucumber, controller tests, etc.).

- PATH: `.github/prompts/`
  ROLE: Tool
  PURPOSE: AI agent prompt templates for recurring analysis tasks (component-overview, project-architecture).

- PATH: `scripts/`
  ROLE: Tool
  PURPOSE: Developer utility scripts (e.g., Dependabot PR merging).

---

## Architectural Patterns

- PATTERN: Modular Monolith
  SCOPE: Entire system
  EVIDENCE: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java` (
  `@Modulithic(sharedModules = "shared")`); every module has `package-info.java` with `@ApplicationModule`

- PATTERN: Hexagonal Architecture (Ports and Adapters)
  SCOPE: All six modules within `service/`
  EVIDENCE: `rental/domain/repository/RentalRepository.java` (domain port);
  `rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java` (JPA adapter); same pattern repeated for
  customer, equipment, tariff, and finance modules

- PATTERN: Facade (cross-module boundary contract)
  SCOPE: Inter-module calls within bike-rental-api
  EVIDENCE: `equipment/EquipmentFacade.java`, `customer/CustomerFacade.java`, `tariff/TariffFacade.java`,
  `finance/FinanceFacade.java` — interfaces at module root; callers import only these, never internal domain types

- PATTERN: CQRS (Command/Query separation)
  SCOPE: All modules within bike-rental-api
  EVIDENCE: Separate `*CommandController` and `*QueryController` pairs in every module's `web/` layer; separate
  `*UseCase` interfaces for commands and queries

- PATTERN: Event-Driven (in-process domain events)
  SCOPE: rental → equipment status propagation within bike-rental-api
  EVIDENCE: `shared/infrastructure/messaging/SpringApplicationEventPublisher.java` (producer);
  `equipment/infrastructure/eventlistener/RentalEventListener.java` (consumer using `@ApplicationModuleListener`);
  events: `RentalCreated`, `RentalStarted`, `RentalCompleted`, `RentalUpdated`

- PATTERN: Repository Pattern
  SCOPE: All five business modules within bike-rental-api
  EVIDENCE: Domain repository interfaces under `*/domain/repository/`; JPA adapters under
  `*/infrastructure/persistence/adapter/`

- PATTERN: Continuous Deployment via branch promotion
  SCOPE: CI/CD pipeline
  EVIDENCE: `.github/workflows/deploy.yml` — force-pushes HEAD to `dev-deploy` branch on every successful CI run;
  Render.com watches that branch and rebuilds automatically

---

## Security Topology

- AUTHN_AUTHZ: None active. `spring-boot-starter-security` and `spring-boot-starter-oauth2-resource-server` are declared
  in `gradle/libs.versions.toml` but no `SecurityFilterChain` bean is configured. All `/api/**` endpoints accept
  unauthenticated requests.
  LOCATION: `gradle/libs.versions.toml`, `service/build.gradle`

- TRUST_BOUNDARIES:
    - External clients → bike-rental-api: no authentication enforced; the boundary exists at the network level only (
      Render.com public HTTPS URL).
    - bike-rental-api → bike-rental-db: connection credentials (`DATASOURCE_USER`, `DATASOURCE_SECRET`) injected via
      environment variables; in production these are Render-managed secrets; in local Docker Compose they are hardcoded
      as `postgres/postgres`.

- KNOWN_RISKS:
    - All REST endpoints are publicly accessible without authentication or authorisation.
    - Local Docker Compose hardcodes database credentials (`postgres`/`postgres`) — safe only for local development.
    - `CORS_ALLOWED_ORIGINS` is injected via environment variable; an incorrect value (e.g., `*`) would open the API to
      cross-origin access from any domain.
    - No secret-scanning gate exists in CI; accidental credential commits would not be detected automatically.
    - `spring.liquibase.drop-first=true` is set in `docker-compose.yaml` for the `app` profile — destructive if
      accidentally used against a non-local database.

---

## Testing Strategy

1. There're 3 types of tests:

- component tests in `component-test` module for endpoints testing only positive scenarios. Mandatory part of testing.
  Use a proper skill.
- simple unit tests in `service` module for service, application, and modules testing. Skip this kind of tests until
  it's said explicitly
- WebMvc tests in `service` module for controllers testing both positive and negative scenarios (only request
  validation).

2. use assertJ for assertions
3. awaitility for async operations
4. Mock external dependencies using Mockito
5. To run test the 'test' profile must be activated. Simple add `-Dspring.profiles.active=test` to Gradle test command.
6. Execute only one unit test per time:

- for service `./gradlew :service:test "-Dspring.profiles.active=test" --test {TestClassName}`

7. Component tests are executed all together `./gradlew :service:test "-Dspring.profiles.active=test"`

---

## Deployment Topology

- DEPLOYMENT_MODEL: Modular Monolith (single deployable JAR packaged as a Docker image)

- CONTAINER_RUNTIME: Docker (multi-stage build — Gradle JDK 21 builder → eclipse-temurin:21-jre-alpine runtime)

- ORCHESTRATION:
    - Local: Docker Compose (`docker/docker-compose.yaml`)
    - Production: Render.com managed container platform (no Kubernetes or Helm)

- SERVICES_AND_PORTS:
    - `bike-rental-app`: container port 80 (SERVER_PORT=80), host port 8080 (local Docker Compose); production URL
      provided by Render.com on HTTPS
    - `postgres`: container port 5432, host port 5432 (local Docker Compose); Render-managed PostgreSQL internal URL in
      production

- CONFIG_REFS:
    - `service/Dockerfile` — multi-stage Docker image build for bike-rental-api
    - `docker/docker-compose.yaml` — local full-stack environment (postgres + bike-rental-app)
    - `.github/workflows/build.yml` — CI pipeline (build, unit tests, component tests)
    - `.github/workflows/deploy.yml` — CD pipeline (branch promotion to `dev-deploy`)
    - `deployment.md` — step-by-step Render.com setup guide

---

## Assumptions

- ASSUMPTION: The `component-test` Gradle module is a test-only project and is not a separately deployed service; it
  compiles against `service` directly.
  BASIS: `component-test/build.gradle` declares `testImplementation project(':service')` with no server bootstrap of its
  own.

- ASSUMPTION: No external message broker (RabbitMQ, Kafka, etc.) is in use or planned; all event propagation is
  in-process via Spring Modulith.
  BASIS: No broker client libraries found in `gradle/libs.versions.toml`; `SpringApplicationEventPublisher` wraps
  Spring's `ApplicationEventPublisher`; no broker defined in `docker-compose.yaml`.

- ASSUMPTION: Authentication and authorisation are planned but not yet implemented; the declared OAuth2 and Security
  dependencies are placeholders for a future sprint.
  BASIS: `spring-boot-starter-security` and `spring-boot-starter-oauth2-resource-server` appear in
  `gradle/libs.versions.toml` but no `SecurityFilterChain`, `@EnableWebSecurity`, or security configuration class was
  found during analysis of the source tree.

- ASSUMPTION: The `TariffV2Facade` / `TariffV2FacadeImpl` represents a final solution of tariff API; v1 is deprecated.
  BASIS: The `TariffFacade.java` is deprecated. The `TariffV2Facade.java` exists at the tariff module root alongside
  matching `*Impl` classes and `TariffV2QueryController`;

