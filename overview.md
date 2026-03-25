# BikeRental — Equipment Rental Management System

---

## Summary

- Modular monolith REST API that manages the full lifecycle of equipment rental: customer registration, equipment
  catalogue, tariff selection, rental activation, prepayment, equipment return, and payment recording.
- Primary technologies: Java 21, Spring Boot 4+, Spring Modulith 2+, Spring Data JPA, PostgreSQL, Liquibase, MapStruct,
  Lombok, SpringDoc OpenAPI 3.0.2.
- Six bounded-context modules: `customer`, `equipment`, `tariff`, `rental`, `finance`, `shared`.
- Cross-module calls are made exclusively through published Facade interfaces; domain events are propagated via Spring's
  `ApplicationEventPublisher` with `@ApplicationModuleListener` consumers.
- Deployed as a Docker container on Render.com; CI/CD defined in `service/Dockerfile`.

---

## Projects and Folder Map

- PATH: `service/`
  PURPOSE: Main Spring Boot application containing all six business modules and shared infrastructure.
  ENTRY_FILES: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java`

- PATH: `component-test/`
  PURPOSE: Cucumber BDD integration test suite running against a live database.
  ENTRY_FILES: `component-test/src/test/java/.../RunComponentTests.java`

- PATH: `docker/`
  PURPOSE: Docker Compose configuration for spinning up PostgreSQL infrastructure locally.
  ENTRY_FILES: `docker/docker-compose.yaml`

- PATH: `service/src/main/resources/db/changelog/`
  PURPOSE: Liquibase database migration changesets.
  ENTRY_FILES: `db/changelog/db.changelog-master.xml`

- PATH: `gradle/`
  PURPOSE: Gradle version catalog and wrapper.
  ENTRY_FILES: `gradle/libs.versions.toml`

---

## Components

---

COMPONENT_NAME: BikeRentalApplication
TYPE: Gateway
PURPOSE: Spring Boot bootstrap class that activates the modular monolith and component-property scanning.
RESPONSIBILITIES:

- Bootstraps the Spring application context
- Declares `shared` as a shared Spring Modulith module via `@Modulithic(sharedModules = "shared")`
- Triggers `@ConfigurationPropertiesScan` for all typed config records
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java`
  CALLS:
- NONE
  CALLED_BY:
- JVM entry point (external)

---

COMPONENT_NAME: RentalCommandController
TYPE: API
PURPOSE: Exposes HTTP command endpoints for the rental lifecycle under `/api/rentals`.
RESPONSIBILITIES:

- Accepts POST `/api/rentals` to create an active rental (Fast Path)
- Accepts POST `/api/rentals/draft` to create an empty draft rental
- Accepts PATCH `/api/rentals/{id}` to update a draft via JSON Patch (RFC 6902)
- Accepts POST `/api/rentals/{id}/prepayments` to record a prepayment
- Accepts POST `/api/rentals/return` to process equipment return and complete the rental
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
  CALLS:
- CreateRentalUseCase — to create an active rental or draft
- UpdateRentalUseCase — to apply JSON Patch updates and activate a rental
- RecordPrepaymentUseCase — to record a prepayment against a rental
- ReturnEquipmentUseCase — to process equipment return
  CALLED_BY:
- External HTTP clients (frontend, API consumers)

---

COMPONENT_NAME: RentalQueryController
TYPE: API
PURPOSE: Exposes HTTP read-only endpoints for retrieving rental data under `/api/rentals`.
RESPONSIBILITIES:

- Returns paginated list of rentals filtered by status
- Returns a single rental by ID
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalQueryController.java`
  CALLS:
- FindRentalsUseCase — to retrieve paginated rental list
- GetRentalByIdUseCase — to retrieve a single rental
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: CreateRentalService
TYPE: Service
PURPOSE: Orchestrates creation of a fully-configured active rental or an empty draft.
RESPONSIBILITIES:

- Validates that the referenced customer exists
- Validates that all requested equipment IDs exist and are available
- Auto-selects tariff per equipment type and rental duration
- Calculates estimated cost per equipment unit
- Persists the new Rental aggregate with its RentalEquipment children
- Publishes `RentalCreated` domain event after successful save
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`
  CALLS:
- CustomerFacade — to look up and validate the customer
- EquipmentFacade — to look up equipment by IDs
- TariffFacade — to auto-select tariff and calculate estimated cost per equipment
- RequestedEquipmentValidator — to validate equipment count and availability
- RentalRepository — to persist the Rental aggregate
- SpringApplicationEventPublisher — to publish RentalCreated
  CALLED_BY:
- RentalCommandController

---

COMPONENT_NAME: UpdateRentalService
TYPE: Service
PURPOSE: Applies JSON Patch operations to a draft rental, including rental activation (DRAFT → ACTIVE).
RESPONSIBILITIES:

- Applies patch fields: `customerId`, `equipmentIds`, `duration`, `status`
- Validates new equipment is available when equipment list changes
- Re-selects tariffs and recalculates estimated costs on equipment change
- Transitions rental status to ACTIVE on `status=ACTIVE` patch, recording `startedAt`
- Enforces prepayment existence before activation
- Publishes `RentalStarted` event on activation; `RentalUpdated` event on draft update
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/UpdateRentalService.java`
  CALLS:
- RentalRepository — to load and persist the Rental aggregate
- CustomerFacade — to validate customer reference on patch
- EquipmentFacade — to validate and fetch equipment references on patch
- TariffFacade — to re-select tariff and recalculate cost on equipment change
- FinanceFacade — to check prepayment existence before activation
- RequestedEquipmentValidator — to validate equipment availability
- SpringApplicationEventPublisher — to publish RentalStarted or RentalUpdated
  CALLED_BY:
- RentalCommandController

---

COMPONENT_NAME: ReturnEquipmentService
TYPE: Service
PURPOSE: Processes partial or full equipment return, calculates final cost, records additional payment, and completes
the rental when all equipment is returned.
RESPONSIBILITIES:

- Validates rental is in ACTIVE status
- Delegates actual-duration calculation to RentalDurationCalculator
- Calls TariffFacade to calculate final cost per returned equipment
- Accumulates total cost including previously returned equipment costs
- Calls FinanceFacade to record additional payment when final cost exceeds prepayment
- Marks all returned equipment as RETURNED and closes the Rental when all returned
- Publishes `RentalCompleted` event when rental is fully closed
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`
  CALLS:
- RentalRepository — to load and save the Rental aggregate
- RentalDurationCalculator — to compute billable minutes
- TariffFacade — to calculate final cost per equipment
- FinanceFacade — to record additional payment if required
- SpringApplicationEventPublisher — to publish RentalCompleted
  CALLED_BY:
- RentalCommandController

---

COMPONENT_NAME: RecordPrepaymentService
TYPE: Service
PURPOSE: Records a prepayment for a rental that is in DRAFT status.
RESPONSIBILITIES:

- Validates rental exists and is in DRAFT status
- Validates that the prepayment amount meets the minimum required
- Delegates payment recording to FinanceFacade
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RecordPrepaymentService.java`
  CALLS:
- RentalRepository — to load the Rental aggregate
- FinanceFacade — to record the prepayment
  CALLED_BY:
- RentalCommandController

---

COMPONENT_NAME: FindRentalsService
TYPE: Service
PURPOSE: Retrieves a paginated list of rentals filtered by status.
RESPONSIBILITIES:

- Delegates paged query to RentalRepository
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/FindRentalsService.java`
  CALLS:
- RentalRepository — to fetch paginated rentals
  CALLED_BY:
- RentalQueryController

---

COMPONENT_NAME: GetRentalByIdService
TYPE: Service
PURPOSE: Retrieves a single rental by its numeric ID.
RESPONSIBILITIES:

- Delegates lookup to RentalRepository and throws ResourceNotFoundException when absent
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/GetRentalByIdService.java`
  CALLS:
- RentalRepository — to look up a rental by ID
  CALLED_BY:
- RentalQueryController

---

COMPONENT_NAME: RentalDurationCalculatorImpl
TYPE: Service
PURPOSE: Implements the domain port RentalDurationCalculator — computes billable minutes rounded up to the configured
time increment.
RESPONSIBILITIES:

- Rounds actual duration up to the nearest 5-minute (configurable) increment
- Exposes billable minutes and raw actual duration to callers
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalDurationCalculatorImpl.java`
  CALLS:
- RentalProperties — to read `app.rental.time-increment` configuration value
  CALLED_BY:
- ReturnEquipmentService (via RentalDurationCalculator port on Rental aggregate)

---

COMPONENT_NAME: RequestedEquipmentValidator
TYPE: Utility
PURPOSE: Validates that the list of requested equipment IDs matches fetched results and that all units are in AVAILABLE
status.
RESPONSIBILITIES:

- Checks that the number of fetched equipment records equals the number requested
- Checks that each fetched equipment unit is in AVAILABLE status, throwing EquipmentNotAvailableException otherwise
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/validator/RequestedEquipmentValidator.java`
  CALLS:
- NONE
  CALLED_BY:
- CreateRentalService
- UpdateRentalService

---

COMPONENT_NAME: RentalRepositoryAdapter
TYPE: Repository
PURPOSE: JPA adapter that implements the domain RentalRepository port for persisting and querying Rental aggregates.
RESPONSIBILITIES:

- Persists Rental with cascaded RentalEquipment children via JPA
- Queries rentals by ID, status (paginated), and UUID
- Maps between JPA entities and domain objects via RentalJpaMapper
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`
  CALLS:
- RentalJpaRepository — Spring Data JPA repository
  CALLED_BY:
- CreateRentalService
- UpdateRentalService
- ReturnEquipmentService
- RecordPrepaymentService
- FindRentalsService
- GetRentalByIdService

---

COMPONENT_NAME: EquipmentCommandController
TYPE: API
PURPOSE: Exposes HTTP command endpoints for equipment catalogue management under `/api/equipments`.
RESPONSIBILITIES:

- Accepts POST `/api/equipments` to register a new equipment unit
- Accepts PUT `/api/equipments/{id}` to update an equipment unit
- Accepts PATCH `/api/equipments/{id}/status` to transition equipment status
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/command/EquipmentCommandController.java`
  CALLS:
- CreateEquipmentUseCase — to create equipment
- UpdateEquipmentUseCase — to update equipment
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: EquipmentQueryController
TYPE: API
PURPOSE: Exposes read-only HTTP endpoints for equipment queries under `/api/equipments`.
RESPONSIBILITIES:

- Returns paginated, filterable equipment list
- Returns equipment by ID, UID, or serial number
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/EquipmentQueryController.java`
  CALLS:
- SearchEquipmentsUseCase — to query equipment with filters
- GetEquipmentByIdUseCase — to fetch a single equipment by ID
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: EquipmentFacadeImpl
TYPE: Service
PURPOSE: Implements EquipmentFacade — the only cross-module entry point into the equipment module for other modules.
RESPONSIBILITIES:

- Exposes `findById(Long)` to locate a single equipment record
- Exposes `findByIds(List<Long>)` to batch-locate equipment records
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/equipment/EquipmentFacadeImpl.java`
  CALLS:
- GetEquipmentByIdService — for single-ID lookup
- GetEquipmentByIdsService — for batch lookup
  CALLED_BY:
- CreateRentalService
- UpdateRentalService

---

COMPONENT_NAME: RentalEventListener
TYPE: Consumer
PURPOSE: Listens for rental domain events and updates equipment status in response.
RESPONSIBILITIES:

- On `RentalCreated`: marks equipment as RESERVED
- On `RentalStarted`: marks equipment as RENTED
- On `RentalCompleted`: marks returned equipment as AVAILABLE
- On `RentalUpdated` (draft cancelled): marks equipment as AVAILABLE; on draft updated re-assigns RESERVED/AVAILABLE
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`
  CALLS:
- EquipmentRepository — to load equipment records by IDs
- UpdateEquipmentUseCase — to apply the new status
  CALLED_BY:
- SpringApplicationEventPublisher (via Spring Modulith @ApplicationModuleListener)

---

COMPONENT_NAME: CustomerCommandController
TYPE: API
PURPOSE: Exposes HTTP command endpoints for customer management under `/api/customers`.
RESPONSIBILITIES:

- Accepts POST `/api/customers` to create a new customer profile
- Accepts PUT `/api/customers/{id}` to update an existing customer profile
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/web/command/CustomerCommandController.java`
  CALLS:
- CreateCustomerUseCase — to create a customer
- UpdateCustomerUseCase — to update a customer
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: CustomerQueryController
TYPE: API
PURPOSE: Exposes HTTP read endpoints for customer data under `/api/customers`.
RESPONSIBILITIES:

- Returns paginated customer list with optional phone-number search
- Returns a single customer by ID
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryController.java`
  CALLS:
- CustomerQueryService — to search or retrieve customers
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: CustomerFacadeImpl
TYPE: Service
PURPOSE: Implements CustomerFacade — the only cross-module entry point into the customer module.
RESPONSIBILITIES:

- Exposes `findById(UUID)` to locate a customer by UUID
- Exposes `findByPhone(String)` to locate a customer by phone number
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/CustomerFacadeImpl.java`
  CALLS:
- CustomerQueryService — to delegate lookups
  CALLED_BY:
- CreateRentalService
- UpdateRentalService

---

COMPONENT_NAME: TariffQueryController
TYPE: API
PURPOSE: Exposes HTTP read endpoints for tariff data under `/api/tariffs`.
RESPONSIBILITIES:

- Returns all tariffs (paginated)
- Returns a single tariff by ID
- Returns active tariffs filtered by equipment type
- Returns an auto-selected tariff for a given equipment type and rental duration
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/TariffQueryController.java`
  CALLS:
- GetTariffByIdUseCase — to fetch a tariff by ID
- GetAllTariffsUseCase — to list all tariffs
- GetActiveTariffsByEquipmentTypeUseCase — to list active tariffs by type
- SelectTariffForRentalUseCase — to select the best tariff for a rental
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: TariffFacadeImpl
TYPE: Service
PURPOSE: Implements TariffFacade — the only cross-module entry point into the tariff module.
RESPONSIBILITIES:

- Exposes `findById(Long)` to look up a tariff
- Exposes `selectTariff(equipmentTypeSlug, duration, rentalDate)` for auto-selection
- Exposes `calculateRentalCost(tariffId, duration)` for estimated cost at creation time
- Exposes `calculateRentalCost(tariffId, actualDuration, billableMinutes, plannedDuration)` for final cost at return
  time
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffFacadeImpl.java`
  CALLS:
- GetTariffByIdService — for tariff lookup
- SelectTariffForRentalService — for tariff auto-selection
- CalculateRentalCostService — for cost estimation
- RentalCostCalculationService — for final cost calculation including forgiveness rule
  CALLED_BY:
- CreateRentalService
- UpdateRentalService
- ReturnEquipmentService

---

COMPONENT_NAME: SelectTariffForRentalService
TYPE: Service
PURPOSE: Selects the cheapest active tariff matching a given equipment type, rental date, and duration.
RESPONSIBILITIES:

- Loads all active tariffs for the equipment type slug
- Selects the tariff period (HALF_HOUR / HOUR / DAY) based on duration
- Picks the tariff with the minimum price for the selected period, valid on the rental date
- Throws SuitableTariffNotFoundException when no match exists
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/SelectTariffForRentalService.java`
  CALLS:
- GetActiveTariffsByEquipmentTypeUseCase — to obtain active tariff candidates
  CALLED_BY:
- TariffFacadeImpl

---

COMPONENT_NAME: CalculateRentalCostService
TYPE: Service
PURPOSE: Calculates rental cost including overtime forgiveness rule for final cost at return time.
RESPONSIBILITIES:

- Loads tariff by ID
- Selects the billing period based on actual duration
- Applies forgiveness strategy (7-minute overtime threshold)
- Returns detailed RentalCost with base cost, overtime charge, and total
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/CalculateRentalCostService.java`
  CALLS:
- GetTariffByIdUseCase — to load tariff details
  CALLED_BY:
- TariffFacadeImpl

---

COMPONENT_NAME: PaymentCommandController
TYPE: API
PURPOSE: Exposes HTTP command endpoint for recording payments under `/api/payments`.
RESPONSIBILITIES:

- Accepts POST `/api/payments` to record a payment against a rental
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/PaymentCommandController.java`
  CALLS:
- RecordPaymentUseCase — to record the payment
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: PaymentQueryController
TYPE: API
PURPOSE: Exposes HTTP read endpoints for payment data under `/api/payments`.
RESPONSIBILITIES:

- Returns payments associated with a given rental ID
- Returns a single payment by ID
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/PaymentQueryController.java`
  CALLS:
- GetPaymentsByRentalIdService — to list payments by rental
- GetPaymentByIdService — to fetch a single payment
  CALLED_BY:
- External HTTP clients

---

COMPONENT_NAME: FinanceFacadeImpl
TYPE: Service
PURPOSE: Implements FinanceFacade — the only cross-module entry point into the finance module.
RESPONSIBILITIES:

- Exposes `recordPrepayment(...)` to create a PREPAYMENT record
- Exposes `recordAdditionalPayment(...)` to create an ADDITIONAL_PAYMENT record
- Exposes `hasPrepayment(rentalId)` to check whether a prepayment exists
- Exposes `getPrepayment(rentalId)` to retrieve an existing prepayment
- Exposes `getPayments(rentalId)` to list all payments for a rental
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
  CALLS:
- RecordPaymentService — to persist new payments
- GetPaymentsByRentalIdService — to query payments
  CALLED_BY:
- UpdateRentalService
- ReturnEquipmentService
- RecordPrepaymentService

---

COMPONENT_NAME: RecordPaymentService
TYPE: Service
PURPOSE: Persists a new payment record for a rental with a generated receipt number.
RESPONSIBILITIES:

- Generates a unique receipt number via ReceiptNumberGenerationService
- Persists the Payment domain object via PaymentRepository
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordPaymentService.java`
  CALLS:
- ReceiptNumberGenerationService — to generate a unique receipt number
- PaymentRepository — to persist the payment
  CALLED_BY:
- FinanceFacadeImpl

---

COMPONENT_NAME: SpringApplicationEventPublisher
TYPE: Producer
PURPOSE: Implements the EventPublisher port — wraps Spring's ApplicationEventPublisher to publish domain events
in-process.
RESPONSIBILITIES:

- Publishes domain event objects to all registered Spring Modulith listeners within a mandatory transaction
- Logs the destination and message payload
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/messaging/SpringApplicationEventPublisher.java`
  CALLS:
- Spring ApplicationEventPublisher — to dispatch the domain event
  CALLED_BY:
- CreateRentalService
- UpdateRentalService
- ReturnEquipmentService

---

COMPONENT_NAME: CoreExceptionHandlerAdvice
TYPE: Utility
PURPOSE: Global `@RestControllerAdvice` that maps all application exceptions to RFC 7807 `ProblemDetail` responses.
RESPONSIBILITIES:

- Handles MethodArgumentNotValidException, ConstraintViolationException, and related validation failures → 400
- Handles ResourceNotFoundException, ReferenceNotFoundException → 404
- Handles ResourceConflictException → 409
- Handles EquipmentNotAvailableException → 422
- Handles unhandled exceptions → 500
- Attaches `correlationId` (from MDC) and `errorCode` to every ProblemDetail
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/CoreExceptionHandlerAdvice.java`
  CALLS:
- UuidGenerator — to generate a fallback correlationId when MDC has none
  CALLED_BY:
- Spring MVC DispatcherServlet (on any thrown exception)

---

COMPONENT_NAME: CorrelationIdFilter
TYPE: Utility
PURPOSE: Servlet filter that ensures every request carries a correlationId in MDC throughout the request thread.
RESPONSIBILITIES:

- Reads X-Correlation-ID request header; generates a UUID v7 when absent
- Sets the value in SLF4J MDC under key `correlationId`
- Echoes the correlationId in the X-Correlation-ID response header
- Removes the MDC entry after the response completes
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/web/filter/CorrelationIdFilter.java`
  CALLS:
- NONE
  CALLED_BY:
- Spring servlet filter chain (every inbound HTTP request)

---

COMPONENT_NAME: UuidCreatorAdapter
TYPE: Utility
PURPOSE: Implements the UuidGenerator port using UUID v7 (time-ordered) strategy via the uuid-creator library.
RESPONSIBILITIES:

- Generates a new UUID v7 on each call to `generate()`
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/port/uuid/UuidCreatorAdapter.java`
  CALLS:
- NONE
  CALLED_BY:
- CoreExceptionHandlerAdvice

---

COMPONENT_NAME: CorsConfig
TYPE: Utility
PURPOSE: Configures Spring MVC CORS policy from `CorsProperties` typed configuration.
RESPONSIBILITIES:

- Registers allowed origins, methods, headers, credentials, and max-age for all `/**` mappings
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/config/CorsConfig.java`
  CALLS:
- NONE
  CALLED_BY:
- Spring MVC framework on startup

---

COMPONENT_NAME: OpenApiConfig
TYPE: Utility
PURPOSE: Configures SpringDoc OpenAPI metadata, tags, and grouped API definition.
RESPONSIBILITIES:

- Declares OpenAPI info (title, version, contact)
- Declares API tag constants consumed by all controllers
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/config/OpenApiConfig.java`
  CALLS:
- NONE
  CALLED_BY:
- SpringDoc auto-configuration on startup

---

## Component Call Sequences

---

### Use-Case: Create Rental — Fast Path (POST /api/rentals)

STEP 1: RentalCommandController → CreateRentalService
OPERATION: execute(CreateRentalCommand)
PURPOSE: Delegates validated HTTP request body to the application service
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`

STEP 2: CreateRentalService → CustomerFacadeImpl
OPERATION: findById(customerId)
PURPOSE: Verifies that the referenced customer exists; throws ReferenceNotFoundException if absent
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 3: CreateRentalService → EquipmentFacadeImpl
OPERATION: findByIds(equipmentIds)
PURPOSE: Batch-fetches requested equipment records for validation and tariff selection
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 4: CreateRentalService → RequestedEquipmentValidator
OPERATION: validateSize(requestedIds, fetchedEquipments) + validateAvailability(fetchedEquipments)
PURPOSE: Ensures all requested IDs resolved and all units are AVAILABLE; throws EquipmentNotAvailableException otherwise
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 5: CreateRentalService → TariffFacadeImpl
OPERATION: selectTariff(equipmentTypeSlug, duration, rentalDate) [per equipment unit]
PURPOSE: Auto-selects the cheapest matching active tariff for each equipment unit
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 6: TariffFacadeImpl → SelectTariffForRentalService
OPERATION: execute(SelectTariffCommand)
PURPOSE: Picks the tariff with minimum period price from active tariffs valid on rental date
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffFacadeImpl.java`

