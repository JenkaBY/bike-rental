# [US-EQ-001] - Справочник оборудования (Equipment Catalog)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-30  
**Priority:** High  
**Module:** equipment  
**Dependencies:** None

## Original Request

**Как** Администратор  
**Я хочу** управлять справочником всего прокатного оборудования  
**Чтобы** вести учет всего парка оборудования

## User Story Details

**Описание:**  
Система должна поддерживать справочник всего прокатного оборудования.

**Атрибуты оборудования:**

- Уникальный ID
- Порядковый номер (для визуального поиска)
- QR-код (UID)
- Тип оборудования (велосипед, самокат, другое)
- Модель/название
- Статус (доступно, в аренде, на обслуживании, списано)
- Дата ввода в эксплуатацию
- Техническое состояние

**Критерии приемки:**

- Возможность добавления нового оборудования
- Редактирование данных оборудования
- Поиск по порядковому номеру и QR-коду
- Фильтрация по типу и статусу

**Связанные требования:** FR-EQ-001

## Thought Process

This is a foundational user story for the equipment module. It establishes the core equipment catalog that other modules
will depend on. Key considerations:

1. **Equipment Types**: Need a separate EquipmentType entity for reference data with slug-based referencing
2. **Status Management**: Equipment status is critical for rental availability
3. **Search Capabilities**: Multiple search paths (serial number, UID)
4. **UID Integration**: UID must be unique for equipment identification

**Architecture Decisions:**

- Equipment and EquipmentType are separate aggregates
- Equipment status changes should publish domain events
- EquipmentStatus stored as reference data table (not enum) for flexibility
- Status slugs: AVAILABLE, RENTED, MAINTENANCE, DECOMMISSIONED
- Serial number (порядковый номер) is user-friendly identifier, separate from ID
- Use Long for entity IDs instead of UUID for simpler references
- EquipmentType uses slug as unique identifier for cross-table references
- EquipmentStatus uses slug as unique identifier for cross-table references (consistent pattern)

**Domain Model:**

```
Equipment (Aggregate Root)
├── id: Long
├── serialNumber: SerialNumber (value object)
├── uid: Uid (value object)
├── equipmentTypeSlug: String (reference to EquipmentType)
├── statusSlug: String (reference to EquipmentStatus)
├── model: String
├── commissionedAt: LocalDate
└── condition: String

EquipmentType (Separate Aggregate)
├── id: Long
├── slug: String (unique identifier, e.g., "bicycle", "scooter")
├── name: String
└── description: String

EquipmentStatus (Reference Data)
├── id: Long
├── slug: String (unique identifier, e.g., "available", "rented", "maintenance", "decommissioned")
├── name: String
└── description: String
```

## Implementation Plan

- [ ] Create Equipment domain model with value objects
- [ ] Create EquipmentType domain model with slug field
- [ ] Create EquipmentStatus domain model with slug field (reference data)
- [ ] Create equipment repository
- [ ] Implement CRUD use cases (Create, Update, Get, List)
- [ ] Implement search/filter use case (by serial, by UID, by status, by type - combined)
- [ ] Create REST endpoints (CRUD + unified search/filter)
- [ ] Add database migrations (equipment, equipment_types, equipment_statuses tables)
- [ ] Create component tests for all scenarios
- [ ] Write unit tests for domain logic
- [ ] Write WebMvc tests for controllers

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

#### Phase 1: Database Layer (Migrations - Liquibase XML)

**Important:** All database migrations must be created in Liquibase XML format following project conventions.

**Reference Data Tables Rule:** Tables containing predefined system data (equipment_types, equipment_statuses) must
follow special provisioning rules:

- Table structure created in v1/ folder
- Data provisioning created in data/ folder
- Data loaded from CSV files for maintainability

