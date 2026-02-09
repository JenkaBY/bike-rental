# [US-RN-001] - Создание записи аренды (Create Rental Record)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-07  
**Priority:** High  
**Module:** rental  
**Dependencies:** US-CL-001, US-CL-002, US-EQ-002, US-TR-001

## Original Request

**Как** Оператор проката  
**Я хочу** создать новую запись аренды  
**Чтобы** оформить аренду оборудования клиенту

## User Story Details

**Описание:**  
Система должна позволять оператору создать новую запись аренды.

**Последовательность действий:**

1. Поиск/создание клиента
2. Выбор оборудования по порядковому номеру
3. Выбор времени аренды (1 час, 2 часа, сутки и т.д.)
4. Автоматический подбор тарифа
5. Расчет предварительной стоимости
6. Внесение предоплаты
7. Запуск аренды

**Критерии приемки:**

- Все шаги выполняются последовательно
- Невозможно пропустить обязательные шаги
- Сохранение черновика аренды

**Связанные требования:** FR-RN-001

## Thought Process

This is the **core user story** for the rental module - the main business process. It orchestrates the entire rental
workflow. Key considerations:

1. **Workflow State Machine**: Rental goes through states: DRAFT → ACTIVE → COMPLETED/CANCELLED
2. **Multi-Step Process**: Each step builds upon the previous, creating a wizard-like flow
3. **Draft Support**: Allow saving incomplete rentals to resume later
4. **Validation**: Each step must validate before proceeding to next
5. **Aggregate Root**: Rental is a complex aggregate coordinating customer, equipment, tariff, and payments

**Architecture Decisions:**

- Rental is the central aggregate root in rental module
- Use RentalStatus enum for state management
- Implement builder pattern for step-by-step creation
- Commands for each step (SelectCustomer, SelectEquipment, CalculateCost, etc.)
- Events published at key transitions (RentalCreated, RentalStarted, etc.)

**Domain Model Design:**

```
Rental (Aggregate Root)
├── id: Long
├── customerId: UUID (reference)
├── equipmentId: Long (reference)
├── tariffId: Long (reference)
├── status: RentalStatus (enum)
├── startedAt: LocalDateTime
├── expectedReturnAt: LocalDateTime
├── actualReturnAt: LocalDateTime
├── plannedDurationMinutes: int
├── actualDurationMinutes: int
├── estimatedCost: Money
├── finalCost: Money
└── payments: List<PaymentRef>
```

**State Transitions:**

```
DRAFT → ACTIVE (when prepayment received and started)
ACTIVE → COMPLETED (normal return)
ACTIVE → CANCELLED (early cancellation)
DRAFT → CANCELLED (never activated)
```

**Integration Points:**

- Customer module: search/create customer
- Equipment module: select equipment, check availability
- Tariff module: select tariff, calculate cost
- Finance module: record payments

## Implementation Plan

- [x] Create Rental domain model with RentalStatus enum
- [x] Implement CreateRentalUseCase (creates DRAFT)
- [x] Implement SelectCustomerCommand (via JSON Patch)
- [x] Implement SelectEquipmentCommand with availability check (via JSON Patch)
- [x] Implement SelectTariffCommand (via JSON Patch)
- [x] Implement CalculateEstimatedCostCommand (partially in tariff module)
- [x] Create REST endpoints for rental creation workflow
- [x] Add database migration for rentals table
- [x] Implement draft rental persistence
- [x] Create validation for step sequencing
- [x] Publish RentalCreated domain event
- [x] Create component tests for rental creation flow
- [x] Write unit tests for validation logic
- [x] Write WebMvc tests for endpoints

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                     | Status    | Updated    | Notes                                                               |
|-----|---------------------------------|-----------|------------|---------------------------------------------------------------------|
| 1.1 | Create Rental domain model      | Completed | 2026-02-07 | Core aggregate with RentalStatus enum                               |
| 1.2 | Implement draft rental creation | Completed | 2026-02-07 | Fast Path and Draft Path implemented                                |
| 1.3 | Add customer selection          | Completed | 2026-02-07 | Via JSON Patch (PATCH /api/rentals/{id})                            |
| 1.4 | Add equipment selection         | Completed | 2026-02-07 | Via JSON Patch (PATCH /api/rentals/{id})                            |
| 1.5 | Add tariff selection            | Completed | 2026-02-07 | Via JSON Patch (PATCH /api/rentals/{id})                            |
| 1.6 | Implement cost calculation      | Completed | 2026-02-07 | Basic cost calculation implemented (full calculation in US-TR-002)  |
| 1.7 | Create REST endpoints           | Completed | 2026-02-07 | POST /api/rentals, POST /api/rentals/draft, PATCH /api/rentals/{id} |
| 1.8 | Create tests                    | Completed | 2026-02-07 | Unit tests, WebMvc tests, and Component tests implemented           |