STEP 7: CreateRentalService → TariffFacadeImpl
OPERATION: calculateRentalCost(selectedTariffId, duration) [per equipment unit]
PURPOSE: Computes estimated rental cost for each equipment unit
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 8: CreateRentalService → RentalRepositoryAdapter
OPERATION: save(rental)
PURPOSE: Persists the fully-built Rental aggregate with all RentalEquipment children
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 9: CreateRentalService → SpringApplicationEventPublisher
OPERATION: publish("rental-events", RentalCreated)
PURPOSE: Notifies other modules that a rental was created so equipment status can be updated
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`

STEP 10: RentalEventListener → EquipmentRepository
OPERATION: findByIds(equipmentIds)
PURPOSE: Loads equipment records that need a status update
SOURCE:
`service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`

STEP 11: RentalEventListener → UpdateEquipmentUseCase
OPERATION: setStatusForEquipment(equipment, RESERVED)
PURPOSE: Marks each equipment unit as RESERVED in response to RentalCreated event
SOURCE:
`service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`

---

### Use-Case: Return Equipment (POST /api/rentals/return)

STEP 1: RentalCommandController → ReturnEquipmentService
OPERATION: execute(ReturnEquipmentCommand)
PURPOSE: Delegates the validated return request to the application service
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`

STEP 2: ReturnEquipmentService → RentalRepositoryAdapter
OPERATION: findById(rentalId) or findByUid(rentalUid)
PURPOSE: Loads the active Rental aggregate; throws ResourceNotFoundException if absent
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 3: ReturnEquipmentService → RentalDurationCalculatorImpl (via Rental aggregate)
OPERATION: calculateActualDuration(durationCalculator, returnTime)
PURPOSE: Computes actual and billable minutes (rounded to 5-minute increments)
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 4: ReturnEquipmentService → TariffFacadeImpl
OPERATION: calculateRentalCost(tariffId, actualDuration, billableMinutes, plannedDuration) [per returned equipment]
PURPOSE: Computes final cost per equipment unit applying the forgiveness rule (≤ 7 min overtime forgiven)
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 5: TariffFacadeImpl → CalculateRentalCostService
OPERATION: execute(CalculateRentalCostCommand)
PURPOSE: Calculates base period cost plus any overtime surcharge; returns detailed RentalCost breakdown
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffFacadeImpl.java`

STEP 6: ReturnEquipmentService → FinanceFacadeImpl
OPERATION: getPrepayment(rentalId)
PURPOSE: Retrieves the prepayment to compute whether an additional payment is owed
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 7: ReturnEquipmentService → FinanceFacadeImpl
OPERATION: recordAdditionalPayment(rentalId, toPay, method, operatorId) [when toPay > 0]
PURPOSE: Records the outstanding balance as an additional payment record
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 8: ReturnEquipmentService → RentalRepositoryAdapter
OPERATION: save(rental)
PURPOSE: Persists the updated Rental with RETURNED equipment statuses and COMPLETED rental status
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 9: ReturnEquipmentService → SpringApplicationEventPublisher
OPERATION: publish("rental-events", RentalCompleted)
PURPOSE: Notifies other modules that the rental is fully complete so equipment can be freed
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 10: RentalEventListener → EquipmentRepository
OPERATION: findByIds(returnedEquipmentIds)
PURPOSE: Loads equipment records associated with the completed rental
SOURCE:
`service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`

STEP 11: RentalEventListener → UpdateEquipmentUseCase
OPERATION: setStatusForEquipment(equipment, AVAILABLE)
PURPOSE: Marks each returned equipment unit as AVAILABLE for future rentals
SOURCE:
`service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`

---

## Communication Channels

- CHANNEL_TYPE: HTTP
  ENDPOINT: /api/rentals — POST, PATCH, GET
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
  NOTES: Port from `DATASOURCE_URL` env; base path `/api`; JSON request/response; ProblemDetail error format

- CHANNEL_TYPE: HTTP
  ENDPOINT: /api/equipments — POST, PUT, GET, PATCH
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/command/EquipmentCommandController.java`
  NOTES: Content-Type application/json

- CHANNEL_TYPE: HTTP
  ENDPOINT: /api/customers — POST, PUT, GET
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/web/command/CustomerCommandController.java`
  NOTES: Content-Type application/json

- CHANNEL_TYPE: HTTP
  ENDPOINT: /api/tariffs — GET; /api/tariffs/v2 — GET
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/TariffQueryController.java`
  NOTES: Query parameters: equipmentType, duration, date

- CHANNEL_TYPE: HTTP
  ENDPOINT: /api/payments — POST, GET
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/PaymentCommandController.java`
  NOTES: Content-Type application/json

- CHANNEL_TYPE: HTTP
  ENDPOINT: /actuator/health, /actuator/info, /actuator/metrics
  SOURCE: `service/src/main/resources/application-management-config.yaml`
  NOTES: Exposed via Spring Boot Actuator; CORS configured to match app.cors settings

- CHANNEL_TYPE: MessageQueue
  EXCHANGE: rental-events (in-process Spring ApplicationEvent)
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/messaging/SpringApplicationEventPublisher.java`
  NOTES: Not a broker; events are dispatched synchronously within the same JVM transaction via Spring Modulith
  @ApplicationModuleListener