| ID    | Description                                                                       | Status      | Updated    | Notes                                   |
|-------|-----------------------------------------------------------------------------------|-------------|------------|-----------------------------------------|
| 1.1.1 | Create migration v1/equipment_types.create-table.xml                              | Not Started | 2026-01-30 | Table structure only, no data           |
| 1.1.2 | Create migration v1/equipment_statuses.create-table.xml                           | Not Started | 2026-01-30 | Table structure only, no data           |
| 1.1.3 | Create migration v1/equipment.create-table.xml with foreign keys                  | Not Started | 2026-01-30 | References types and statuses by slug   |
| 1.1.4 | Create data/data/equipment_types.csv with initial types (bicycle, scooter, other) | Not Started | 2026-01-30 | CSV format: id,slug,name,description    |
| 1.1.5 | Create data/equipment_types-provisioning.xml loading from CSV                     | Not Started | 2026-01-30 | Uses loadData with csvFile              |
| 1.1.6 | Create data/data/equipment_statuses.csv with 4 statuses                           | Not Started | 2026-01-30 | CSV format: id,slug,name,description    |
| 1.1.7 | Create data/equipment_statuses-provisioning.xml loading from CSV                  | Not Started | 2026-01-30 | Uses loadData with csvFile              |
| 1.1.8 | Update db.changelog-master.xml to include all new changesets in correct order     | Not Started | 2026-01-30 | Order: types, statuses, equipment, data |

#### Phase 2: Domain Layer - Value Objects

| ID    | Description                                      | Status      | Updated    | Notes |
|-------|--------------------------------------------------|-------------|------------|-------|
| 2.1.1 | Create SerialNumber value object with validation | Not Started | 2026-01-30 |       |
| 2.1.2 | Create Uid value object with validation          | Not Started | 2026-01-30 |       |
| 2.1.3 | Write unit tests for SerialNumber validation     | Not Started | 2026-01-30 |       |
| 2.1.4 | Write unit tests for Uid validation              | Not Started | 2026-01-30 |       |

#### Phase 3: Domain Layer - Entities

| ID    | Description                                                        | Status      | Updated    | Notes |
|-------|--------------------------------------------------------------------|-------------|------------|-------|
| 3.1.1 | Create EquipmentType domain entity (id, slug, name, description)   | Not Started | 2026-01-30 |       |
| 3.1.2 | Create EquipmentStatus domain entity (id, slug, name, description) | Not Started | 2026-01-30 |       |
| 3.1.3 | Create Equipment domain entity (all fields)                        | Not Started | 2026-01-30 |       |
| 3.1.4 | Write unit tests for Equipment domain logic                        | Not Started | 2026-01-30 |       |

#### Phase 4: Domain Layer - Repositories (Interfaces)

| ID    | Description                                                | Status      | Updated    | Notes |
|-------|------------------------------------------------------------|-------------|------------|-------|
| 4.1.1 | Create EquipmentTypeRepository interface with findBySlug   | Not Started | 2026-01-30 |       |
| 4.1.2 | Create EquipmentStatusRepository interface with findBySlug | Not Started | 2026-01-30 |       |
| 4.1.3 | Create EquipmentRepository interface with findById         | Not Started | 2026-01-30 |       |
| 4.1.4 | Add findBySerialNumber method to EquipmentRepository       | Not Started | 2026-01-30 |       |
| 4.1.5 | Add findByUid method to EquipmentRepository                | Not Started | 2026-01-30 |       |
| 4.1.6 | Add findAll with filters method to EquipmentRepository     | Not Started | 2026-01-30 |       |

#### Phase 5: Infrastructure Layer - JPA Entities

| ID    | Description                                                      | Status      | Updated    | Notes |
|-------|------------------------------------------------------------------|-------------|------------|-------|
| 5.1.1 | Create EquipmentTypeJpaEntity with JPA annotations               | Not Started | 2026-01-30 |       |
| 5.1.2 | Create EquipmentStatusJpaEntity with JPA annotations             | Not Started | 2026-01-30 |       |
| 5.1.3 | Create EquipmentJpaEntity with JPA annotations and relationships | Not Started | 2026-01-30 |       |

#### Phase 6: Infrastructure Layer - Spring Data Repositories

| ID    | Description                                               | Status      | Updated    | Notes |
|-------|-----------------------------------------------------------|-------------|------------|-------|
| 6.1.1 | Create EquipmentTypeJpaRepository extends JpaRepository   | Not Started | 2026-01-30 |       |
| 6.1.2 | Create EquipmentStatusJpaRepository extends JpaRepository | Not Started | 2026-01-30 |       |
| 6.1.3 | Create EquipmentJpaRepository extends JpaRepository       | Not Started | 2026-01-30 |       |
| 6.1.4 | Add custom query methods to EquipmentJpaRepository        | Not Started | 2026-01-30 |       |

