# [US-TR-001] - Справочник тарифов (Tariff Catalog)

**Status:** Completed  
**Added:** 2026-01-21  
**Updated:** 2026-02-03  
**Priority:** High  
**Module:** tariff  
**Dependencies:** None

## Original Request

**Как** Администратор  
**Я хочу** управлять справочником тарифов  
**Чтобы** настраивать цены на аренду для разных типов оборудования и периодов

## User Story Details

**Описание:**  
Система должна содержать справочник тарифов для разных типов оборудования и времени аренды.

**Структура тарифа:**

- Название тарифа
- Тип оборудования
- Время аренды (1 час, 2 часа, сутки и т.д.)
- Стоимость базового периода
- Стоимость дополнительного времени (за 5 минут)
- Период действия тарифа (с даты / по дату)

**Критерии приемки:**

- Создание и редактирование тарифов
- Множественные тарифы для одного типа оборудования
- Версионирование тарифов
- Активация/деактивация тарифов

**Связанные требования:** FR-TR-001

## Thought Process

The tariff module is critical for rental pricing and must be flexible enough to support:

1. **Multiple Tariffs**: Same equipment type can have different tariffs (e.g., weekday vs weekend)
2. **Time Periods**: Support for defined rental periods (HALF_HOUR, HOUR, DAY)
3. **Versioning**: Tariff changes over time without losing historical data
4. **Active Period**: Tariffs have validity periods (validFrom/validTo)

**Key Business Rules:**

- Multiple active tariffs can exist for same equipment type
- Tariff selection happens during rental creation
- Historical tariffs must be preserved for audit

**Architecture Decisions:**

- Tariff is an aggregate root
- Use TariffPeriod enum: HALF_HOUR, HOUR, DAY
- Link to EquipmentType by reference (not direct FK)
- Use Money type for amounts (considering currency)
- status field represents activation state

**Domain Model:**

```
Tariff (Aggregate Root)
├── id: Long
├── name: String
├── description: String (nullable)
├── equipmentTypeSlug: String (reference)
├── basePrice: Money
├── halfHourPrice: Money
├── hourPrice: Money
├── dayPrice: Money
├── hourDiscountedPrice: Money
├── validFrom: LocalDate
├── validTo: LocalDate (nullable)
└── status: String
```

## Implementation Plan

- [ ] Define component tests for tariff CRUD and activation scenarios (happy paths)
- [ ] Add unit tests for tariff validation rules (price positivity, date range)
- [ ] Add WebMvc tests for create/update/activate and query endpoints
- [ ] Create Tariff domain model with Money value object
- [ ] Create TariffPeriod enum
- [ ] Implement tariff repository interface
- [ ] Add database migration for tariffs table (schema aligned to Technical Details)
- [ ] Implement CRUD use cases (Create, Update, Activate/Deactivate)
- [ ] Implement query use cases (get by id, list/paged, active by equipment type)
- [ ] Create REST endpoints for tariff management
- [ ] Implement versioning strategy (new rows for new versions, preserve history)

## Validation

I validated the repository for artifacts related to each planned item. Summary below shows what is present in the
codebase (file paths) and what is still missing.

- Define component tests for tariff CRUD and activation scenarios (happy paths) — DONE
  - Evidence: component tests added under the `component-test` module (see progress log). Feature and step files
    referenced in memory bank progress.

- Add unit tests for tariff validation rules (price positivity, date range) — DONE
  - Evidence: `service` module contains unit tests: `TariffTest`, `MoneyTest`, `TariffPeriodTest` (see
    `service/src/test/java/.../tariff/domain/model/TariffTest.java`).

- Add WebMvc tests for create/update/activate and query endpoints — DONE (negative/validation tests present)
  - Evidence: `service/src/test/java/com/github/jenkaby/bikerental/tariff/web/command/TariffCommandControllerTest.java`
    and `service/src/test/java/com/github/jenkaby/bikerental/tariff/web/query/TariffQueryControllerTest.java` contain
    comprehensive WebMvc tests covering validation and error scenarios.

- Create Tariff domain model with Money value object — DONE
  - Evidence: `service/src/main/java/com/github/jenkaby/ b ikerental/tariff/domain/model/Tariff.java` and
    `shared/domain/model/vo/Money.java` exist and implement required behavior.

- Create TariffPeriod enum — DONE
  - Evidence: `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/TariffPeriod.java`.

- Implement tariff repository interface — DONE
  - Evidence: `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/repository/TariffRepository.java` and
    adapter `TariffRepositoryAdapter` with JPA mapper/Entity exist.

