# [US-EQ-004] - Управление статусами оборудования (Equipment Status Management)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-06  
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

**Key Design Decisions (Updated 2026-02-06):**

1. **DDD-Compliant Architecture**: Domain entities use ports (interfaces) for validation, not direct infrastructure
   dependencies
2. **StatusTransitionPolicy Port**: Domain layer defines interface (`StatusTransitionPolicy`), application layer
   provides implementation (`EquipmentStatusTransitionPolicy`)
3. **EquipmentStatus as Separate Aggregate**: `EquipmentStatus` is a Reference Data Aggregate, not part of `Equipment`
   aggregate
4. **Status Reference by Slug**: `Equipment` stores `statusSlug` (String) as Value Object, not full `EquipmentStatus`
   entity reference
5. **Embedded Transition Rules**: `EquipmentStatus` entity contains `allowedTransitions` (Set<String>) for transition
   management
6. **Status changes handled via equipment update**: Status changes are applied through the existing equipment update
   operation (PUT /api/equipment/{id})
   rather than a separate status-change endpoint or separate service class. This simplifies the API surface and keeps
   equipment updates atomic.
7. **Event-Driven Cross-Module Communication**: Rental module publishes events, Equipment module reacts via listeners
8. **Performance Optimization**: Direct mapping of `statusSlug` eliminates N+1 queries and unnecessary entity loading

**State Machine Design:**

```
AVAILABLE → RESERVED | RENTED | MAINTENANCE | DECOMMISSIONED
RESERVED → RENTED | AVAILABLE | DECOMMISSIONED
RENTED → AVAILABLE | DECOMMISSIONED
MAINTENANCE → AVAILABLE | DECOMMISSIONED
DECOMMISSIONED → (terminal, no transitions)
```

**Architecture Pattern (DDD-Compliant):**

```
Equipment Aggregate (Root)
├── statusSlug: String (Value Object)
└── changeStatusTo(slug, StatusTransitionPolicy)

EquipmentStatus Aggregate (Reference Data)
├── id, slug, name, description
├── allowedTransitions: Set<String>
└── canTransitionTo(slug): boolean

StatusTransitionPolicy (Domain Port)
└── validateTransition(fromSlug, toSlug)

EquipmentStatusTransitionPolicy (Application Service)
└── implements StatusTransitionPolicy using EquipmentStatusRepository

Cross-Module Communication
├── Rental Module publishes: RentalReserved, RentalStarted, RentalCompleted
└── Equipment Module reacts: RentalEventListener → UpdateEquipmentUseCase
```

**Architecture Considerations:**

- Domain entities never depend directly on repositories or infrastructure
- Status transition validation through one to many relationship
- Rental and Equipment modules communicate via domain events (loose coupling)
- Equipment status changes always enforce transition rules

## Implementation Plan

- [x] Enhance Equipment domain model with status transition logic
- [x] Reuse `UpdateEquipmentUseCase` / existing equipment update flow to change status (PUT /api/equipment/{id})
- [x] Create status validation rules (state machine)
- [x] Implement event handlers for automatic status changes (should use equipment update flow internally)
- [x] Support manual status change via equipment update (admin)
- [x] Publish EquipmentStatusChanged event when status changes as part of update
- [x] Add component tests for status transitions
- [x] Write unit tests for validation logic
- [x] Write WebMvc tests for equipment update including status change scenarios
- [x] Add audit logging for status changes
- [x] Document state machine diagram

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                                      | Status   | Updated    | Notes                                        |
|-----|--------------------------------------------------|----------|------------|----------------------------------------------|
| 4.1 | Implement status transition logic                | Complete | 2026-02-05 | State machine pattern implemented with tests |
| 4.2 | Support status change via equipment update (PUT) | Complete | 2026-02-05 | UpdateEquipmentUseCase implemented and wired |
| 4.3 | Implement automatic status updates               | Complete | 2026-02-05 | Event-driven handlers call update flow       |
| 4.4 | Add validation rules                             | Complete | 2026-02-05 | Validation in domain model                   |
| 4.5 | Publish domain events                            | Complete | 2026-02-05 | EquipmentStatusChanged published on update   |
| 4.6 | Create tests                                     | Complete | 2026-02-05 | Unit, WebMvc and component tests added       |
| 4.7 | Add audit logging                                | Complete | 2026-02-05 | Audit entries logged for status changes      |