#### Phase 7: Infrastructure Layer - Mappers

| ID    | Description                                                     | Status      | Updated    | Notes |
|-------|-----------------------------------------------------------------|-------------|------------|-------|
| 7.1.1 | Create shared SerialNumberMapper (String ↔ SerialNumber)        | Not Started | 2026-01-30 |       |
| 7.1.2 | Create shared UidMapper (String ↔ Uid)                          | Not Started | 2026-01-30 |       |
| 7.1.3 | Create EquipmentTypeJpaMapper (domain ↔ JPA entity)             | Not Started | 2026-01-30 |       |
| 7.1.4 | Create EquipmentStatusJpaMapper (domain ↔ JPA entity)           | Not Started | 2026-01-30 |       |
| 7.1.5 | Create EquipmentJpaMapper (domain ↔ JPA entity) with VO mappers | Not Started | 2026-01-30 |       |

#### Phase 8: Infrastructure Layer - Repository Adapters

| ID    | Description                                                 | Status      | Updated    | Notes |
|-------|-------------------------------------------------------------|-------------|------------|-------|
| 8.1.1 | Implement EquipmentTypeRepositoryAdapter                    | Not Started | 2026-01-30 |       |
| 8.1.2 | Implement EquipmentStatusRepositoryAdapter                  | Not Started | 2026-01-30 |       |
| 8.1.3 | Implement EquipmentRepositoryAdapter with all query methods | Not Started | 2026-01-30 |       |

#### Phase 9: Application Layer - Use Cases (Interfaces)

| ID    | Description                                                      | Status      | Updated    | Notes |
|-------|------------------------------------------------------------------|-------------|------------|-------|
| 9.1.1 | Create GetEquipmentTypesUseCase interface                        | Not Started | 2026-01-30 |       |
| 9.1.2 | Create GetEquipmentStatusesUseCase interface                     | Not Started | 2026-01-30 |       |
| 9.1.3 | Create CreateEquipmentUseCase interface                          | Not Started | 2026-01-30 |       |
| 9.1.4 | Create UpdateEquipmentUseCase interface                          | Not Started | 2026-01-30 |       |
| 9.1.5 | Create GetEquipmentByIdUseCase interface                         | Not Started | 2026-01-30 |       |
| 9.1.6 | Create SearchEquipmentsUseCase interface (unified search/filter) | Not Started | 2026-01-30 |       |

#### Phase 10: Application Layer - Services (Implementations)

| ID     | Description                                                        | Status      | Updated    | Notes |
|--------|--------------------------------------------------------------------|-------------|------------|-------|
| 10.1.1 | Implement GetEquipmentTypesService                                 | Not Started | 2026-01-30 |       |
| 10.1.2 | Implement GetEquipmentStatusesService                              | Not Started | 2026-01-30 |       |
| 10.1.3 | Implement CreateEquipmentService with validation                   | Not Started | 2026-01-30 |       |
| 10.1.4 | Implement UpdateEquipmentService with validation                   | Not Started | 2026-01-30 |       |
| 10.1.5 | Implement GetEquipmentByIdService                                  | Not Started | 2026-01-30 |       |
| 10.1.6 | Implement SearchEquipmentsService with all filters                 | Not Started | 2026-01-30 |       |
| 10.1.7 | Write unit tests for CreateEquipmentService (all scenarios)        | Not Started | 2026-01-30 |       |
| 10.1.8 | Write unit tests for UpdateEquipmentService (all scenarios)        | Not Started | 2026-01-30 |       |
| 10.1.9 | Write unit tests for SearchEquipmentsService (filter combinations) | Not Started | 2026-01-30 |       |

#### Phase 11: Web Layer - DTOs

