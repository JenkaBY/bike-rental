# System Patterns

## Architecture Overview

BikeRental follows a **Modular Monolithic Architecture** using **Spring Modulith** to provide strict module boundaries
while maintaining deployment simplicity.

### Core Architectural Principles

1. **Modular Monolith with Spring Modulith**
    - Single deployable JAR (fat JAR) for minimal infrastructure
    - Strict module boundaries enforced via `@ApplicationModule`
    - Event-driven communication between modules
    - Easy migration path to microservices if needed

2. **Domain-Driven Design (DDD)**
    - Each module represents a bounded context
    - Aggregate roots with clear boundaries
    - Domain events for cross-module communication
    - Ubiquitous language within each module
   - Rich domain models encapsulating business logic
   - Use custom value objects for domain concepts (e.g., Money, DiscountPercent or java classes representing value
     objects)

3. **Hexagonal Architecture (Ports & Adapters)**
    - Domain core isolated from infrastructure
    - Ports define abstractions (interfaces)
    - Adapters implement infrastructure concerns
    - Easy testing with mock implementations

4. **CQRS (Command Query Responsibility Segregation)**
    - Separate command and query operations
    - Command controllers handle state changes
    - Query controllers handle read operations
    - Different DTOs for commands and queries

5. **Event-Driven Architecture**
    - Domain events for loose coupling
    - Async processing of cross-module operations
    - Event sourcing potential for audit trail
    - Spring Application Events for communication

## Module Structure

### 8 Business Modules

The system is decomposed into 8 modules based on functional requirements (FR-*):

```
┌─────────────────────────────────────────────────────────────┐
│              CORE MODULES (Independent)                     │
├─────────────────────────────────────────────────────────────┤
│  customer    │  equipment   │  tariff                      │
│  FR-CL-*     │  FR-EQ-*     │  FR-TR-*                     │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│              BUSINESS MODULES                               │
├─────────────────────────────────────────────────────────────┤
│  rental      │  finance     │  maintenance                 │
│  FR-RN-*     │  FR-FN-*     │  FR-MT-*                     │
│  (CORE)      │              │                              │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│              SUPPORT MODULES                                │
├─────────────────────────────────────────────────────────────┤
│  reporting   │  admin                                       │
│  FR-RP-*     │  FR-AD-*                                     │
└─────────────────────────────────────────────────────────────┘
```

**Module Dependencies:**

- Rental → Customer, Equipment, Tariff, Finance
- Maintenance → Equipment
- Reporting → All data modules (read-only)
- Admin → System-wide (configuration)

### Layered Architecture per Module

Each module follows a 5-layer structure:

```
┌────────────────────────────────────────────────────────────┐
│  web (REST API)                                            │  ← HTTP Entry Point
│  - command/ (state-changing operations)                   │
│  - query/ (read operations)                               │
├────────────────────────────────────────────────────────────┤
│  application (Use Cases)                                   │  ← Business Scenarios
│  - usecase/ (interfaces)                                  │
│  - service/ (implementations)                             │
│  - port/ (abstractions)                                   │
├────────────────────────────────────────────────────────────┤
│  domain (Business Logic)                                   │  ← Core Domain
│  - model/ (aggregates, entities, value objects)          │
│  - repository/ (domain interfaces)                        │
├────────────────────────────────────────────────────────────┤
│  infrastructure (Technical Implementation)                 │  ← Infrastructure
│  - persistence/ (JPA, adapters)                          │
│  - event/ (event publishers)                             │
│  - adapter/ (external integrations)                       │
├────────────────────────────────────────────────────────────┤
│  event (Public Events)                                     │  ← Inter-module API
│  - Domain events published to other modules               │
└────────────────────────────────────────────────────────────┘
```

### Shared Kernel

Common infrastructure and utilities shared across all modules:

```
shared/
├── exception/           # Common exception types
├── domain/
│   ├── Money.java      # Value Object for money
│   ├── TimeRange.java  # Value Object for time intervals
│   └── DomainEvent.java # Base event interface
└── web/
    └── advice/
        └── CoreExceptionHandlerAdvice.java # Global exception handling
```