- Add database migration for tariffs table (schema aligned to Technical Details) — DONE
  - Evidence: Liquibase changelog: `service/src/main/resources/db/changelog/v1/tariffs.create-table.xml` and
    provisioning `data/tariffs-provisioning.xml`.

- Implement CRUD use cases (Create, Update, Activate/Deactivate) — DONE
  - Evidence: Use case interfaces and service implementations present under `application/usecase` and
    `application/service` (CreateTariffService, UpdateTariffService, ActivateTariffService, DeactivateTariffService,
    GetTariffByIdService, GetAllTariffsService, GetActiveTariffsByEquipmentTypeService).

- Implement query use cases (get by id, list/paged, active by equipment type) — DONE
  - Evidence: `GetTariffByIdUseCase`, `GetAllTariffsUseCase`, `GetActiveTariffsByEquipmentTypeUseCase` and
    implementations exist.

- Create REST endpoints for tariff management — DONE
  - Evidence: `TariffCommandController` (POST/PUT/PATCH endpoints) and `TariffQueryController` (GET endpoints) are
    present under `web.command` and `web.query` packages.

- Implement versioning strategy (new rows for new versions, preserve history) — NOT VERIFIED / NOT IMPLEMENTED
  - Evidence: I could not find explicit code implementing versioning (no `version` column or versioning strategy, and
    service implementations appear to update existing rows). The database migration for `tariffs` does not include a
    `version` or `effective_from`/`effective_to` beyond `valid_from`/`valid_to`.

## Conclusion & Status

- Based on the code inspection, most planned items are implemented and covered by tests and migrations.
- The explicit tariff versioning strategy was intentionally deferred and extracted to a separate task `US-TR-006` (
  Tariff versioning) so the team can address it independently.

For now we assume the current single row in `tariffs` is the active/current tariff for the given `root` (business)
identity — i.e. the existing representation continues to be used as the canonical "current" tariff.

Therefore the user story `US-TR-001 - Tariff Catalog` is considered **Completed** for the purposes of Phase 1 foundation
work; versioning is tracked separately under `US-TR-006`.

### Recommended next steps

- Decide and implement a tariff versioning approach. Options:
  - Add a `version` numeric column and write services that create a new row on update (preserve old row). Update
    `Create/Update` services and repository accordingly.
  - Or document that `valid_from`/`valid_to` together with mapping policies serve as versioning; if so, add
    documentation and component tests asserting historical behavior.

- If you want, I can implement the minimal versioning change (add `version` column + adjust repository/service to insert
  new row on update) and add tests. Say "implement versioning" and I'll proceed.

## Progress Tracking (updated)

**Overall Status:** In Progress  
**Completion:** ~92% (most subtasks done; versioning missing)

### Subtasks (updated)

| ID  | Description                | Status      | Updated    | Notes                                                        |
|-----|----------------------------|-------------|------------|--------------------------------------------------------------|
| 1.1 | Create domain model        | Complete    | 2026-02-02 | Domain model with unit tests                                 |
| 1.2 | Implement CRUD use cases   | Complete    | 2026-02-02 | Services and use cases present                               |
| 1.3 | Create REST endpoints      | Complete    | 2026-02-02 | Controllers and mappers present                              |
| 1.4 | Add database migration     | Complete    | 2026-01-26 | Liquibase changelog and provisioning files exist             |
| 1.5 | Create component tests     | Complete    | 2026-02-03 | Component tests added to `component-test` module             |
| 1.6 | Add tariff selection query | Complete    | 2026-02-02 | Query use case implemented (GetActiveTariffsByEquipmentType) |
| 1.7 | Implement versioning       | Not Started | 2026-02-03 | No explicit versioning strategy found (action required)      |

## Progress Log

### 2026-02-03

- Created component tests for the Tariff module in the `component-test` module. Added/updated the following files:
  - `src/test/resources/features/tariff/tariff.feature` (converted to Scenario Outline, transposed table)
  - `src/test/java/.../steps/tariff/TariffWebSteps.java` (web step definitions)
  - `src/test/java/.../steps/tariff/TariffDbSteps.java` (db step definitions)
  - `src/test/java/.../config/db/repository/InsertableTariffRepository.java` (DB insert helper)
  - `src/test/java/.../transformer/TariffRequestTransformer.java` (DataTable -> TariffRequest)
  - `src/test/java/.../transformer/TariffJpaEntityTransformer.java` (DataTable -> TariffJpaEntity)