## Progress Log

### 2026-02-07 (Testing Completion)

**Testing Implementation:**

- ✅ **WebMvc Tests**: Comprehensive test suite for `RentalCommandController` implemented
    - Tests grouped using `@Nested` annotations by HTTP method and expected status
    - Coverage for `POST /api/rentals` validation (customerId, equipmentId, duration, startTime)
    - Coverage for `POST /api/rentals/draft` success scenario
    - Coverage for `PATCH /api/rentals/{id}` validation (operations list, op, path, value, cross-field validation,
      status values)
    - All validation scenarios covered with proper error responses

- ✅ **Component Tests**: End-to-end testing with Cucumber/Gherkin implemented
    - Feature files: `rental.feature` and `rental-validation.feature`
    - Transformers created for all DTOs:
        - `RentalRequestTransformer` - converts DataTable to `CreateRentalRequest`
        - `RentalResponseTransformer` - converts DataTable to `RentalResponse` (with alias support)
        - `RentalPatchOperationTransformer` - converts DataTable to `RentalPatchOperation` (with type inference)
        - `RentalJpaEntityTransformer` - converts DataTable to `RentalJpaEntity`
    - Step definitions in `RentalWebSteps` and `RentalDbSteps`
    - Scenarios cover: draft creation, rental creation (with/without tariff), JSON Patch updates (customer, equipment,
      duration/startTime, activation, combined), query operations
    - Scenario Outline pattern used for parameterized testing

- ✅ **Unit Tests**: Validator tests implemented
    - `RentalPatchOperationValidator` tests
    - `RentalPatchRequestValidator` tests
    - All validation rules covered

**Architecture Decision:**

- **Service Layer Abstraction**: Decision made to keep `Map<String, Object>` abstraction in `UpdateRentalUseCase` and
  `UpdateRentalService`
    - This provides flexibility and decouples service layer from specific JSON Patch library implementation
    - Controller layer handles JSON Patch validation and conversion, service layer works with domain-friendly Map
      structure
    - This is an intentional architectural choice, not a limitation

### 2026-02-07 (Initial Implementation)

**Implementation Updates:**

- ✅ **JSON Patch (RFC 6902) Implementation**: Implemented unified PATCH endpoint using JSON Patch standard
  - Created `JsonPatchOperation` enum (REPLACE, ADD) for type-safe operation handling
  - Created `RentalPatchOperation` DTO with validation annotations
  - Created `RentalUpdateJsonPatchRequest` DTO wrapping list of operations
  - Implemented custom validators (`RentalPatchOperationValidator`, `RentalPatchRequestValidator`)
  - Added `@ValidRentalPatchOperation` and `@ValidRentalPatchRequest` custom validation annotations

- ✅ **Domain Exceptions**: Created domain-specific exceptions for rental state management
  - `InvalidRentalStatusException` - thrown when operation is invalid for current rental status
  - `RentalNotReadyForActivationException` - thrown when rental cannot be activated (missing required fields)
  - All methods in `Rental` aggregate now use these exceptions for state validation

- ✅ **Mappers**: Extracted conversion logic to dedicated MapStruct mappers
  - `RentalEventMapper` - converts domain events (`RentalCreated`, `RentalStarted`) to DTOs
  - `RentalCommandMapper` - converts `RentalUpdateJsonPatchRequest` to `Map<String, Object>` for service layer
  - `TariffToInfoMapper` - converts `Tariff` domain model to `TariffInfo` DTO
  - `EquipmentToInfoMapper` - converts `Equipment` domain model to `EquipmentInfo` DTO