## Progress Log

### 2026-02-06 (DDD Refactoring Completed)

**Major Architecture Improvement: EquipmentStatus as Separate Aggregate**

- **Refactored Equipment domain model**: Changed from storing `EquipmentStatus` Entity to `statusSlug: String` (Value
  Object)
- **Created StatusTransitionPolicy port**: Domain layer interface (`domain/service/StatusTransitionPolicy`) for
  validation
- **Implemented EquipmentStatusTransitionPolicy**: Application service that implements the port using
  `EquipmentStatusRepository`
- **Updated Equipment.changeStatusTo()**: Now accepts `StatusTransitionPolicy` as parameter (dependency injection
  pattern)
- **Removed EquipmentJpaMapperDecorator**: Direct mapping of `statusSlug` eliminates N+1 queries and unnecessary entity
  loading
- **Updated all mappers**: `EquipmentJpaMapper`, `EquipmentCommandToDomainMapper`, `EquipmentQueryMapper` now work with
  `statusSlug`
- **Updated services**: `CreateEquipmentService` and `UpdateEquipmentService` use policy for validation
- **Performance improvement**: No more additional database queries when loading Equipment entities
- **DDD compliance**: EquipmentStatus is now correctly modeled as separate Reference Data Aggregate

**Key Changes:**

- Equipment stores `statusSlug: String` instead of `EquipmentStatus` Entity
- StatusTransitionPolicy port in domain layer, implementation in application layer
- Direct statusSlug mapping in persistence layer (no decorator needed)
- All validation through policy, maintaining domain purity

### 2026-02-05 (Task Completed)

- All subtasks implemented and verified. The status-change functionality is now part of the existing equipment update
  flow (PUT /api/equipment/{id}).
- Removed the separate PATCH endpoint and dedicated ChangeEquipmentStatus* classes from the implementation; rental event
  listeners and admin UI now use the UpdateEquipmentUseCase to change `statusSlug`.
- Added/updated unit tests, WebMvc tests and component tests to cover manual and automatic status changes; all tests
  related to status management are passing in local test runs.
- Audit logging implemented for status changes and EquipmentStatusChanged domain event is published when equipment
  status is updated.
- Updated documentation and memory bank entries to reflect the consolidated update flow.

### 2026-02-05 (Architecture Update)

**DDD-Compliant Solution Implemented ✅**

**Architecture Changes:**

- Introduced `StatusTransitionPolicy` port interface in domain layer
- `Equipment.changeStatus()`

**EquipmentStatus Model Enhanced:**

- Added `allowedTransitionSlugs: Set<String>` field
- Database schema: `equipment_status_transitions` table with `@ElementCollection`
- Status CRUD operations now manage transition rules in same request
- Foreign key constraint ensures referenced statuses exist

**Cross-Module Communication Pattern:**

- Rental module publishes domain events (RentalReserved, RentalStarted, RentalCompleted)
- Equipment module has `RentalEventListener` that reacts to events
- Listener invokes `ChangeEquipmentStatusUseCase` to update status
- No direct repository calls between modules (bounded context isolation)

**Implementation Status:**

- ✅ StatusTransitionPolicy port interface
- ✅ InMemoryStatusTransitionPolicy implementation
- ✅ DatabaseStatusTransitionPolicy implementation
- ✅ EquipmentStatus with embedded transitions
- ✅ CreateEquipmentStatusService with transition validation
- ✅ UpdateEquipmentStatusService with transition validation (status changes integrated into equipment update flow)
- ✅ Liquibase changelog for transition rules table
- ⏳ RentalEventListener (pending wiring to use equipment update flow)
- ⏳ Integration: ensure equipment update endpoint accepts and validates `statusSlug` and publishes
  EquipmentStatusChanged