| ID     | Description                                                   | Status      | Updated    | Notes |
|--------|---------------------------------------------------------------|-------------|------------|-------|
| 11.1.1 | Create EquipmentTypeResponse DTO                              | Not Started | 2026-01-30 |       |
| 11.1.2 | Create EquipmentStatusResponse DTO                            | Not Started | 2026-01-30 |       |
| 11.1.3 | Create CreateEquipmentRequest DTO with validation annotations | Not Started | 2026-01-30 |       |
| 11.1.4 | Create UpdateEquipmentRequest DTO with validation annotations | Not Started | 2026-01-30 |       |
| 11.1.5 | Create EquipmentResponse DTO                                  | Not Started | 2026-01-30 |       |
| 11.1.6 | Create SearchEquipmentsRequest DTO (optional query params)    | Not Started | 2026-01-30 |       |

#### Phase 12: Web Layer - Mappers

| ID     | Description                                           | Status      | Updated    | Notes |
|--------|-------------------------------------------------------|-------------|------------|-------|
| 12.1.1 | Create EquipmentTypeMapper (domain → response DTO)    | Not Started | 2026-01-30 |       |
| 12.1.2 | Create EquipmentStatusMapper (domain → response DTO)  | Not Started | 2026-01-30 |       |
| 12.1.3 | Create EquipmentCommandMapper (request DTOs → domain) | Not Started | 2026-01-30 |       |
| 12.1.4 | Create EquipmentQueryMapper (domain → response DTO)   | Not Started | 2026-01-30 |       |

#### Phase 13: Web Layer - Controllers

| ID     | Description                                                    | Status      | Updated    | Notes |
|--------|----------------------------------------------------------------|-------------|------------|-------|
| 13.1.1 | Create EquipmentTypeQueryController (GET endpoints)            | Not Started | 2026-01-30 |       |
| 13.1.2 | Create EquipmentStatusQueryController (GET endpoints)          | Not Started | 2026-01-30 |       |
| 13.1.3 | Create EquipmentCommandController (POST, PUT endpoints)        | Not Started | 2026-01-30 |       |
| 13.1.4 | Create EquipmentQueryController (GET by ID endpoint)           | Not Started | 2026-01-30 |       |
| 13.1.5 | Add unified search/filter endpoint to EquipmentQueryController | Not Started | 2026-01-30 |       |

#### Phase 14: Web Layer - Controller Tests (WebMvc)

| ID     | Description                                                         | Status      | Updated    | Notes |
|--------|---------------------------------------------------------------------|-------------|------------|-------|
| 14.1.1 | Write WebMvc tests for GET /api/equipment-types                     | Not Started | 2026-01-30 |       |
| 14.1.2 | Write WebMvc tests for GET /api/equipment-statuses                  | Not Started | 2026-01-30 |       |
| 14.1.3 | Write WebMvc tests for POST /api/equipments (valid request)         | Not Started | 2026-01-30 |       |
| 14.1.4 | Write WebMvc tests for POST /api/equipments (validation errors)     | Not Started | 2026-01-30 |       |
| 14.1.5 | Write WebMvc tests for PUT /api/equipments/{id} (valid request)     | Not Started | 2026-01-30 |       |
| 14.1.6 | Write WebMvc tests for PUT /api/equipments/{id} (validation errors) | Not Started | 2026-01-30 |       |
| 14.1.7 | Write WebMvc tests for GET /api/equipments/{id} (found)             | Not Started | 2026-01-30 |       |
| 14.1.8 | Write WebMvc tests for GET /api/equipments/{id} (not found)         | Not Started | 2026-01-30 |       |
| 14.1.9 | Write WebMvc tests for GET /api/equipments (no filters)             | Not Started | 2026-01-30 |       |
| 14.2.0 | Write WebMvc tests for GET /api/equipments?status=available         | Not Started | 2026-01-30 |       |
| 14.2.1 | Write WebMvc tests for GET /api/equipments?typeSlug=bicycle         | Not Started | 2026-01-30 |       |
| 14.2.2 | Write WebMvc tests for GET /api/equipments?serialNumber=BK-001      | Not Started | 2026-01-30 |       |
| 14.2.3 | Write WebMvc tests for GET /api/equipments?uid=ABC123               | Not Started | 2026-01-30 |       |
| 14.2.4 | Write WebMvc tests for combined filters (status + typeSlug)         | Not Started | 2026-01-30 |       |

#### Phase 15: Component Tests (End-to-End)

