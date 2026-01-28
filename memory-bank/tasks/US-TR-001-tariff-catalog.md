# [US-TR-001] - Справочник тарифов (Tariff Catalog)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
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
2. **Time Periods**: Support for various rental periods (1h, 2h, day)
3. **Versioning**: Tariff changes over time without losing historical data
4. **Active Period**: Tariffs have validity periods (validFrom/validTo)

**Key Business Rules:**

- Multiple active tariffs can exist for same equipment type
- Tariff selection happens during rental creation
- Extra time charged in 5-minute increments
- Historical tariffs must be preserved for audit

**Architecture Decisions:**

- Tariff is an aggregate root
- Use TariffPeriod enum: HOUR_1, HOUR_2, HOUR_4, DAY, WEEK
- Link to EquipmentType by reference (not direct FK)
- Use Money type for amounts (considering currency)
- isActive flag separate from validity period

**Domain Model:**

```
Tariff (Aggregate Root)
├── id: UUID
├── name: String
├── equipmentTypeId: UUID (reference)
├── period: TariffPeriod (enum)
├── basePrice: Money
├── extraTimePricePer5Min: Money
├── validFrom: LocalDate
├── validTo: LocalDate (nullable)
└── isActive: Boolean
```

## Implementation Plan

- [ ] Create Tariff domain model with Money value object
- [ ] Create TariffPeriod enum
- [ ] Implement tariff repository
- [ ] Create CRUD use cases (Create, Update, Activate/Deactivate)
- [ ] Implement tariff selection logic (active + valid period)
- [ ] Create REST endpoints for tariff management
- [ ] Add database migration for tariffs table
- [ ] Implement versioning strategy
- [ ] Create component tests for tariff CRUD
- [ ] Write unit tests for validation logic
- [ ] Write WebMvc tests for endpoints
- [ ] Add query endpoint for active tariffs by equipment type

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 1.1 | Create domain model        | Not Started | 2026-01-26 |       |
| 1.2 | Implement CRUD use cases   | Not Started | 2026-01-26 |       |
| 1.3 | Create REST endpoints      | Not Started | 2026-01-26 |       |
| 1.4 | Add database migration     | Not Started | 2026-01-26 |       |
| 1.5 | Implement versioning       | Not Started | 2026-01-26 |       |
| 1.6 | Create component tests     | Not Started | 2026-01-26 |       |
| 1.7 | Add tariff selection query | Not Started | 2026-01-26 |       |

## Progress Log

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
- `PATCH /api/tariffs/{id}/activate` - Activate tariff
- `PATCH /api/tariffs/{id}/deactivate` - Deactivate tariff
- `GET /api/tariffs/{id}` - Get by ID
- `GET /api/tariffs` - List all tariffs
- `GET /api/tariffs/active?equipmentTypeId={id}` - Get active tariffs for equipment type

**Database Schema:**

```sql
CREATE TABLE tariffs
(
    id                        UUID PRIMARY KEY,
    name                      VARCHAR(200)   NOT NULL,
    equipment_type_id         UUID           NOT NULL,
    period VARCHAR (20) NOT NULL,
    base_price                DECIMAL(10, 2) NOT NULL,
    extra_time_price_per_5min DECIMAL(10, 2) NOT NULL,
    valid_from                DATE           NOT NULL,
    valid_to                  DATE,
    is_active                 BOOLEAN DEFAULT TRUE,
    created_at                TIMESTAMP,
    updated_at                TIMESTAMP
);

CREATE INDEX idx_tariffs_equipment_type ON tariffs (equipment_type_id);
CREATE INDEX idx_tariffs_active ON tariffs (is_active);
CREATE INDEX idx_tariffs_validity ON tariffs (valid_from, valid_to);
```

**TariffPeriod Enum:**

```java
public enum TariffPeriod {
    HOUR_1(60),
    HOUR_2(120),
    HOUR_4(240),
    DAY(1440),
    WEEK(10080);

    private final int minutes;
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-TR-001/us-tr-001.md](../../../docs/tasks/us/US-TR-001/us-tr-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Critical dependency for: US-RN-001, US-RN-002, US-TR-002, US-AD-003