**Benefits:**

- Domain purity: no infrastructure dependencies in entities
- Testability: mock StatusTransitionPolicy in unit tests
- Flexibility: swap policy implementations via dependency injection
- Module isolation: Rental and Equipment remain decoupled
- Invariant protection: status changes always validated

### 2026-02-04 (Update)

**Subtask 4.1 COMPLETED ✅** - Domain state machine and validation logic implemented

**Implemented:**

- Created `StatusSlug` enum with `canTransitionTo()` state machine logic
  - AVAILABLE → RENTED, MAINTENANCE, DECOMMISSIONED
  - RENTED → AVAILABLE, DECOMMISSIONED
  - MAINTENANCE → AVAILABLE, DECOMMISSIONED
  - DECOMMISSIONED → (terminal, no transitions)
- Created `InvalidStatusTransitionException` for domain validation
- Enhanced `Equipment` domain model with:
  - `changeStatus(StatusSlug, String)` method with validation
  - `getCurrentStatus()` helper
  - `isAvailable()` and `isRented()` convenience methods
- Created `EquipmentStatusChanged` domain event (implements BikeRentalEvent)
- Comprehensive unit tests (20 tests total, all passing):
  - `StatusSlugTest`: 20 tests covering transition matrix and slug conversion
  - `EquipmentTest.StatusChangeTests`: 10 tests for Equipment status change behavior

**Test Results:**

- All 30 tests passing (20 new + 10 existing)
- Coverage: transition matrix fully tested, invalid transitions verified, terminal state validated

**Next Steps:**

1. Integrate status change into the existing equipment update use case (PUT /api/equipment/{id}) and ensure validation
   and audit logging
2. Update WebMvc tests to cover manual status changes via equipment update
3. Wire Rental event listeners to call the equipment update use case (not a separate ChangeEquipmentStatus service)
4. Update documentation and memory bank to remove the separate PATCH endpoint and ChangeEquipmentStatus* classes

### 2026-02-04

- Taking ownership of **US-EQ-004: Equipment Status Management** and starting implementation work.
- Initial focus: implement domain state machine and status transition logic (subtask 4.1) so the rest of the flow (
  automatic updates and manual endpoint) can be validated against domain rules.
- Short-term plan (next steps):
  1. Create `EquipmentStatus` enum with `canTransitionTo(...)` method and unit tests for transition matrix.
  2. Add `changeStatus` behavior to `Equipment` domain model and a `ChangeEquipmentStatusUseCase` interface.
  3. Implement domain validation (`InvalidStatusTransitionException`) and basic audit logging.
  4. Wire an event `EquipmentStatusChanged` and start implementing handlers for rental start/complete events.
  5. Add a minimal `PATCH /api/equipment/{id}/status` endpoint (admin only) with WebMvc tests for validation flows.
- Blockers/Notes:
  - Depends on US-EQ-001 (equipment catalog) being available; will mock equipment repository in unit tests where needed.
  - Will coordinate with rental module owners to confirm event shapes for `RentalStarted`/`RentalCompleted`.

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-EQ-001 completion
- Part of Phase 2: Basic Module Functions

## Technical Details

**DDD-Compliant Domain Model:**