| ID     | Description                                                   | Status      | Updated    | Notes |
|--------|---------------------------------------------------------------|-------------|------------|-------|
| 15.1.1 | Create EquipmentTypesComponentTest (list all types)           | Not Started | 2026-01-30 |       |
| 15.1.2 | Create EquipmentStatusesComponentTest (list all statuses)     | Not Started | 2026-01-30 |       |
| 15.1.3 | Create CreateEquipmentComponentTest (happy path)              | Not Started | 2026-01-30 |       |
| 15.1.4 | Create CreateEquipmentComponentTest (duplicate serial number) | Not Started | 2026-01-30 |       |
| 15.1.5 | Create CreateEquipmentComponentTest (invalid type slug)       | Not Started | 2026-01-30 |       |
| 15.1.6 | Create CreateEquipmentComponentTest (invalid status slug)     | Not Started | 2026-01-30 |       |
| 15.1.7 | Create UpdateEquipmentComponentTest (happy path)              | Not Started | 2026-01-30 |       |
| 15.1.8 | Create UpdateEquipmentComponentTest (equipment not found)     | Not Started | 2026-01-30 |       |
| 15.1.9 | Create GetEquipmentComponentTest (by ID)                      | Not Started | 2026-01-30 |       |
| 15.2.0 | Create SearchEquipmentComponentTest (filter by status)        | Not Started | 2026-01-30 |       |
| 15.2.1 | Create SearchEquipmentComponentTest (filter by type)          | Not Started | 2026-01-30 |       |
| 15.2.2 | Create SearchEquipmentComponentTest (search by serial number) | Not Started | 2026-01-30 |       |
| 15.2.3 | Create SearchEquipmentComponentTest (search by UID)           | Not Started | 2026-01-30 |       |
| 15.2.4 | Create SearchEquipmentComponentTest (combined filters)        | Not Started | 2026-01-30 |       |
| 15.2.5 | Create SearchEquipmentComponentTest (no results)              | Not Started | 2026-01-30 |       |

#### Phase 16: Admin Endpoints (Equipment Types)

| ID     | Description                                            | Status      | Updated    | Notes |
|--------|--------------------------------------------------------|-------------|------------|-------|
| 16.1.1 | Create CreateEquipmentTypeUseCase interface            | Not Started | 2026-01-30 |       |
| 16.1.2 | Implement CreateEquipmentTypeService                   | Not Started | 2026-01-30 |       |
| 16.1.3 | Create UpdateEquipmentTypeUseCase interface            | Not Started | 2026-01-30 |       |
| 16.1.4 | Implement UpdateEquipmentTypeService                   | Not Started | 2026-01-30 |       |
| 16.1.5 | Create CreateEquipmentTypeRequest DTO                  | Not Started | 2026-01-30 |       |
| 16.1.6 | Create UpdateEquipmentTypeRequest DTO                  | Not Started | 2026-01-30 |       |
| 16.1.7 | Create EquipmentTypeCommandController (POST, PUT)      | Not Started | 2026-01-30 |       |
| 16.1.8 | Write unit tests for CreateEquipmentTypeService        | Not Started | 2026-01-30 |       |
| 16.1.9 | Write unit tests for UpdateEquipmentTypeService        | Not Started | 2026-01-30 |       |
| 16.2.0 | Write WebMvc tests for POST /api/equipment-types       | Not Started | 2026-01-30 |       |
| 16.2.1 | Write WebMvc tests for PUT /api/equipment-types/{slug} | Not Started | 2026-01-30 |       |
| 16.2.2 | Write component tests for equipment type CRUD          | Not Started | 2026-01-30 |       |

#### Phase 17: Admin Endpoints (Equipment Statuses)