## Design Patterns

### Domain Patterns

**1. Aggregate Pattern**

- Each module has aggregate roots (e.g., Rental, Customer, Equipment, Tariff and so on)
- Aggregate root controls all access to entities within
- Transactions limited to single aggregate
- Example: `Rental` aggregate contains rental status, times, costs

**2. Repository Pattern**

- Domain repositories define abstract interfaces
- Infrastructure adapters implement concrete repositories
- Separation of domain logic from data access
- Example: `RentalRepository` (interface) → `RentalRepositoryAdapter` (implementation)

**3. Value Object Pattern**

- Immutable objects for domain concepts
- `Money` for financial amounts with currency
- `RentalDuration` for time periods
- `CustomerRef`, `EquipmentRef` for cross-module references

**3.1 Port Pattern (Hexagonal Architecture)**

- Domain defines **port interfaces** for external dependencies
- Application layer provides **implementations** of domain ports
- Enables dependency inversion and testability
- Domain layer never depends on application layer

**Status Transition Port Example:**

```java
// Domain Port (interface)
public interface StatusTransitionPolicy {
    void validateTransition(String fromStatusSlug, String toStatusSlug);
}

// Domain Entity uses port
public class Equipment {
    public void changeStatusTo(String newStatusSlug, StatusTransitionPolicy policy) {
        policy.validateTransition(this.statusSlug, newStatusSlug);
        this.statusSlug = newStatusSlug;
    }
}

// Application Implementation
@Service
public class EquipmentStatusTransitionPolicy implements StatusTransitionPolicy {
    // Implementation uses repositories, configuration, etc.
}
```

**Rental Duration Calculator Port Example:**

```java
// Domain Port (interface)
public interface RentalDurationCalculator {
    RentalDurationResult calculate(LocalDateTime start, LocalDateTime end);
}

// Domain Entity uses port
public class Rental {
    public RentalDurationResult calculateActualDuration(
            RentalDurationCalculator calculator, 
            LocalDateTime returnTime) {
        return calculator.calculate(this.startedAt, returnTime);
    }
}

// Application Implementation
@Service
public class RentalDurationCalculatorImpl implements RentalDurationCalculator {
    private final RentalProperties properties;
    // Implementation uses configuration, performs calculations
}
```

**Benefits:**

- Domain entities remain pure (no infrastructure dependencies)
- Easy to test with mock policies
- Swap implementations via dependency injection
- Clear separation of concerns

**4. Domain Events**

- Immutable event records
- Published after state changes
- Consumed by other modules asynchronously
- Examples: `RentalStarted`, `RentalCompleted`, `EquipmentStatusChanged`
- **ARCHITECTURAL RULE**: All domain events MUST implement
  `com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent` marker interface

**Event Contract:**

```java
package com.github.jenkaby.bikerental.shared.infrastructure.messaging;

/**
 * Marker interface for all domain events in the BikeRental system.
 * 
 * Purpose:
 * - Type safety for event handling and routing
 * - Enables generic event processing infrastructure
 * - Facilitates event filtering and validation
 * - Serves as documentation of system-wide event contracts
 * 
 * Usage: All domain events (records or classes) published within 
 * the system MUST implement this interface.
 */
public interface BikeRentalEvent {
}
```

**Example Implementation:**

```java
public record PaymentReceived(
    UUID paymentId,
    Long rentalId,
    Money amount,
    PaymentType paymentType,
    Instant receivedAt
) implements BikeRentalEvent {
}
```

**Benefits:**

- **Type Safety**: Compile-time verification of event contracts
- **Event Filtering**: Infrastructure can distinguish domain events from framework events
- **Centralized Processing**: Generic handlers for all domain events
- **Testing**: Simplified event capture and verification in tests
- **Documentation**: Clear identification of domain events vs system events

**5. Specification Pattern**

- Encapsulates query logic
- Business rules as specifications
- Reusable and testable query conditions
- Example: Equipment availability checks

### Application Patterns

**6. Use Case Pattern**

- Each business operation is a use case
- Interface defines the contract
- Service implements the use case
- Example: `CreateRentalUseCase` → `CreateRentalService`

