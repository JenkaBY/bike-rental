# [US-RN-001] - Создание записи аренды (Create Rental Record)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
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
├── id: UUID
├── customerId: UUID (reference)
├── equipmentId: UUID (reference)
├── tariffId: UUID (reference)
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

- [ ] Create Rental domain model with RentalStatus enum
- [ ] Implement CreateRentalUseCase (creates DRAFT)
- [ ] Implement SelectCustomerCommand
- [ ] Implement SelectEquipmentCommand with availability check
- [ ] Implement SelectTariffCommand
- [ ] Implement CalculateEstimatedCostCommand
- [ ] Create REST endpoints for rental creation workflow
- [ ] Add database migration for rentals table
- [ ] Implement draft rental persistence
- [ ] Create validation for step sequencing
- [ ] Publish RentalCreated domain event
- [ ] Create component tests for rental creation flow
- [ ] Write unit tests for validation logic
- [ ] Write WebMvc tests for endpoints

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                     | Status      | Updated    | Notes                           |
|-----|---------------------------------|-------------|------------|---------------------------------|
| 1.1 | Create Rental domain model      | Not Started | 2026-01-26 | Core aggregate                  |
| 1.2 | Implement draft rental creation | Not Started | 2026-01-26 |                                 |
| 1.3 | Add customer selection          | Not Started | 2026-01-26 | Depends on US-CL-001, US-CL-002 |
| 1.4 | Add equipment selection         | Not Started | 2026-01-26 | Depends on US-EQ-002            |
| 1.5 | Add tariff selection            | Not Started | 2026-01-26 | Depends on US-TR-001            |
| 1.6 | Implement cost calculation      | Not Started | 2026-01-26 |                                 |
| 1.7 | Create REST endpoints           | Not Started | 2026-01-26 |                                 |
| 1.8 | Create tests                    | Not Started | 2026-01-26 |                                 |

## Progress Log

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
│   ├── dto.SelectCustomerRequest
│   ├── dto.SelectEquipmentRequest
│   └── dto.RentalResponse
├── application
│   ├── usecase.CreateRentalUseCase
│   ├── usecase.UpdateRentalUseCase
│   ├── command.SelectCustomerCommand
│   ├── command.SelectEquipmentCommand
│   └── service.RentalService
├── domain
│   ├── model.Rental
│   ├── model.RentalStatus (enum)
│   ├── model.vo.RentalDuration
│   ├── repository.RentalRepository
│   └── event.RentalCreated
└── infrastructure
    └── persistence
```

**API Endpoints:**

- `POST /api/rentals` - Create new rental (DRAFT status)
- `PATCH /api/rentals/{id}/customer` - Select customer
- `PATCH /api/rentals/{id}/equipment` - Select equipment
- `PATCH /api/rentals/{id}/tariff` - Select tariff
- `GET /api/rentals/{id}` - Get rental details
- `GET /api/rentals/{id}/estimated-cost` - Calculate estimated cost

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

CREATE INDEX idx_rentals_customer ON rentals (customer_id);
CREATE INDEX idx_rentals_equipment ON rentals (equipment_id);
CREATE INDEX idx_rentals_status ON rentals (status);
CREATE INDEX idx_rentals_started ON rentals (started_at);
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

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-RN-001/us-rn-001.md](../../../docs/tasks/us/US-RN-001/us-rn-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependencies: US-CL-001, US-CL-002 (customer), US-EQ-002 (equipment), US-TR-001 (tariff)
- Leads to: US-RN-002, US-RN-003, US-RN-004, US-RN-005 (rental workflow steps)