| ID     | Description                                               | Status      | Updated    | Notes |
|--------|-----------------------------------------------------------|-------------|------------|-------|
| 17.1.1 | Create CreateEquipmentStatusUseCase interface             | Not Started | 2026-01-30 |       |
| 17.1.2 | Implement CreateEquipmentStatusService                    | Not Started | 2026-01-30 |       |
| 17.1.3 | Create UpdateEquipmentStatusUseCase interface             | Not Started | 2026-01-30 |       |
| 17.1.4 | Implement UpdateEquipmentStatusService                    | Not Started | 2026-01-30 |       |
| 17.1.5 | Create CreateEquipmentStatusRequest DTO                   | Not Started | 2026-01-30 |       |
| 17.1.6 | Create UpdateEquipmentStatusRequest DTO                   | Not Started | 2026-01-30 |       |
| 17.1.7 | Create EquipmentStatusCommandController (POST, PUT)       | Not Started | 2026-01-30 |       |
| 17.1.8 | Write unit tests for CreateEquipmentStatusService         | Not Started | 2026-01-30 |       |
| 17.1.9 | Write unit tests for UpdateEquipmentStatusService         | Not Started | 2026-01-30 |       |
| 17.2.0 | Write WebMvc tests for POST /api/equipment-statuses       | Not Started | 2026-01-30 |       |
| 17.2.1 | Write WebMvc tests for PUT /api/equipment-statuses/{slug} | Not Started | 2026-01-30 |       |
| 17.2.2 | Write component tests for equipment status CRUD           | Not Started | 2026-01-30 |       |

#### Phase 18: Documentation and Finalization

| ID     | Description                                  | Status      | Updated    | Notes |
|--------|----------------------------------------------|-------------|------------|-------|
| 18.1.1 | Update API documentation with all endpoints  | Not Started | 2026-01-30 |       |
| 18.1.2 | Verify all acceptance criteria are met       | Not Started | 2026-01-30 |       |
| 18.1.3 | Run full test suite and verify coverage      | Not Started | 2026-01-30 |       |
| 18.1.4 | Update memory bank with implementation notes | Not Started | 2026-01-30 |       |
| 18.1.5 | Mark US-EQ-001 as complete                   | Not Started | 2026-01-30 |       |

## Progress Log

### 2026-01-30 (Evening)

- Updated database migration specifications to follow Liquibase XML format:
    - All migrations must be in XML format (not SQL)
    - Follow Liquibase naming conventions: {table}.{action}-table_{description}
    - Placed in v1/ folder for table structures
    - Added special rules for predefined reference data tables:
        - equipment_types and equipment_statuses are system-defined reference data
        - Table structures in v1/ folder (structure only, no data)
        - Data provisioning in data/ folder (separate from structure)
        - Data stored in CSV files for maintainability: data/data/{table}.csv
        - Provisioning XML files use loadData to import CSV: data/{table}-provisioning.xml
    - Updated Phase 1 subtasks to reflect Liquibase workflow (8 subtasks now)
    - Included CSV format examples for reference data
    - Master changelog must include changesets in order: types, statuses, equipment, data

### 2026-01-30 (Afternoon)

- Broke down subtasks into highly granular, actionable items (18 phases, 100+ subtasks):
  // ...existing entries...
    - Phase 1: Database migrations (5 subtasks)
    - Phase 2: Domain value objects (4 subtasks)
    - Phase 3: Domain entities (4 subtasks)
    - Phase 4: Repository interfaces (6 subtasks)
    - Phase 5: JPA entities (3 subtasks)
    - Phase 6: Spring Data repositories (4 subtasks)
    - Phase 7: Infrastructure mappers (5 subtasks)
    - Phase 8: Repository adapters (3 subtasks)
    - Phase 9: Use case interfaces (6 subtasks)
    - Phase 10: Service implementations with unit tests (9 subtasks)
    - Phase 11: Web DTOs (6 subtasks)
    - Phase 12: Web mappers (4 subtasks)
    - Phase 13: Controllers (5 subtasks)
    - Phase 14: WebMvc tests (14 subtasks)
    - Phase 15: Component tests (15 subtasks)
    - Phase 16: Admin equipment types endpoints (12 subtasks)
    - Phase 17: Admin equipment statuses endpoints (12 subtasks)
    - Phase 18: Documentation and finalization (5 subtasks)
- Each subtask is now small, focused, and can be picked up independently
- Organized by architectural layer for clear progression
- Suitable for less sophisticated LLMs to execute

### 2026-01-30 (Morning)