**7. Command Pattern**

- Commands represent state-changing operations
- Immutable command objects
- Validated before execution
- Examples: `CreateRentalCommand`, `ReturnEquipmentCommand`

**7.1. Result Object Pattern**

- Encapsulates calculation results in immutable objects
- Provides computed values via default methods
- Single source of truth for calculation outputs
- Located in domain layer (domain.service package)
- Example: `RentalDurationResult` interface with `BaseRentalDurationResult` record implementation

**Example:**

```java
// Domain layer (rental.domain.service)
public interface RentalDurationResult {
    default int actualMinutes() {
        return (int) actualDuration().toMinutes();
    }
    int billableMinutes();
    Duration actualDuration();
}

public record BaseRentalDurationResult(
    int billableMinutes,
    Duration actualDuration
) implements RentalDurationResult {}
```

**Benefits:**

- Encapsulation: all calculation results in one object
- Performance: calculations performed once
- Immutability: record ensures thread-safety
- Computed values: default methods avoid redundant storage
- Domain ownership: result objects belong to domain layer

**8. Facade Pattern**

- Public API for each module
- Simplifies inter-module communication
- Example: `RentalFacade` exposes rental operations to other modules

**9. Strategy Pattern**

- Tariff selection strategies
- Cost calculation strategies
- Payment method strategies
- Example: `TariffSelectionService` with different matching strategies

**9.1. Calculator Pattern**

- Dedicated services for complex calculations
- Configuration-driven behavior via application properties
- Single responsibility: perform calculations only
- Follows Dependency Inversion: domain defines port interface, application provides implementation
- Example: `RentalDurationCalculator` port in domain with `RentalDurationCalculatorImpl` in application

**Example:**

```java
// Domain Port (interface)
public interface RentalDurationCalculator {
    RentalDurationResult calculate(LocalDateTime start, LocalDateTime end);
}

// Application Implementation
@Service
public class RentalDurationCalculatorImpl implements RentalDurationCalculator {
    private final RentalProperties properties;
    
    @Override
    public RentalDurationResult calculate(LocalDateTime start, LocalDateTime end) {
        Duration actualDuration = Duration.between(start, end);
        int increment = getTimeIncrementMinutes();
        int billableMinutes = roundUp(actualDuration.toMinutes(), increment);
        return new BaseRentalDurationResult(billableMinutes, actualDuration);
    }
}
```

**Benefits:**

- Separation of concerns: calculation logic isolated
- Configuration: business rules externalized (app.rental.time-increment)
- Testability: easy to unit test with different configurations
- Reusability: can be used by multiple use cases
- Architecture compliance: domain layer doesn't depend on application layer

**10. State Machine Pattern**

- Rental status transitions (DRAFT → ACTIVE → COMPLETED/CANCELLED)
- Equipment status transitions (AVAILABLE → RENTED → AVAILABLE/MAINTENANCE)
- Valid state transitions enforced in domain

### Infrastructure Patterns

**11. Adapter Pattern**

- Adapters implement domain interfaces
- `RentalRepositoryAdapter` adapts JPA to domain repository
- `SpringDomainEventPublisher` adapts Spring events to domain events

**12. Mapper Pattern (MapStruct)**

- Three layers of mapping:
    - Web DTO ↔ Command/Query (Web layer)
    - Domain ↔ Public DTO (API layer)
    - Domain ↔ JPA Entity (Persistence layer)
- Type-safe compile-time code generation
- Shared mappers (`MoneyMapper`, `InstantMapper`, `UuidMapper`) for common conversions
- **Time Type Conventions:**
    - `Instant` for audit fields (`createdAt`, `updatedAt`) - UTC timestamps
    - `LocalDateTime` for business time (`startedAt`, `expectedReturnAt`) - timezone-agnostic local time
    - `LocalDate` for date-only fields (`birthDate`, `validFrom`)
    - Automatic conversion via `InstantMapper` when mapping between layers

**13. Factory Pattern**

- Aggregate creation logic
- Example: `RentalFactory` creates valid Rental aggregates

**14. Builder Pattern**

