# [US-MT-002] - Учет ремонтов и обслуживания (Maintenance Records)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** maintenance  
**Dependencies:** US-EQ-001

## Original Request

**Как** Технический персонал  
**Я хочу** фиксировать все виды работ по обслуживанию оборудования  
**Чтобы** вести историю обслуживания и планировать расходы

## User Story Details

**Описание:**  
Система должна фиксировать все виды работ по обслуживанию оборудования.

**Информация о работах:**

- Дата начала/завершения
- Тип работ (плановое ТО, ремонт, диагностика)
- Описание выполненных работ
- Замененные детали/компоненты
- Стоимость работ и запчастей
- Исполнитель

**Критерии приемки:**

- Создание записи о ТО
- История всех ТО по оборудованию
- Статистика затрат на обслуживание
- Интеграция со справочником запчастей (опционально)

**Связанные требования:** FR-MT-002

## Thought Process

Maintenance tracking is essential for equipment lifecycle management and cost analysis. This is a foundational feature
for the maintenance module. Considerations:

1. **Maintenance Types**: Different types of work (scheduled maintenance, repair, diagnostics)
2. **Cost Tracking**: Separate costs for labor and parts
3. **Equipment Downtime**: Maintenance records help understand availability patterns
4. **Historical Analysis**: Track maintenance costs and frequency over time
5. **Integration**: May integrate with inventory system for parts (future)

**Technical Approach:**

- MaintenanceRecord as aggregate root
- Link to Equipment via equipmentId
- Store detailed work descriptions and costs
- Track start/completion times for downtime analysis
- Optional parts list (can be simple text initially, structured later)

**Architecture Decisions:**

- MaintenanceRecord is an aggregate in maintenance module
- Published events when maintenance starts/completes
- Equipment status changes to MAINTENANCE and back to AVAILABLE
- History queryable by equipment, by date range, by cost

## Implementation Plan

- [ ] Create MaintenanceRecord domain model
- [ ] Create MaintenanceType enum
- [ ] Implement maintenance repository
- [ ] Create CRUD use cases for maintenance records
- [ ] Implement query for equipment maintenance history
- [ ] Implement cost statistics calculation
- [ ] Create REST endpoints
- [ ] Add database migration for maintenance_records table
- [ ] Publish MaintenanceStarted/MaintenanceCompleted events
- [ ] Create component tests for maintenance operations
- [ ] Write unit tests for cost calculations
- [ ] Write WebMvc tests for endpoints

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description               | Status      | Updated    | Notes |
|-----|---------------------------|-------------|------------|-------|
| 2.1 | Create domain model       | Not Started | 2026-01-26 |       |
| 2.2 | Implement CRUD use cases  | Not Started | 2026-01-26 |       |
| 2.3 | Create REST endpoints     | Not Started | 2026-01-26 |       |
| 2.4 | Add history queries       | Not Started | 2026-01-26 |       |
| 2.5 | Implement cost statistics | Not Started | 2026-01-26 |       |
| 2.6 | Publish domain events     | Not Started | 2026-01-26 |       |
| 2.7 | Create tests              | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-EQ-001 completion
- Part of Phase 2: Basic Module Functions
- Priority: Low (can be deferred to later phase if needed)

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.maintenance
├── web.command
│   ├── MaintenanceCommandController
│   ├── dto.CreateMaintenanceRecordRequest
│   └── dto.CompleteMaintenanceRequest
├── web.query
│   ├── MaintenanceQueryController
│   ├── dto.MaintenanceRecordResponse
│   └── dto.MaintenanceCostSummary
├── application
│   ├── usecase.CreateMaintenanceRecordUseCase
│   ├── usecase.CompleteMaintenanceRecordUseCase
│   └── service.MaintenanceService
├── domain
│   ├── model.MaintenanceRecord
│   ├── model.MaintenanceType (enum)
│   ├── repository.MaintenanceRepository
│   └── event.MaintenanceStarted, MaintenanceCompleted
└── infrastructure
    └── persistence
