# [US-MT-001] - Планирование технического обслуживания (Maintenance Scheduling)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** maintenance  
**Dependencies:** US-EQ-005

## Original Request

**Как** Технический персонал / Администратор  
**Я хочу** автоматически планировать техническое обслуживание  
**Чтобы** поддерживать оборудование в рабочем состоянии

## User Story Details

**Описание:**  
Система должна автоматически планировать техническое обслуживание на основе времени использования.

**Критерии планирования ТО:**

- Каждые X часов работы (настраивается для типа оборудования)
- Каждые Y аренд
- По календарю (раз в месяц/квартал)

**Критерии приемки:**

- Автоматический расчет даты следующего ТО
- Уведомления о приближении срока ТО
- Уведомления о просроченном ТО
- Блокировка оборудования с просроченным критическим ТО

**Связанные требования:** FR-MT-001

## Thought Process

Maintenance scheduling ensures equipment reliability through proactive maintenance. Builds on usage tracking (
US-EQ-005).

**Key Features:**

1. **Multiple Scheduling Criteria**: Hours-based, rental-count-based, calendar-based
2. **Automatic Calculation**: Next maintenance due date computed automatically
3. **Notifications**: Alert technicians before maintenance is due
4. **Equipment Blocking**: Prevent rental of equipment with overdue critical maintenance
5. **Configuration**: Per equipment type settings

**Technical Approach:**

- Store maintenance schedule configuration per equipment type
- Calculate next maintenance date based on current usage
- Event-driven updates when equipment usage changes
- Background job to check for upcoming/overdue maintenance
- Publish MaintenanceDue events for notifications

**Scheduling Logic:**

```
Next Maintenance Date = MAX(
    Last Maintenance + Usage Hours Interval,
    Last Maintenance + Rental Count Interval,
    Last Maintenance + Calendar Interval
)
```

## Implementation Plan

- [ ] Create MaintenanceSchedule domain model
- [ ] Add schedule configuration per equipment type
- [ ] Implement next maintenance calculation
- [ ] Create background job to check schedules
- [ ] Publish MaintenanceDue events
- [ ] Listen to usage updates (US-EQ-005)
- [ ] Implement equipment blocking for overdue
- [ ] Create REST endpoints
- [ ] Add database migration
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                  | Status      | Updated    | Notes |
|-----|------------------------------|-------------|------------|-------|
| 1.1 | Create schedule domain model | Not Started | 2026-01-26 |       |
| 1.2 | Implement calculation logic  | Not Started | 2026-01-26 |       |
| 1.3 | Create background scheduler  | Not Started | 2026-01-26 |       |
| 1.4 | Add equipment blocking       | Not Started | 2026-01-26 |       |
| 1.5 | Publish events               | Not Started | 2026-01-26 |       |
| 1.6 | Create tests                 | Not Started | 2026-01-26 |       |

## Technical Details

**Domain Model:**

```java
public class MaintenanceSchedule {
    private UUID id;
    private UUID equipmentTypeId;
    private int usageHoursInterval;      // e.g., every 100 hours
    private int rentalCountInterval;     // e.g., every 50 rentals
    private Period calendarInterval;     // e.g., P1M (1 month)
    private boolean isCritical;          // If true, block equipment when overdue
}

public class Equipment {
    // ...existing fields...
    private LocalDateTime lastMaintenanceAt;
    private LocalDateTime nextMaintenanceAt;

    public void calculateNextMaintenance(MaintenanceSchedule schedule) {
        LocalDateTime byUsage = lastMaintenanceAt.plusHours(
                schedule.getUsageHoursInterval()
        );

        LocalDateTime byRentals = lastMaintenanceAt.plusDays(
                estimateDaysForRentals(schedule.getRentalCountInterval())
        );

        LocalDateTime byCalendar = lastMaintenanceAt.plus(
                schedule.getCalendarInterval()
        );

        this.nextMaintenanceAt = Stream.of(byUsage, byRentals, byCalendar)
                .max(LocalDateTime::compareTo)
                .orElse(byCalendar);
    }

    public boolean isMaintenanceOverdue() {
        return nextMaintenanceAt != null &&
                LocalDateTime.now().isAfter(nextMaintenanceAt);
    }
}
```

**Background Scheduler:**

```java

@Component
public class MaintenanceScheduleChecker {

    @Scheduled(cron = "0 0 8 * * ?") // Daily at 8 AM
    public void checkMaintenanceSchedules() {
        List<Equipment> equipment = equipmentRepository.findAll();

        for (Equipment eq : equipment) {
            if (eq.isMaintenanceOverdue()) {
                eventPublisher.publish(new MaintenanceOverdue(
                        eq.getId(), eq.getNextMaintenanceAt()
                ));

                // Block critical equipment
                if (eq.getSchedule().isCritical()) {
                    eq.setStatus(EquipmentStatus.MAINTENANCE);
                }
            } else if (eq.isMaintenanceSoon(7)) { // 7 days warning
                eventPublisher.publish(new MaintenanceSoon(
                        eq.getId(), eq.getNextMaintenanceAt()
                ));
            }
        }
    }
}
```

**Event Handler:**

```java

@EventListener
public void onRentalCompleted(RentalCompleted event) {
    Equipment equipment = equipmentRepository.findById(event.equipmentId())
            .orElseThrow();

    // Recalculate next maintenance based on updated usage
    MaintenanceSchedule schedule = getScheduleForEquipmentType(
            equipment.getEquipmentTypeId()
    );

    equipment.calculateNextMaintenance(schedule);
    equipmentRepository.save(equipment);
}
```

**API Endpoints:**

- `GET /api/maintenance/schedules` - Get all maintenance schedules
- `POST /api/maintenance/schedules` - Create schedule for equipment type
- `GET /api/maintenance/due` - Get equipment due for maintenance
- `GET /api/maintenance/overdue` - Get overdue equipment

**Database Schema:**

```sql
CREATE TABLE maintenance_schedules
(
    id                    UUID PRIMARY KEY,
    equipment_type_id     UUID      NOT NULL REFERENCES equipment_types (id),
    usage_hours_interval  INT,
    rental_count_interval INT,
    calendar_interval     VARCHAR(20), -- ISO-8601 period format
    is_critical           BOOLEAN   NOT NULL DEFAULT false,
    created_at            TIMESTAMP NOT NULL
);

ALTER TABLE equipment
    ADD COLUMN last_maintenance_at TIMESTAMP;
ALTER TABLE equipment
    ADD COLUMN next_maintenance_at TIMESTAMP;
```

## References

- User Story File: [docs/tasks/us/US-MT-001/us-mt-001.md](../../../docs/tasks/us/US-MT-001/us-mt-001.md)
- Dependencies: US-EQ-005 (usage tracking)
- Used by: US-MT-004 (maintenance notifications)