---

## Dependency Registration and Wiring

- DI_CONTAINER: Spring IoC (Spring Boot auto-configuration + component scanning)
- REGISTRATION_FILE: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java` (`main` method);
  additional `@Configuration` classes in each module

Key registrations:

- LIFETIME: Singleton
  ABSTRACTION: EventPublisher
  IMPLEMENTATION: SpringApplicationEventPublisher
  REGISTRATION: `@Component` on SpringApplicationEventPublisher; auto-discovered by component scan
  ```java
  @Component
  public class SpringApplicationEventPublisher implements EventPublisher {
      private final ApplicationEventPublisher eventPublisher;
  }
  ```

- LIFETIME: Singleton
  ABSTRACTION: UuidGenerator
  IMPLEMENTATION: UuidCreatorAdapter
  REGISTRATION: `@Component` on UuidCreatorAdapter

- LIFETIME: Singleton
  ABSTRACTION: RentalDurationCalculator (domain port)
  IMPLEMENTATION: RentalDurationCalculatorImpl
  REGISTRATION: `@Service` on RentalDurationCalculatorImpl

- LIFETIME: Singleton
  ABSTRACTION: CreateRentalUseCase
  IMPLEMENTATION: CreateRentalService
  REGISTRATION: `@Service` on CreateRentalService (package-private class — not exposed outside module)

- LIFETIME: Singleton (request-scoped filter)
  ABSTRACTION: OncePerRequestFilter
  IMPLEMENTATION: CorrelationIdFilter
  REGISTRATION: `@Component` on CorrelationIdFilter; auto-registered by Spring Boot

- LIFETIME: Singleton
  ABSTRACTION: RentalRepository (domain port)
  IMPLEMENTATION: RentalRepositoryAdapter
  REGISTRATION: `@Repository` on RentalRepositoryAdapter

---

## Configuration and Secrets

- SOURCE_TYPE: config file
  KEYS: spring.application.name, spring.jpa.open-in-view, spring.liquibase.change-log, spring.liquibase.drop-first,
  springdoc.swagger-ui.enabled, app.default-locale, app.customers.search-limit-result, app.rental.time-increment,
  app.rental.forgiveness.overtime-duration
  SENSITIVE: NO
  LOCATION: `service/src/main/resources/application.yaml`

- SOURCE_TYPE: environment variable
  KEYS: DATASOURCE_URL, DATASOURCE_USER, DATASOURCE_SECRET
  SENSITIVE: YES
  LOCATION: `service/src/main/resources/application.yaml` (referenced via `${...}`)

- SOURCE_TYPE: environment variable
  KEYS: CORS_ALLOWED_ORIGINS
  SENSITIVE: NO
  LOCATION: `service/src/main/resources/application.yaml`

- SOURCE_TYPE: config file
  KEYS: management.endpoints.web.exposure.include, management.endpoint.health.show-details, management.info.env.enabled
  SENSITIVE: NO
  LOCATION: `service/src/main/resources/application-management-config.yaml`

- SOURCE_TYPE: config file
  KEYS: (profile-specific overrides, not committed)
  SENSITIVE: YES
  LOCATION: `service/src/main/resources/application-local.properties` (git-ignored; contains DATASOURCE_URL,
  DATASOURCE_USER, DATASOURCE_SECRET for local dev)

---

## Persistence and Data Access

- DATABASE: PostgreSQL (version managed by docker-compose; driver `org.postgresql:postgresql`)
- DATA_ACCESS: Spring Data JPA with Hibernate (via `spring-boot-starter-data-jpa`)
- MIGRATIONS_PATH: `service/src/main/resources/db/changelog/` (Liquibase; master: `db.changelog-master.xml`)
- REPOSITORY_PATTERN: YES

    - ABSTRACTION: `RentalRepository` (`rental/domain/repository/RentalRepository.java`)
      IMPLEMENTATION: `RentalRepositoryAdapter` (
      `rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`)

    - ABSTRACTION: `EquipmentRepository` (`equipment/domain/repository/EquipmentRepository.java`)
      IMPLEMENTATION: EquipmentRepositoryAdapter (`equipment/infrastructure/persistence/adapter/`)

    - ABSTRACTION: `CustomerRepository` (customer domain port)
      IMPLEMENTATION: CustomerRepositoryAdapter (`customer/infrastructure/persistence/adapter/`)

    - ABSTRACTION: `TariffRepository` (tariff domain port)
      IMPLEMENTATION: TariffRepositoryAdapter (`tariff/infrastructure/persistence/adapter/`)

    - ABSTRACTION: `PaymentRepository` (finance domain port)
      IMPLEMENTATION: PaymentRepositoryAdapter (`finance/infrastructure/persistence/adapter/`)

---

## Patterns and Architecture Notes

- PATTERN: Hexagonal Architecture (Ports and Adapters)
  EVIDENCE: `rental/domain/repository/RentalRepository.java` (port),
  `rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java` (adapter)
  SNIPPET:
  ```java
  // Port — domain layer, no framework imports
  public interface RentalRepository { Rental save(Rental rental); }
  // Adapter — infrastructure layer
  @Repository class RentalRepositoryAdapter implements RentalRepository { ... }
  ```

- PATTERN: Modular Monolith with Spring Modulith
  EVIDENCE: `BikeRentalApplication.java` (`@Modulithic(sharedModules = "shared")`), every module has `package-info.java`
  with `@ApplicationModule`
  SNIPPET:
  ```java
  @Modulithic(sharedModules = "shared")
  @SpringBootApplication
  public class BikeRentalApplication { ... }
  ```

- PATTERN: Facade (cross-module boundary contracts)
  EVIDENCE: `EquipmentFacade.java`, `CustomerFacade.java`, `TariffFacade.java`, `FinanceFacade.java` — interfaces
  residing at module root; implementations in `*FacadeImpl.java`
  SNIPPET: NONE

- PATTERN: CQRS (Command/Query separation at controller and use-case level)
  EVIDENCE: `RentalCommandController` vs `RentalQueryController`; `CreateRentalUseCase` vs `FindRentalsUseCase`
  SNIPPET: NONE

- PATTERN: Repository Pattern
  EVIDENCE: All five modules define a domain repository interface and a JPA adapter implementation
  SNIPPET: NONE

- PATTERN: Domain Events (in-process event-driven state propagation)
  EVIDENCE: `SpringApplicationEventPublisher.java`, `RentalEventListener.java`, `RentalCreated.java`,
  `RentalStarted.java`, `RentalCompleted.java`, `RentalUpdated.java`
  SNIPPET:
  ```java
  @ApplicationModuleListener
  public void onRentalCompleted(RentalCompleted event) {
      equipmentRepository.findByIds(event.returnedEquipmentIds())
          .forEach(e -> setStatusForEquipment(e, EquipmentStatus.AVAILABLE.name()));
  }
  ```

- PATTERN: MapStruct Mapper (compile-time type-safe mapping)
  EVIDENCE: `service/build.gradle` compiler flags `mapstruct.defaultComponentModel=spring`,
  `mapstruct.unmappedTargetPolicy=ERROR`; mapper classes in every `application/mapper/` and `web/*/mapper/` package
  SNIPPET: NONE

- PATTERN: Typed Configuration Properties
  EVIDENCE: `RentalProperties.java`, `CorsProperties.java`, `AppProperties.java` — `@ConfigurationProperties` records
  injected via constructor
  SNIPPET:
  ```java
  @ConfigurationProperties(prefix = "app.rental")
  public record RentalProperties(Duration timeIncrement, ForgivenessProperties forgiveness) {}
  ```

---

## Security and Operational Considerations

- AUTHN_AUTHZ: No authentication mechanism currently active in production code paths. `spring-boot-starter-security` and
  `spring-boot-starter-oauth2-resource-server` are declared in `gradle/libs.versions.toml` but no security configuration
  file was found in the scanned source tree — no `SecurityFilterChain` bean detected. All endpoints appear open.
  KNOWN_RISKS:
    - No authentication enforced on REST endpoints — all `/api/**` routes are publicly accessible
    - CORS origins are configurable via `CORS_ALLOWED_ORIGINS` env var; misconfiguration could expose the API to
      unwanted origins
    - `application-local.properties` is git-ignored but must never be committed; no secret scanning CI gate was
      identified
    - `spring.liquibase.drop-first=false` is set in `application.yaml` (safe default), but must remain false in all
      non-test profiles

- OBSERVABILITY:
    - Logging: SLF4J + Logback via `spring-boot-starter-logging`; MDC key `correlationId` set by `CorrelationIdFilter`;
      log level `root: info` in `application.yaml`
    - Health: `GET /actuator/health` (Spring Boot Actuator; `show-details: when-authorized`)
    - Info: `GET /actuator/info`
    - Metrics: `GET /actuator/metrics`
    - Actuator configuration: `service/src/main/resources/application-management-config.yaml`
    - API documentation: Swagger UI at `/swagger-ui/index.html`; OpenAPI JSON at `/v3/api-docs`

- DEPLOYMENT:
    - `service/Dockerfile` — Docker image definition for the Spring Boot application
    - `docker/docker-compose.yaml` — local infrastructure stack (PostgreSQL)
    - Deployed to Render.com as a Docker container (free tier); see `deployment.md`

