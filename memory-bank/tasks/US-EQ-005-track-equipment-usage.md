# [US-EQ-005] - Учет износа и пробега (Track Equipment Usage and Wear)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** equipment  
**Dependencies:** US-RN-006

## Original Request

**Как** Технический персонал  
**Я хочу** видеть информацию об износе и пробеге оборудования  
**Чтобы** планировать техническое обслуживание

## User Story Details

**Описание:**  
Система должна автоматически учитывать время использования и пробег оборудования.

**Показатели:**

- Общее время в аренде (часы)
- Количество аренд
- Дата последнего ТО
- Следующее плановое ТО (по времени использования)

**Критерии приемки:**

- Автоматический подсчет времени использования
- Отображение в карточке оборудования
- Уведомления о необходимости ТО

**Связанные требования:** FR-EQ-005

## Thought Process

Usage tracking enables predictive maintenance scheduling. Accumulate usage hours from completed rentals.

**Metrics to Track:**

- Total usage hours
- Total rental count
- Last maintenance date
- Next maintenance due (based on usage hours)

**Update Trigger:**

- Listen to RentalCompleted event
- Increment totalUsageHours
- Increment rentalCount
- Check if maintenance is due

## Implementation Plan

- [ ] Add usage fields to Equipment model
- [ ] Create usage update event handler
- [ ] Implement maintenance due calculation
- [ ] Create usage statistics query
- [ ] Add usage display to equipment details
- [ ] Create maintenance alerts
- [ ] Add database migration
- [ ] Create tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                  | Status      | Updated    | Notes |
|-----|------------------------------|-------------|------------|-------|
| 5.1 | Add usage tracking fields    | Not Started | 2026-01-26 |       |
| 5.2 | Create event handler         | Not Started | 2026-01-26 |       |
| 5.3 | Implement maintenance alerts | Not Started | 2026-01-26 |       |
| 5.4 | Create tests                 | Not Started | 2026-01-26 |       |

## Technical Details

**Equipment Model Extension:**

```java
public class Equipment {
    // ...existing fields...
    private int totalUsageMinutes;
    private int totalRentalCount;
    private LocalDateTime lastMaintenanceAt;

    public int getTotalUsageHours() {
        return totalUsageMinutes / 60;
    }

    public boolean isMaintenanceDue(int maintenanceIntervalHours) {
        int hoursSinceLastMaintenance = calculateHoursSince(lastMaintenanceAt);
        return hoursSinceLastMaintenance >= maintenanceIntervalHours;
    }
}
```

**Event Handler:**

```java

@EventListener
public void onRentalCompleted(RentalCompleted event) {
    Equipment equipment = equipmentRepository.findById(event.equipmentId())
            .orElseThrow();

    int rentalDuration = event.actualDurationMinutes();
    equipment.addUsage(rentalDuration);
    equipmentRepository.save(equipment);

    // Check if maintenance due
    if (equipment.isMaintenanceDue(config.getMaintenanceIntervalHours())) {
        eventPublisher.publish(new MaintenanceDue(equipment.getId()));
    }
}
```

## References

- User Story File: [docs/tasks/us/US-EQ-005/us-eq-005.md](../../../docs/tasks/us/US-EQ-005/us-eq-005.md)
- Depends on: US-RN-006