- Updated technical specifications based on architecture decisions:
    - Changed entity IDs from UUID to Long for simpler references
    - Renamed NfcUid to Uid throughout the specification
    - Updated EquipmentType to use slug as unique identifier
    - Changed EquipmentStatus from enum to reference data table with slug field
    - Added equipment_statuses table and corresponding API endpoints
    - Equipment table references status_slug instead of storing status directly
    - Consistent pattern: both equipment_types and equipment_statuses use slug
    - Removed timestamp fields from equipment_types table
    - Updated API endpoints to follow REST principles (plural form: /api/equipments)
    - Combined all search/filter operations into single GET /api/equipments endpoint with optional query parameters
    - Removed database indexes (no premature optimization)
    - Removed search optimization subtask from implementation plan

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, independent task that can be started after US-CL-002

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.equipment
├── web.command
│   ├── EquipmentCommandController
│   ├── dto.CreateEquipmentRequest
│   └── dto.UpdateEquipmentRequest
├── web.query
│   ├── EquipmentQueryController
│   ├── dto.EquipmentResponse
│   └── dto.EquipmentSearchRequest
├── application
│   ├── usecase.CreateEquipmentUseCase
│   ├── usecase.UpdateEquipmentUseCase
│   ├── usecase.SearchEquipmentUseCase
│   └── service.*
├── domain
│   ├── model.Equipment
│   ├── model.EquipmentType
│   ├── model.EquipmentStatus
│   ├── model.vo.SerialNumber
│   ├── model.vo.Uid
│   └── repository.EquipmentRepository
└── infrastructure
    └── persistence
```

**Liquibase Migration Structure:**

```
service/src/main/resources/db/changelog/
├── db.changelog-master.xml                      # Master changelog
├── v1/                                          # Version 1 changelogs
│   ├── equipment_types.create-table.xml         # Table structure
│   ├── equipment_statuses.create-table.xml      # Table structure
│   └── equipment.create-table.xml               # Table structure with FKs
└── data/                                        # Predefined data
    ├── data/                                    # CSV data files
    │   ├── equipment_types.csv                  # Reference data
    │   └── equipment_statuses.csv               # Reference data
    ├── equipment_types-provisioning.xml         # Load CSV data
    └── equipment_statuses-provisioning.xml      # Load CSV data
```

**Liquibase Conventions:**

| Element        | Convention                      | Example                                 |
|----------------|---------------------------------|-----------------------------------------|
| ChangeSet ID   | `{table}.{action}-table`        | `equipment.create-table`                |
| File Name      | `{table}.{action}-table.xml`    | `equipment.create-table.xml`            |
| Author         | `bikerental`                    | Consistent across all changesets        |
| CSV File       | `data/data/{table}.csv`         | `data/data/equipment_types.csv`         |
| Provision File | `data/{table}-provisioning.xml` | `data/equipment_types-provisioning.xml` |

**Master Changelog Order:**

1. Table structures (v1/equipment_types, v1/equipment_statuses, v1/equipment)
2. Data provisioning (data/equipment_types-provisioning, data/equipment_statuses-provisioning)

**Predefined Data Rules:**

- **Reference tables** (equipment_types, equipment_statuses): Data loaded from CSV files
- **Transactional tables** (equipment): No predefined data, populated at runtime
- **CSV format**: First row is header, subsequent rows are data
- **CSV columns**: Must match table column names exactly

**API Endpoints:**

**Equipment:**

- `POST /api/equipments` - Create equipment
- `PUT /api/equipments/{id}` - Update equipment
- `GET /api/equipments/{id}` - Get equipment by ID
- `GET /api/equipments` - List/search equipments with optional query parameters:
    - `?status={status}` - Filter by status (AVAILABLE, RENTED, MAINTENANCE, DECOMMISSIONED)
    - `?typeSlug={typeSlug}` - Filter by equipment type slug
    - `?serialNumber={num}` - Search by serial number (exact match)
    - `?uid={uid}` - Search by UID (exact match)
    - Multiple parameters can be combined

**Equipment Types:**

- `GET /api/equipment-types` - List all equipment types
- `POST /api/equipment-types` - Create equipment type
- `PUT /api/equipment-types/{slug}` - Update equipment type by slug

**Equipment Statuses:**

- `GET /api/equipment-statuses` - List all equipment statuses
- `POST /api/equipment-statuses` - Create equipment status
- `PUT /api/equipment-statuses/{slug}` - Update equipment status by slug

**Database Schema:**

**Liquibase Migrations (XML Format):**

```xml
<!-- v1/equipment_types.create-table.xml -->
<changeSet id="equipment_types.create-table" author="bikerental">
    <createTable tableName="equipment_types">
        <column name="id" type="SERIAL">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="slug" type="VARCHAR(50)">
            <constraints nullable="false" unique="true"/>
        </column>
        <column name="name" type="VARCHAR(100)">
            <constraints nullable="false"/>
        </column>
        <column name="description" type="TEXT"/>
    </createTable>
