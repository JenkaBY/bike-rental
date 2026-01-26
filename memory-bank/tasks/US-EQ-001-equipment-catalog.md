# [US-EQ-001] - Справочник оборудования (Equipment Catalog)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
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

1. **Equipment Types**: Need a separate EquipmentType entity for reference data
2. **Status Management**: Equipment status is critical for rental availability
3. **Search Capabilities**: Multiple search paths (serial number, QR/NFC UID)
4. **NFC Integration**: QR/NFC UID must be unique for equipment identification

**Architecture Decisions:**

- Equipment and EquipmentType are separate aggregates
- Equipment status changes should publish domain events
- Use enum for EquipmentStatus: AVAILABLE, RENTED, MAINTENANCE, DECOMMISSIONED
- Serial number (порядковый номер) is user-friendly identifier, separate from UUID

**Domain Model:**

```
Equipment (Aggregate Root)
├── id: UUID
├── serialNumber: SerialNumber (value object)
├── nfcUid: NfcUid (value object)
├── equipmentType: EquipmentTypeRef
├── model: String
├── status: EquipmentStatus (enum)
├── commissionedAt: LocalDate
└── condition: String

EquipmentType (Separate Aggregate)
├── id: UUID
├── name: String
└── description: String
```

## Implementation Plan

- [ ] Create Equipment domain model with value objects
- [ ] Create EquipmentType domain model
- [ ] Implement EquipmentStatus enum
- [ ] Create equipment repository
- [ ] Implement CRUD use cases (Create, Update, Get, List)
- [ ] Implement search use cases (by serial, by NFC UID)
- [ ] Create REST endpoints (CRUD + search)
- [ ] Add database migrations (equipment, equipment_types tables)
- [ ] Create indexes for search optimization
- [ ] Implement filtering by type and status
- [ ] Create component tests for all scenarios
- [ ] Write unit tests for domain logic
- [ ] Write WebMvc tests for controllers

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                    | Status      | Updated    | Notes |
|-----|--------------------------------|-------------|------------|-------|
| 1.1 | Create domain models           | Not Started | 2026-01-26 |       |
| 1.2 | Implement CRUD use cases       | Not Started | 2026-01-26 |       |
| 1.3 | Create REST endpoints          | Not Started | 2026-01-26 |       |
| 1.4 | Implement search functionality | Not Started | 2026-01-26 |       |
| 1.5 | Add database migrations        | Not Started | 2026-01-26 |       |
| 1.6 | Create component tests         | Not Started | 2026-01-26 |       |
| 1.7 | Add filtering capabilities     | Not Started | 2026-01-26 |       |

## Progress Log

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
│   ├── model.vo.SerialNumber
│   ├── model.vo.NfcUid
│   ├── model.EquipmentStatus (enum)
│   └── repository.EquipmentRepository
└── infrastructure
    └── persistence
```

**API Endpoints:**

- `POST /api/equipment` - Create equipment
- `PUT /api/equipment/{id}` - Update equipment
- `GET /api/equipment/{id}` - Get by ID
- `GET /api/equipment` - List all with filtering
- `GET /api/equipment/search?serialNumber={num}` - Search by serial
- `GET /api/equipment/search?nfcUid={uid}` - Search by NFC UID
- `GET /api/equipment-types` - List equipment types

**Database Schema:**

```sql
CREATE TABLE equipment_types
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE equipment
(
    id                UUID PRIMARY KEY,
    serial_number     VARCHAR(50) UNIQUE NOT NULL,
    nfc_uid           VARCHAR(100) UNIQUE,
    equipment_type_id UUID REFERENCES equipment_types (id),
    model             VARCHAR(200),
    status            VARCHAR(20)        NOT NULL,
    commissioned_at   DATE,
    condition         TEXT,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE INDEX idx_equipment_serial ON equipment (serial_number);
CREATE INDEX idx_equipment_nfc ON equipment (nfc_uid);
CREATE INDEX idx_equipment_status ON equipment (status);
CREATE INDEX idx_equipment_type ON equipment (equipment_type_id);
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-EQ-001/us-eq-001.md](../../../docs/tasks/us/US-EQ-001/us-eq-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Critical dependency for: US-EQ-002, US-EQ-004, US-RN-001