- ✅ **Utility Components**: Created reusable Spring components
  - `PatchValueParser` - parses values from patch operations (UUID, Long, Duration, LocalDateTime, String)
  - `TariffPeriodSelector` - selects appropriate tariff period based on rental duration

- ✅ **Use Case Refactoring**: Updated `GetTariffByIdUseCase` to support both Optional and direct return
  - `Optional<Tariff> execute(Long id)` - returns Optional for flexible handling
  - `Tariff get(Long id)` - throws `ResourceNotFoundException` if not found

- ✅ **Tariff Module Improvements**:
  - Created `SuitableTariffNotFoundException` for business rule violations (no active/valid tariff found)
  - Created `TariffRestControllerAdvice` for handling tariff-specific exceptions (422 UNPROCESSABLE_CONTENT)
  - Refactored `TariffFacadeImpl` to use `TariffToInfoMapper` and `TariffPeriodSelector`

- ✅ **Error Handling**: Standardized HTTP status codes
  - `422 UNPROCESSABLE_CONTENT` for domain exceptions (`InvalidRentalStatusException`,
    `RentalNotReadyForActivationException`, `SuitableTariffNotFoundException`)
  - `404 NOT_FOUND` for resource not found (`ResourceNotFoundException`)

- ✅ **Code Quality**: Consistent use of `RentalStatus` enum throughout codebase
  - Replaced string literals with enum references
  - Updated documentation to reference enum

**Architecture Decisions:**

- **Hybrid Approach**: Fast Path (create with customer/equipment) and Draft Path (create empty, update via PATCH)
- **JSON Patch Standard**: Using RFC 6902 for partial updates instead of custom endpoints
- **Type Safety**: Enum for operations (`JsonPatchOperation`) instead of string literals
- **Validation**: Multi-level validation (Bean Validation + custom validators) for JSON Patch requests
- **Separation of Concerns**: Parsing logic extracted to `PatchValueParser`, conversion to mappers

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on multiple Phase 1 and Phase 2 tasks
- Part of Phase 3: Main Rental Process
- This is the foundational rental workflow task

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.rental
├── web.command
│   ├── RentalCommandController
│   ├── dto.CreateRentalRequest
│   ├── dto.RentalUpdateJsonPatchRequest
│   ├── dto.RentalPatchOperation
│   ├── dto.JsonPatchOperation (enum)
│   ├── dto.validation.* (validators)
│   ├── mapper.RentalCommandMapper
│   └── error.RentalRestControllerAdvice
├── application
│   ├── usecase.CreateRentalUseCase
│   ├── usecase.UpdateRentalUseCase
│   ├── service.CreateRentalService
│   ├── service.UpdateRentalService
│   ├── mapper.RentalEventMapper
│   └── util.PatchValueParser
├── domain
│   ├── model.Rental
│   ├── model.RentalStatus (enum)
│   ├── exception.InvalidRentalStatusException
│   ├── exception.RentalNotReadyForActivationException
│   ├── repository.RentalRepository
│   └── event.RentalCreated, RentalStarted
└── infrastructure
    └── persistence
```

**API Endpoints:**

- `POST /api/rentals` - Create new rental (Fast Path - with customer and equipment)
- `POST /api/rentals/draft` - Create draft rental (Draft Path - empty rental)
- `PATCH /api/rentals/{id}` - Update rental using JSON Patch (RFC 6902)
  - Supports partial updates: customer, equipment, tariff, duration, startTime, status
  - Uses `JsonPatchOperation` enum (REPLACE, ADD)
  - Validated via `RentalUpdateJsonPatchRequest` DTO
- `GET /api/rentals/{id}` - Get rental details

**JSON Patch Examples:**

```json
// Select customer
[
  {
    "op": "replace",
    "path": "/customerId",
    "value": "uuid"
  }
]

// Select equipment
[
  {
    "op": "replace",
    "path": "/equipmentId",
    "value": 123
  }
]