- Complex object construction
- Fluent API for readability
- Example: Building rental with step-by-step validation

### Cross-Cutting Patterns

**15. Event Sourcing (Partial)**

- Audit log captures all state changes
- Can reconstruct state from events
- Not full event sourcing, but event-aware

**16. Saga Pattern**

- Multi-step business processes
- Compensating transactions for rollback
- Example: Rental cancellation with refund

**17. Anti-Corruption Layer**

- Protects domain from external system changes
- Mappers act as anti-corruption layer
- Example: JPA entities isolated from domain

## Component Communication

### Synchronous Communication

**Within Module:** Direct method calls

```
Controller → UseCase → Domain Service → Repository
```

**Between Modules:** Facade pattern with DTO exchange

```
RentalService → CustomerLookupService (Facade)
              → EquipmentAvailabilityService (Facade)
              → TariffSelectionService (Facade)
```

**Public APIs:**

- Each module exposes Facade interface
- DTOs for data exchange (immutable records)
- No direct domain object sharing

### Asynchronous Communication

**Event Publishing:**

```java
// Domain publishes events
rental.complete();
eventPublisher.

publish(new RentalCompleted(rentalId, equipmentId, cost));
```

**Event Handling:**

```java
// Other modules listen to events
@EventListener
public void onRentalCompleted(RentalCompleted event) {
    // Update equipment status
    // Record payment
    // Update usage statistics
}
```

**Event Flow Example:**

```
ReturnEquipmentUseCase
  ↓ publishes
RentalCompleted Event
  ↓ consumed by
  ├── FinanceModule (record surcharge payment)
  ├── EquipmentModule (update status to AVAILABLE)
  └── MaintenanceModule (update usage hours)
```

## Data Flow

### Command Flow (Write Operations)

```
1. HTTP Request → REST Controller
2. Controller validates request
3. Maps to Command object
4. UseCase receives Command
5. Domain logic executes
6. Repository persists changes
7. Events published
8. Response DTO returned
```

**Example: Create Rental**

```
POST /api/rentals
  → RentalCommandController
  → CreateRentalUseCase
  → Rental.create() [domain logic]
  → RentalRepository.save()
  → Publish RentalCreated
  → Return RentalResponse
```

### Query Flow (Read Operations)

```
1. HTTP Request → REST Controller
2. Query service fetches data
3. Data mapped to DTO
4. Response returned
```

**Example: Get Active Rentals**

```
GET /api/rentals/active
  → RentalQueryController
  → RentalQueryService
  → RentalRepository.findActive()
  → Map to RentalResponse[]
  → Return JSON
```

### Event Flow (Cross-Module)

```
1. Domain event published
2. Spring ApplicationEventPublisher broadcasts
3. All registered listeners receive event
4. Each listener processes independently
5. Eventual consistency achieved
```

**Example: Rental Completion**

```
RentalCompleted Event
  ├─> FinanceEventHandler (async)
  │     └─> Record payment
  ├─> EquipmentEventHandler (async)
  │     └─> Update status
  └─> MaintenanceEventHandler (async)
        └─> Update usage hours
```

## API Design

### RESTful Principles

**Resource-based URLs:**

```
/api/rentals           # Rental collection
/api/rentals/{id}      # Specific rental
/api/customers         # Customer collection
/api/equipment         # Equipment collection
```

**HTTP Methods:**

- GET: Read operations (idempotent)
- POST: Create new resource
- PUT: Full update (replace)
- PATCH: Partial update
- DELETE: Remove resource

**Status Codes:**

- 200 OK: Successful GET/PUT/PATCH
- 201 Created: Successful POST
- 204 No Content: Successful DELETE
- 400 Bad Request: Validation error
- 404 Not Found: Resource not found
- 409 Conflict: Business rule violation
- 500 Internal Server Error: System error

### API Versioning

**Content-Type Negotiation:**

```
Accept: application/vnd.bikerental.v1+json
```

### Request/Response Format

**Standard Response Structure:**

```json
{
  "id": "uuid",
  "field1": "value",
  "field2": 123,
  "createdAt": "2026-01-26T10:00:00Z"
}
```

**Error Response:**