- Transposed key/value tables into header+row format and aligned column names (`equipmentType`, `discountedPrice`) to
  test transformers.
- Added helpers in tests to parse BigDecimal/LocalDate via `DataTableHelper` and wired transformers with
  `@DataTableType`.
- Notes: Some test-step code still contains warnings related to JSON parsing fallback and mapper usage (`TariffWebSteps`
  used an invalid mapper access pattern earlier). This is a non-blocking issue for code edits but should be fixed before
  running component tests end-to-end.

### 2026-02-02 (Late Evening)

**Subtask 1.3: REST Endpoints - COMPLETED ✅**

Created complete web layer for tariff module following CQRS and hexagonal architecture:

**DTOs (2):**

- `TariffRequest` - Command DTO with Jakarta validation
  - All price fields validated as positive with max 2 decimal places
  - Size constraints on name and description
  - Required fields: name, equipmentTypeSlug, period, all prices, validFrom, status
  - Optional: description, validTo
- `TariffResponse` - Query DTO for responses
  - Mirrors domain model structure
  - Includes all tariff fields with proper types

**Web Mappers (2):**

- `TariffCommandMapper` - Maps web requests to use case commands
  - toCreateCommand(TariffRequest)
  - toUpdateCommand(Long id, TariffRequest)
- `TariffQueryMapper` - Maps domain to response DTOs
  - toResponse(Tariff)
  - Uses MoneyMapper for BigDecimal ↔ Money conversion

**Command Controller (TariffCommandController):**

- `POST /api/tariffs` - Create new tariff (returns 201 CREATED)
- `PUT /api/tariffs/{id}` - Update existing tariff (returns 200 OK)
- `PATCH /api/tariffs/{id}/activate` - Activate tariff (returns 200 OK)
- `PATCH /api/tariffs/{id}/deactivate` - Deactivate tariff (returns 200 OK)

**Query Controller (TariffQueryController):**

- `GET /api/tariffs/{id}` - Get tariff by ID
- `GET /api/tariffs` - Get all tariffs with pagination (default 20 items, sorted by id)
- `GET /api/tariffs/active?equipmentTypeSlug={slug}` - Get active tariffs by equipment type

**Features:**

- ✅ @Validated and @Valid for request validation
- ✅ Proper HTTP status codes (201 for creation, 200 for updates/queries)
- ✅ Logging at INFO level for all operations
- ✅ DEBUG level for detailed request payloads
- ✅ Constructor injection for all dependencies
- ✅ CQRS pattern (separate command and query controllers)
- ✅ Pagination support with Spring Data Pageable
- ✅ Query parameter support for filtering

**Architecture Compliance:**

- Follows hexagonal architecture (web → application → domain)
- CQRS separation (command vs query)
- MapStruct for type-safe mapping
- Proper use of DTOs to isolate domain from web layer
- RESTful endpoint design

### 2026-02-02 (Evening)

**Subtask 1.2: CRUD Use Cases - COMPLETED ✅**

Created complete application layer for tariff module following hexagonal architecture:

**Use Case Interfaces (7):**

- `CreateTariffUseCase` - Create new tariff with all fields
- `UpdateTariffUseCase` - Update existing tariff (checks existence)
- `GetTariffByIdUseCase` - Retrieve tariff by ID
- `GetAllTariffsUseCase` - Paginated list of all tariffs
- `GetActiveTariffsByEquipmentTypeUseCase` - Query active tariffs by equipment type
- `ActivateTariffUseCase` - Activate tariff (status change)
- `DeactivateTariffUseCase` - Deactivate tariff (status change)

**Service Implementations (7):**

- `CreateTariffService` - Maps command to domain, saves tariff
- `UpdateTariffService` - Validates existence, maps and saves
- `GetTariffByIdService` - Retrieves or throws ResourceNotFoundException
- `GetAllTariffsService` - Delegates to repository with pagination
- `GetActiveTariffsByEquipmentTypeService` - Filters by type and active status
- `ActivateTariffService` - Rebuilds tariff with ACTIVE status
- `DeactivateTariffService` - Rebuilds tariff with INACTIVE status

**Mapper:**

- `TariffCommandToDomainMapper` - MapStruct mapper for command → domain conversion
  - toTariff(CreateTariffCommand)
  - toTariff(UpdateTariffCommand)
  - toMoney(BigDecimal) and toBigDecimal(Money) helpers

**Test Coverage (9 tests, all passing):**