```java
// Domain Port (interface) - domain/service layer
public interface StatusTransitionPolicy {
  void validateTransition(@NonNull String fromStatusSlug, @NonNull String toStatusSlug);
}

// Domain Entity - Equipment Aggregate Root
public class Equipment {
  private Long id;
  private String statusSlug;  // Value Object, not Entity reference
    
    // DDD-compliant: uses injected policy (dependency inversion)
    public void changeStatusTo(@NonNull String newStatusSlug, @NonNull StatusTransitionPolicy policy) {
        policy.validateTransition(this.statusSlug, newStatusSlug);
        this.statusSlug = newStatusSlug;
    }

  public void setInitialStatus(@NonNull String initialStatusSlug) {
    this.statusSlug = initialStatusSlug;
  }
}

// EquipmentStatus Aggregate (Reference Data) - Separate Aggregate
public class EquipmentStatus {
    private Long id;
    private String slug;
    private String name;
  private String description;
  private Set<String> allowedTransitions;

  public boolean canTransitionTo(@NonNull String toStatusSlug) {
    return allowedTransitions != null && allowedTransitions.contains(toStatusSlug);
  }
}

// Application Service - implements domain port
@Service
public class EquipmentStatusTransitionPolicy implements StatusTransitionPolicy {
  private final EquipmentStatusRepository statusRepository;

  @Override
  public void validateTransition(@NonNull String fromStatusSlug, @NonNull String toStatusSlug) {
    // Validates status exists and transition is allowed
    EquipmentStatus fromStatus = statusRepository.findBySlug(fromStatusSlug)
            .orElseThrow(() -> new ReferenceNotFoundException(EquipmentStatus.class, fromStatusSlug));

    if (!fromStatus.canTransitionTo(toStatusSlug)) {
      throw new InvalidStatusTransitionException(null, fromStatusSlug, toStatusSlug);
    }
    }
}
```

**API Endpoints:**

```http
# Create status with transitions
POST /api/equipment-statuses
{
  "slug": "available",
  "name": "Available",
  "allowedTransitions": ["reserved", "rented", "in-maintenance"]
}

# Update status transitions
PUT /api/equipment-statuses/available
{
  "slug": "available",
  "name": "Available",
  "allowedTransitions": ["reserved", "scrapped"]
}

# Update equipment (includes status change)
PUT /api/equipment/{id}
{
  "name": "City Bike",
  "serialNumber": "SN-001",
  "statusSlug": "in-maintenance",
  "location": "Warehouse A",
  // other equipment fields...
}
```

**Cross-Module Event Flow:**

```java
// Rental Module - publishes event
@Service
public class CreateRentalReservationService {
    public Rental execute(CreateRentalReservationCommand command) {
        Rental rental = Rental.reserve();
        rentalRepository.save(rental);
        
        // Publish event - Equipment module will react
        eventPublisher.publishEvent(new RentalReserved(
            rental.getId(),
            command.equipmentId(),
            rental.getReservedAt()
        ));
        return rental;
    }
}

// Equipment Module - reacts to event
@Component
public class RentalEventListener {
    @EventListener
    @Async
    public void onRentalReserved(RentalReserved event) {
        // Instead of invoking a dedicated ChangeEquipmentStatusUseCase, use the equipment update use case
        var updateCommand = new UpdateEquipmentCommand(
            event.equipmentId(),
            /* other fields omitted */
            Map.of("statusSlug", "reserved"),
            "system"
        );
        updateEquipmentUseCase.execute(updateCommand);
    }
}
```

**Package Structure:**

- com.github.jenkaby.bikerental.equipment
  - domain
    - model
      - Equipment (stores statusSlug: String, uses StatusTransitionPolicy port)
      - EquipmentStatus (Reference Data Aggregate with allowedTransitions)
    - service
      - StatusTransitionPolicy (port interface)
    - exception
      - InvalidStatusTransitionException
    - event
      - EquipmentStatusChanged
  - application
    - usecase
      - CreateEquipmentStatusUseCase
      - UpdateEquipmentUseCase
    - service
      - CreateEquipmentStatusService
      - UpdateEquipmentService
      - EquipmentStatusTransitionPolicy (implements domain port)
  - infrastructure
    - persistence
      - adapter
        - EquipmentRepositoryAdapter (direct statusSlug mapping)
        - EquipmentStatusRepositoryAdapter
      - entity
        - EquipmentJpaEntity (stores statusSlug: String)
        - EquipmentStatusJpaEntity (with allowedTransitionSlugs)
      - mapper
        - EquipmentJpaMapper (direct statusSlug mapping, no decorator)
        - EquipmentStatusJpaMapper
    - eventlistener
      - RentalEventListener (reacts to rental events)
  - web
    - command
      - EquipmentCommandController (handles PUT /api/equipment/{id})