</changeSet>

        <!-- v1/equipment_statuses.create-table.xml -->
<changeSet id="equipment_statuses.create-table" author="bikerental">
<createTable tableName="equipment_statuses">
    <column name="id" type="SERIAL">
        <constraints primaryKey="true" nullable="false"/>
    </column>
    <column name="slug" type="VARCHAR(50)">
        <constraints nullable="false" unique="true"/>
    </column>
    <column name="name" type="VARCHAR(100)">
        <constraints nullable="false"/>
    </column>
    <column name="description" type="TEXT"/>
</createTable>
</changeSet>

        <!-- v1/equipment.create-table.xml -->
<changeSet id="equipment.create-table" author="bikerental">
<createTable tableName="equipment">
    <column name="id" type="BIGSERIAL">
        <constraints primaryKey="true" nullable="false"/>
    </column>
    <column name="serial_number" type="VARCHAR(50)">
        <constraints nullable="false" unique="true"/>
    </column>
    <column name="uid" type="VARCHAR(100)">
        <constraints unique="true"/>
    </column>
    <column name="equipment_type_slug" type="VARCHAR(50)">
        <constraints nullable="false"/>
    </column>
    <column name="status_slug" type="VARCHAR(50)">
        <constraints nullable="false"/>
    </column>
    <column name="model" type="VARCHAR(200)"/>
    <column name="commissioned_at" type="DATE"/>
    <column name="condition" type="TEXT"/>
    <column name="created_at" type="TIMESTAMP">
        <constraints nullable="false"/>
    </column>
    <column name="updated_at" type="TIMESTAMP"/>
</createTable>

<addForeignKeyConstraint
        baseTableName="equipment"
        baseColumnNames="equipment_type_slug"
        constraintName="fk_equipment_type"
        referencedTableName="equipment_types"
        referencedColumnNames="slug"/>

<addForeignKeyConstraint
        baseTableName="equipment"
        baseColumnNames="status_slug"
        constraintName="fk_equipment_status"
        referencedTableName="equipment_statuses"
        referencedColumnNames="slug"/>
</changeSet>
```

**Predefined Data (CSV Format):**

```csv
# data/data/equipment_types.csv
id,slug,name,description
1,bicycle,Bicycle,Standard bicycles for rental
2,scooter,Scooter,Electric and manual scooters
3,other,Other,Other equipment types

# data/data/equipment_statuses.csv
id,slug,name,description
1,available,Available,Equipment is ready for rental
2,rented,Rented,Equipment is currently in active rental
3,maintenance,Under Maintenance,Equipment is being serviced or repaired
4,decommissioned,Decommissioned,Equipment has been removed from service
```

```xml
<!-- data/equipment_types-provisioning.xml -->
<changeSet id="equipment_types.provision-data" author="bikerental">
    <loadData tableName="equipment_types"
              file="db/changelog/data/data/equipment_types.csv"
              relativeToChangelogFile="false">
        <column name="id" type="numeric"/>
        <column name="slug" type="string"/>
        <column name="name" type="string"/>
        <column name="description" type="string"/>
    </loadData>
</changeSet>

        <!-- data/equipment_statuses-provisioning.xml -->
<changeSet id="equipment_statuses.provision-data" author="bikerental">
<loadData tableName="equipment_statuses"
          file="db/changelog/data/data/equipment_statuses.csv"
          relativeToChangelogFile="false">
    <column name="id" type="numeric"/>
    <column name="slug" type="string"/>
    <column name="name" type="string"/>
    <column name="description" type="string"/>
</loadData>
</changeSet>
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-EQ-001/us-eq-001.md](../../../docs/tasks/us/US-EQ-001/us-eq-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Critical dependency for: US-EQ-002, US-EQ-004, US-RN-001
