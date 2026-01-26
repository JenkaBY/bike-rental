# [US-MT-003] - Вывод оборудования из эксплуатации (Equipment Decommissioning)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** maintenance  
**Dependencies:** US-EQ-004

## Original Request

**Как** Администратор  
**Я хочу** выводить оборудование из эксплуатации  
**Чтобы** исключить неисправное или устаревшее оборудование из парка

## User Story Details

**Описание:**  
Система должна позволять выводить оборудование из эксплуатации (списание).

**Причины списания:**

- Износ
- Поломка без возможности ремонта
- Моральное устаревание
- Утеря/кража

**Критерии приемки:**

- Изменение статуса на "Списано"
- Указание причины и даты списания
- Исключение из доступного оборудования
- Сохранение истории использования
- Учет в отчетности

**Связанные требования:** FR-MT-003

## Thought Process

Equipment decommissioning is the terminal state in equipment lifecycle. This user story manages the process of
permanently retiring equipment. Considerations:

1. **Terminal State**: DECOMMISSIONED is a terminal status (no transitions out)
2. **Validation**: Cannot decommission equipment currently in rental
3. **History Preservation**: All rental and maintenance history must remain
4. **Reporting**: Decommissioned equipment still appears in historical reports
5. **Audit Requirements**: Document reason, date, and who performed decommissioning

**Business Rules:**

- Cannot decommission equipment with status RENTED (must complete/cancel rental first)
- Once decommissioned, equipment cannot be reactivated (business decision)
- All historical data (rentals, maintenance, usage) must be preserved
- Decommissioned equipment excluded from availability counts and searches

**Technical Approach:**

- Use equipment status management from US-EQ-004
- Add decommission metadata (reason, date, performed by)
- Validate that equipment is not in active rental
- Publish EquipmentDecommissioned domain event
- Update queries to exclude DECOMMISSIONED equipment by default

**Architecture Decisions:**

- Decommissioning is a special status change with additional metadata
- Store decommission details in separate table or as JSON in equipment
- Use soft delete pattern (status change, not physical deletion)
- Consider retention policy for decommissioned equipment data

## Implementation Plan

- [ ] Create DecommissionEquipmentUseCase
- [ ] Add decommission metadata to Equipment model
- [ ] Implement validation (check for active rentals)
- [ ] Create decommission endpoint
- [ ] Add decommission reason enum
- [ ] Update equipment queries to exclude decommissioned
- [ ] Publish EquipmentDecommissioned event
- [ ] Add database migration for decommission metadata
- [ ] Create component tests for decommissioning
- [ ] Write unit tests for validation
- [ ] Write WebMvc tests for endpoint
- [ ] Update reporting to handle decommissioned equipment

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                  | Status      | Updated    | Notes |
|-----|------------------------------|-------------|------------|-------|
| 3.1 | Create decommission use case | Not Started | 2026-01-26 |       |
| 3.2 | Add decommission metadata    | Not Started | 2026-01-26 |       |
| 3.3 | Implement validation         | Not Started | 2026-01-26 |       |
| 3.4 | Create endpoint              | Not Started | 2026-01-26 |       |
| 3.5 | Update equipment queries     | Not Started | 2026-01-26 |       |
| 3.6 | Publish domain events        | Not Started | 2026-01-26 |       |
| 3.7 | Create tests                 | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-EQ-004 completion
- Part of Phase 2: Basic Module Functions
- Priority: Low (can be deferred to later phase if needed)

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.maintenance
├── web.command
│   ├── DecommissionController
│   └── dto.DecommissionEquipmentRequest
├── application
│   ├── usecase.DecommissionEquipmentUseCase
│   └── service.DecommissionService
├── domain
│   ├── model.DecommissionReason (enum)
│   ├── validation.DecommissionValidator
│   └── event.EquipmentDecommissioned
└── infrastructure
    └── persistence