```json
{
  "timestamp": "2026-01-26T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/rentals",
  "errors": [
    {
      "field": "customerId",
      "message": "Customer ID is required"
    }
  ]
}
```

### CQRS Endpoints

**Command Endpoints** (state-changing):

```
POST   /api/rentals
POST   /api/rentals/{id}/start
POST   /api/rentals/{id}/return
POST   /api/rentals/{id}/cancel
PATCH  /api/rentals/{id}/customer
```

**Query Endpoints** (read-only):

```
GET /api/rentals
GET /api/rentals/{id}
GET /api/rentals/active
GET /api/rentals?customerId={id}
GET /api/dashboard
```

### Role-Based Access

**Public APIs** (OPERATOR role):

```
/api/rentals/*
/api/customers/*
/api/equipment/*
/api/payments/*
/api/dashboard
```

**Admin APIs** (ADMIN role):

```
/api/admin/users/*
/api/admin/settings/*
/api/admin/tariffs/*
/api/admin/equipments/*
/api/admin/audit/*
```

## Security Architecture

### Authentication

**Spring Security with JWT:**

- Stateless authentication
- JWT tokens for API access
- Token expiration and refresh

**OAuth2 Support:**

- Google Sign-In integration
- Future: additional providers

**Password Security:**

- BCrypt hashing
- Minimum complexity requirements
- Password history (future)

### Authorization

**Role-Based Access Control (RBAC):**

```java

@PreAuthorize("hasRole('OPERATOR')")
public RentalResponse createRental(...) {
}

@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(...) {
}
```

**Simplified Role Model:**

- **OPERATOR**: All operational functions (rental, customers, equipment, payments)
- **ADMIN**: All functions + system administration

### Audit Trail

**Comprehensive Logging:**

- Authentication events (login/logout/failed attempts)
- Authorization failures (access denied)
- Entity changes (create/update/delete)
- Financial operations (payments/refunds)
- Configuration changes

**AOP-based Auditing:**

```java

@Audited(eventType = ENTITY_CREATED)
public Customer createCustomer(...) {
}
```

**Immutable Audit Log:**

- Cannot be modified or deleted
- 1+ year retention
- Async processing for performance

### Data Security

**Sensitive Data Handling:**

- Customer phone numbers (searchable but protected)
- Financial transaction details
- User credentials

**Database Security:**

- Encrypted connections
- Prepared statements (SQL injection prevention)
- Row-level security (future)

### API Security

**CORS Configuration (Implemented):**

- `CorsProperties` — `@ConfigurationProperties(prefix = "app.cors")` с `@DefaultValue` для всех полей кроме
  `allowedOrigins`
- `CorsConfig` — `WebMvcConfigurer.addCorsMappings("/**")` + `CorsConfigurationSource` бин
- `allowedOrigins` — обязательный, задаётся в `application.yaml` для каждого окружения
- Defaults: methods = GET/POST/PUT/PATCH/DELETE/OPTIONS, headers = *, credentials = true, maxAge = 3600
- Exists: `shared/config/` (web infrastructure adapter layer)

**Input Validation:**

- JSR-303 Bean Validation
- Custom validators for business rules
- Sanitization of user input

## Technical Decisions

### Key Choices

1. **Spring Modulith over Microservices**
    - Rationale: Simplified deployment, minimal infrastructure, easy refactoring later
    - Trade-off: Shared database, single deployment unit

2. **PostgreSQL**
    - Rationale: Robust, ACID compliance, JSON support, proven technology
    - Trade-off: No NoSQL flexibility

3. **Event-Driven Communication**
    - Rationale: Loose coupling, async processing, eventual consistency
    - Trade-off: Debugging complexity, eventual consistency challenges

4. **MapStruct for Mapping**
    - Rationale: Type-safe, compile-time code generation, performance
    - Trade-off: Additional build step, learning curve

5. **TDD Approach**
    - Rationale: High quality, regression prevention, living documentation
    - Trade-off: Initial development time

6. **Single JAR Deployment**
    - Rationale: Simplified operations, minimal infrastructure
    - Trade-off: Cannot scale individual components independently
