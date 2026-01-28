# [US-EQ-004] - Управление статусами оборудования (Equipment Status Management)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** equipment  
**Dependencies:** US-EQ-001

## Original Request

**Как** Система / Оператор проката / Администратор  
**Я хочу** управлять статусами оборудования  
**Чтобы** отслеживать состояние каждого оборудования и его доступность

## User Story Details

**Описание:**  
Система должна управлять статусами оборудования и автоматически менять их.

**Статусы:**

- **Доступно** — готово к аренде
- **В аренде** — находится у клиента
- **На обслуживании** — на ремонте/ТО
- **Списано** — выведено из эксплуатации

**Переходы статусов:**

- Доступно → В аренде (при оформлении аренды)
- В аренде → Доступно (при возврате)
- Доступно → На обслуживании (ручное переключение)
- На обслуживании → Доступно (после завершения ТО)
- Любой статус → Списано (при списании)

**Критерии приемки:**

- Автоматическое изменение статуса при оформлении/возврате аренды
- Возможность ручного изменения статуса администратором
- Валидация переходов статусов

**Связанные требования:** FR-EQ-004

## Thought Process

Equipment status is central to the rental workflow and must be managed carefully. This involves both automatic (
system-driven) and manual (user-driven) status transitions.

**Key Design Decisions:**

1. **Status as Domain Concept**: EquipmentStatus should be a first-class domain concept with transition rules
2. **Event-Driven Updates**: Status changes should publish domain events
3. **Validation**: Not all status transitions are valid (state machine pattern)
4. **Automatic vs Manual**: System changes status during rental lifecycle, admin can manually change
5. **Audit Trail**: Status changes should be logged for tracking

**State Machine Design:**

```
AVAILABLE → RENTED (automatic, on rental start)
RENTED → AVAILABLE (automatic, on rental return)
AVAILABLE ↔ MAINTENANCE (manual, by admin/technician)
MAINTENANCE → AVAILABLE (manual, after service complete)
* → DECOMMISSIONED (manual, by admin only)
```

**Architecture Considerations:**

- Use domain events: `EquipmentStatusChanged`
- Rental module listens to rental events and updates equipment status
- Admin endpoints allow manual status changes with validation
- Consider using state machine pattern in domain model

## Implementation Plan

- [ ] Enhance Equipment domain model with status transition logic
- [ ] Implement ChangeEquipmentStatusUseCase
- [ ] Create status validation rules (state machine)
- [ ] Implement event handlers for automatic status changes
- [ ] Create manual status change endpoint (admin)
- [ ] Publish EquipmentStatusChanged event
- [ ] Add component tests for status transitions
- [ ] Write unit tests for validation logic
- [ ] Write WebMvc tests for manual status changes
- [ ] Add audit logging for status changes
- [ ] Document state machine diagram

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                          | Status      | Updated    | Notes                 |
|-----|--------------------------------------|-------------|------------|-----------------------|
| 4.1 | Implement status transition logic    | Not Started | 2026-01-26 | State machine pattern |
| 4.2 | Create manual status change endpoint | Not Started | 2026-01-26 |                       |
| 4.3 | Implement automatic status updates   | Not Started | 2026-01-26 | Event-driven          |
| 4.4 | Add validation rules                 | Not Started | 2026-01-26 |                       |
| 4.5 | Publish domain events                | Not Started | 2026-01-26 |                       |
| 4.6 | Create tests                         | Not Started | 2026-01-26 |                       |
| 4.7 | Add audit logging                    | Not Started | 2026-01-26 |                       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-EQ-001 completion
- Part of Phase 2: Basic Module Functions

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.equipment
├── web.command
│   ├── EquipmentCommandController
│   └── dto.ChangeStatusRequest
├── application
│   ├── usecase.ChangeEquipmentStatusUseCase
│   ├── service.EquipmentStatusService
│   └── eventhandler.RentalEventHandler
├── domain
│   ├── model.Equipment (add status transition methods)
│   ├── model.EquipmentStatus (enum with transition rules)
│   ├── exception.InvalidStatusTransitionException
│   └── event.EquipmentStatusChanged
└── infrastructure
    └── eventlistener (for automatic status changes)
```

**API Endpoints:**

- `PATCH /api/equipment/{id}/status` - Manual status change (admin only)
- Request body: `{ "status": "MAINTENANCE", "reason": "Brake repair needed" }`
- Response: `200 OK` with updated equipment
- Error: `400 Bad Request` if invalid transition

**Domain Events:**

```java
record EquipmentStatusChanged(
        UUID equipmentId,
        EquipmentStatus oldStatus,
        EquipmentStatus newStatus,
        String reason,
        LocalDateTime changedAt,
        UUID changedBy  // null for automatic changes
) {
}
```

**Status Transition Matrix:**

```java
public enum EquipmentStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE,
    DECOMMISSIONED;

    public boolean canTransitionTo(EquipmentStatus newStatus) {
        return switch (this) {
            case AVAILABLE -> newStatus == RENTED || newStatus == MAINTENANCE || newStatus == DECOMMISSIONED;
            case RENTED -> newStatus == AVAILABLE || newStatus == DECOMMISSIONED;
            case MAINTENANCE -> newStatus == AVAILABLE || newStatus == DECOMMISSIONED;
            case DECOMMISSIONED -> false; // Terminal state
        };
    }
}
```

**Event Handlers:**

```java

@EventListener
public void onRentalStarted(RentalStarted event) {
    changeEquipmentStatus(event.equipmentId(), RENTED, "Rental started");
}

@EventListener
public void onRentalCompleted(RentalCompleted event) {
    changeEquipmentStatus(event.equipmentId(), AVAILABLE, "Rental completed");
}
```

**Audit Log:**

- Log all status changes with timestamp, old status, new status, reason, changed by
- Useful for troubleshooting and business intelligence

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-EQ-004/us-eq-004.md](../../../docs/tasks/us/US-EQ-004/us-eq-004.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-EQ-001 (Equipment catalog)
- Critical for: US-RN-005 (Rental start), US-RN-006 (Equipment return)
- Related: US-MT-003 (Decommissioning)