```

**API Endpoint:**

- `POST /api/maintenance/equipment/{id}/decommission` - Decommission equipment (ADMIN only)
- Request body:

```json
{
  "reason": "BEYOND_REPAIR",
  "notes": "Frame damaged beyond repair after accident",
  "decommissionDate": "2026-01-26"
}
```

- Response: `200 OK` with updated equipment showing DECOMMISSIONED status

**Database Schema Changes:**

```sql
-- Add columns to equipment table
ALTER TABLE equipment
    ADD COLUMN decommission_reason VARCHAR(50);
ALTER TABLE equipment
    ADD COLUMN decommission_notes TEXT;
ALTER TABLE equipment
    ADD COLUMN decommissioned_at TIMESTAMP;
ALTER TABLE equipment
    ADD COLUMN decommissioned_by UUID REFERENCES app_users (id);

CREATE INDEX idx_equipment_decommissioned ON equipment (status, decommissioned_at);
```

**Decommission Reason Enum:**

```java
public enum DecommissionReason {
    WEAR_AND_TEAR,        // Износ
    BEYOND_REPAIR,        // Поломка без возможности ремонта
    OBSOLETE,             // Моральное устаревание
    LOST,                 // Утеря
    STOLEN,               // Кража
    SAFETY_CONCERNS,      // Проблемы безопасности
    OTHER
}
```

**Domain Model Extension:**

```java
public class Equipment {
    // ...existing fields...

    private DecommissionReason decommissionReason;
    private String decommissionNotes;
    private LocalDateTime decommissionedAt;
    private UUID decommissionedBy;

    public void decommission(DecommissionReason reason, String notes, UUID decommissionedBy) {
        if (this.status == EquipmentStatus.RENTED) {
            throw new IllegalStateException("Cannot decommission equipment that is currently rented");
        }

        this.status = EquipmentStatus.DECOMMISSIONED;
        this.decommissionReason = reason;
        this.decommissionNotes = notes;
        this.decommissionedAt = LocalDateTime.now();
        this.decommissionedBy = decommissionedBy;

        // Publish event
        registerEvent(new EquipmentDecommissioned(
                this.id, reason, decommissionedAt
        ));
    }

    public boolean isDecommissioned() {
        return status == EquipmentStatus.DECOMMISSIONED;
    }
}
```

**Validation:**

```java

@Service
public class DecommissionValidator {

    private final RentalRepository rentalRepository;

    public void validateDecommission(UUID equipmentId) {
        // Check for active rentals
        boolean hasActiveRentals = rentalRepository.existsByEquipmentIdAndStatusIn(
                equipmentId,
                List.of(RentalStatus.DRAFT, RentalStatus.ACTIVE)
        );

        if (hasActiveRentals) {
            throw new EquipmentInUseException(
                    "Cannot decommission equipment with active rentals. " +
                            "Complete or cancel rentals first."
            );
        }
    }
}
```

**Domain Event:**

```java
record EquipmentDecommissioned(
        UUID equipmentId,
        DecommissionReason reason,
        LocalDateTime decommissionedAt
) {
}
```

**Query Updates:**
By default, exclude decommissioned equipment from searches:

```java
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    // Default query excludes decommissioned
    @Query("SELECT e FROM Equipment e WHERE e.status != 'DECOMMISSIONED'")
    List<Equipment> findAllActive();

    // Explicit query for all including decommissioned
    @Query("SELECT e FROM Equipment e")
    List<Equipment> findAllIncludingDecommissioned();

    // Search by serial, exclude decommissioned
    @Query("SELECT e FROM Equipment e WHERE e.serialNumber = :serial AND e.status != 'DECOMMISSIONED'")
    Optional<Equipment> findBySerialNumber(@Param("serial") String serialNumber);
}
```

**Reporting Considerations:**

- Historical reports (usage, rentals) include decommissioned equipment
- Current inventory reports exclude decommissioned equipment
- Decommission report shows all decommissioned equipment with reasons and dates

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-MT-003/us-mt-003.md](../../../docs/tasks/us/US-MT-003/us-mt-003.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-EQ-004 (Equipment status management - must be complete)
- Related: US-EQ-001 (Equipment catalog)
- Related: US-MT-002 (Maintenance records preserved for decommissioned equipment)