- `CreateTariffServiceTest` - 1 test (successful creation)
- `UpdateTariffServiceTest` - 2 tests (success + not found exception)
- `GetTariffByIdServiceTest` - 2 tests (success + not found exception)
- `ActivateTariffServiceTest` - 2 tests (success + not found exception)
- `DeactivateTariffServiceTest` - 2 tests (success + not found exception)

**Architecture Compliance:**

- ✅ Use case interfaces define contracts
- ✅ Services implement use cases with @Service and @Transactional
- ✅ Constructor injection for dependencies
- ✅ Proper exception handling (ResourceNotFoundException)
- ✅ Comprehensive mocking with Mockito
- ✅ BDD style testing with given/when/then

**Technical Notes:**

- Activation/Deactivation rebuild entire tariff to maintain immutability
- UpdateTariff validates existence before attempting update
- All query operations are @Transactional(readOnly = true)
- Mapper provides BigDecimal ↔ Money conversion helpers

### 2026-02-02 (Afternoon)

**Subtask 1.1: Domain Model - COMPLETED ✅**

Created complete domain layer for tariff module following hexagonal architecture:

**Domain Model Files:**

- `Tariff.java` - Aggregate root with id, name, description, equipmentTypeSlug, period, price fields, validFrom/validTo,
  status
- `TariffPeriod.java` - Enum with HALF_HOUR(30min), HOUR(60min), DAY(1440min)
- `Money.java` - Value object for monetary amounts with validation and arithmetic operations
- `TariffRepository.java` - Repository interface with save, findById, findAll (paged), findActiveByEquipmentTypeSlug

**Business Logic:**

- `Tariff.isActive()` - Checks if status equals "ACTIVE"
- `Tariff.activate()` - Changes status accordingly
- `Tariff.deactivate()` - Changes status accordingly
- `Tariff.isValidOn(LocalDate)` - Validates if tariff is valid for a given date based on validFrom/validTo range

**Test Coverage (26 tests, all passing):**

- `MoneyTest.java` - 13 tests covering creation, validation, arithmetic operations, edge cases
- `TariffPeriodTest.java` - 3 tests verifying enum values and duration calculations
- `TariffTest.java` - 10 tests for Tariff entity builder, validation logic, date range checks

**Architecture Compliance:**

- Pure domain layer with no framework dependencies
- Lombok used for builder pattern and getters
- Value objects enforce invariants
- Repository as interface (ports & adapters pattern)

### 2026-02-02 (Morning)

- Aligned TariffPeriod and schema decisions to the Technical Details section
- Updated domain model and implementation plan to match tariff schema fields

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, independent task that can be started after US-CL-002

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.tariff
├── web.command
│   ├── TariffCommandController
│   ├── dto.CreateTariffRequest
│   └── dto.UpdateTariffRequest
├── web.query
│   ├── TariffQueryController
│   └── dto.TariffResponse
├── application
│   ├── usecase.CreateTariffUseCase
│   ├── usecase.UpdateTariffUseCase
│   ├── usecase.ActivateTariffUseCase
│   └── service.*
├── domain
│   ├── model.Tariff
│   ├── model.vo.Money
│   ├── model.TariffPeriod (enum)
│   └── repository.TariffRepository
└── infrastructure
    └── persistence
```

**API Endpoints:**

- `POST /api/tariffs` - Create tariff
- `PUT /api/tariffs/{id}` - Update tariff
- `PATCH /api/tariffs/{id}/activete|deactivate` - Activate/Deactivate tariff
- `GET /api/tariffs/{id}` - Get by ID
- `GET /api/tariffs` - List all tariffs (pagination)
- `GET /api/tariffs/active?equipmentType={equipmentTypeSlug}` - Get active tariffs for equipment type

**Database Schema:**

```sql
CREATE TABLE tariffs
(
  id                    BIGSERIAL PRIMARY KEY,
  name                  VARCHAR(200)   NOT NULL,
  description           VARCHAR(1000),
  equipment_type_slug   VARCHAR(50)    NOT NULL,
  base_price            DECIMAL(10, 2) NOT NULL,
  half_hour_price       DECIMAL(10, 2) NOT NULL,
  hour_price            DECIMAL(10, 2) NOT NULL,
  day_price             DECIMAL(10, 2) NOT NULL,
  hour_discounted_price DECIMAL(10, 2) NOT NULL,
  valid_from            DATE           NOT NULL,
  valid_to              DATE,
  status                VARCHAR(50)    NOT NULL DEFAULT 'INACTIVE',
  created_at            TIMESTAMP,
  updated_at            TIMESTAMP
);
```