// Set duration and start time
[
  {
    "op": "replace",
    "path": "/duration",
    "value": "PT2H"
  },
  {
    "op": "replace",
    "path": "/startTime",
    "value": "2026-02-07T10:00:00"
  }
]

// Start rental (activate)
[
  {
    "op": "replace",
    "path": "/status",
    "value": "ACTIVE"
  }
]

// Combined update
[
  {
    "op": "replace",
    "path": "/customerId",
    "value": "uuid"
  },
  {
    "op": "replace",
    "path": "/equipmentId",
    "value": 123
  }
]
```

**Database Schema:**

```sql
CREATE TABLE rentals
(
    id                       UUID PRIMARY KEY,
    customer_id              UUID REFERENCES customers (id),
    equipment_id             UUID REFERENCES equipment (id),
    tariff_id                UUID REFERENCES tariffs (id),
    status                   VARCHAR(20) NOT NULL,
    started_at               TIMESTAMP,
    expected_return_at       TIMESTAMP,
    actual_return_at         TIMESTAMP,
    planned_duration_minutes INT,
    actual_duration_minutes  INT,
    estimated_cost           DECIMAL(10, 2),
    final_cost               DECIMAL(10, 2),
    created_at               TIMESTAMP   NOT NULL,
    updated_at               TIMESTAMP   NOT NULL
);
```

**RentalStatus Enum:**

```java
public enum RentalStatus {
    DRAFT,      // Создается, еще не активна
    ACTIVE,     // Активна, оборудование у клиента
    COMPLETED,  // Завершена, оборудование возвращено
    CANCELLED   // Отменена
}
```

**Domain Events:**

```java
record RentalCreated(
        UUID rentalId,
        UUID customerId,
        UUID equipmentId,
        LocalDateTime createdAt
) {
}
```

**Validation Rules:**

- Customer must exist before equipment selection
- Equipment must be AVAILABLE before selection
- Tariff must match equipment type
- Draft rental must have customer + equipment + tariff before starting
- Only DRAFT rentals can be updated (except status change to ACTIVE)
- Rental status transitions are validated via domain exceptions

**JSON Patch Validation:**

- Operation type: Must be "replace" or "add" (`JsonPatchOperation` enum)
- Path: Must start with "/" and be one of allowed paths:
  - `/customerId`, `/equipmentId`, `/tariffId`, `/duration`, `/startTime`, `/status`
- Value: Required for REPLACE and ADD operations
- Cross-field validation: Duration and startTime must be provided together
- Status validation: Status value must be valid `RentalStatus` enum value

**Error Handling:**

- `422 UNPROCESSABLE_CONTENT`: Domain rule violations
  - `InvalidRentalStatusException` - invalid status transition
  - `RentalNotReadyForActivationException` - missing required fields for activation
  - `SuitableTariffNotFoundException` - no active/valid tariff found
- `404 NOT_FOUND`: Resource not found (`ResourceNotFoundException`)

## Known Issues

**Current Implementation Notes:**

- ✅ JSON Patch implementation complete with validation
- ✅ Domain exceptions properly mapped to HTTP status codes
- ✅ Mappers extracted for better separation of concerns
- ✅ **Architecture Decision**: `UpdateRentalUseCase` and `UpdateRentalService` use `Map<String, Object>` abstraction
    - This is an intentional architectural choice to decouple service layer from JSON Patch library specifics
    - Controller layer handles JSON Patch validation and converts to Map for service layer
    - Provides flexibility and keeps service layer focused on domain logic

**Testing Status:**

- ✅ Unit tests for validators: Implemented
- ✅ WebMvc tests for endpoints: Implemented (`RentalCommandControllerTest`)
- ✅ Component tests for rental flow: Implemented (`rental.feature`, `rental-validation.feature`)

## References

- User Story File: [docs/tasks/us/US-RN-001/us-rn-001.md](../../../docs/tasks/us/US-RN-001/us-rn-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependencies: US-CL-001, US-CL-002 (customer), US-EQ-002 (equipment), US-TR-001 (tariff)
- Leads to: US-RN-002, US-RN-003, US-RN-004, US-RN-005 (rental workflow steps)