```

**API Endpoints:**

- `POST /api/maintenance` - Create maintenance record
- `PATCH /api/maintenance/{id}/complete` - Complete maintenance
- `GET /api/maintenance/{id}` - Get record details
- `GET /api/maintenance/equipment/{equipmentId}` - Get equipment history
- `GET /api/maintenance/equipment/{equipmentId}/costs` - Get cost summary

**Database Schema:**

```sql
CREATE TABLE maintenance_records
(
    id                UUID PRIMARY KEY,
    equipment_id      UUID        NOT NULL REFERENCES equipment (id),
    maintenance_type  VARCHAR(50) NOT NULL,
    description       TEXT        NOT NULL,
    parts_description TEXT,
    labor_cost        DECIMAL(10, 2),
    parts_cost        DECIMAL(10, 2),
    total_cost        DECIMAL(10, 2) GENERATED ALWAYS AS (labor_cost + parts_cost) STORED,
    started_at        TIMESTAMP   NOT NULL,
    completed_at      TIMESTAMP,
    performed_by      UUID REFERENCES app_users (id),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP
);

CREATE INDEX idx_maintenance_equipment ON maintenance_records (equipment_id, started_at DESC);
CREATE INDEX idx_maintenance_type ON maintenance_records (maintenance_type);
CREATE INDEX idx_maintenance_dates ON maintenance_records (started_at, completed_at);
```

**Domain Model:**

```java
public class MaintenanceRecord {
    private UUID id;
    private UUID equipmentId;
    private MaintenanceType maintenanceType;
    private String description;
    private String partsDescription;
    private Money laborCost;
    private Money partsCost;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private UUID performedBy;

    public Money getTotalCost() {
        return laborCost.add(partsCost);
    }

    public boolean isComplete() {
        return completedAt != null;
    }

    public Duration getDuration() {
        if (!isComplete()) return null;
        return Duration.between(startedAt, completedAt);
    }
}
```

**Enums:**

```java
public enum MaintenanceType {
    SCHEDULED_MAINTENANCE,  // Плановое ТО
    REPAIR,                 // Ремонт
    DIAGNOSTICS,           // Диагностика
    PARTS_REPLACEMENT,     // Замена деталей
    CLEANING,              // Чистка
    OTHER
}
```

**Domain Events:**

```java
record MaintenanceStarted(
        UUID maintenanceId,
        UUID equipmentId,
        MaintenanceType type,
        LocalDateTime startedAt
) {
}

record MaintenanceCompleted(
        UUID maintenanceId,
        UUID equipmentId,
        BigDecimal totalCost,
        LocalDateTime completedAt
) {
}
```

**Cost Summary Query:**

```java
public class MaintenanceCostSummary {
    private UUID equipmentId;
    private int totalMaintenanceCount;
    private Money totalCost;
    private Money averageCostPerMaintenance;
    private Map<MaintenanceType, Money> costsByType;
    private Money lastMaintenanceCost;
    private LocalDateTime lastMaintenanceDate;
}
```

**Integration with Equipment Status:**

```java

@EventListener
public void onMaintenanceStarted(MaintenanceStarted event) {
    // Change equipment status to MAINTENANCE
    equipmentStatusService.changeStatus(
            event.equipmentId(),
            EquipmentStatus.MAINTENANCE,
            "Maintenance started: " + event.type()
    );
}

@EventListener
public void onMaintenanceCompleted(MaintenanceCompleted event) {
    // Change equipment status back to AVAILABLE
    equipmentStatusService.changeStatus(
            event.equipmentId(),
            EquipmentStatus.AVAILABLE,
            "Maintenance completed"
    );
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-MT-002/us-mt-002.md](../../../docs/tasks/us/US-MT-002/us-mt-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-EQ-001 (Equipment catalog must exist)
- Related: US-EQ-004 (Equipment status management)
- Future: US-MT-001 (Scheduled maintenance planning)
