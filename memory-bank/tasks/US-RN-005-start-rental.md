# [US-RN-005] - Запуск аренды (Start Rental)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-16  
**Priority:** High  
**Module:** rental  
**Dependencies:** US-RN-001, US-RN-004, US-EQ-004

## Original Request

**Как** Оператор проката  
**Я хочу** активировать аренду после внесения предоплаты  
**Чтобы** начать отсчет времени аренды

## User Story Details

**Описание:**  
Система должна активировать аренду после внесения предоплаты.

**Действия системы:**

- Изменение статуса аренды на "Активна"
- Изменение статуса оборудования на "В аренде"
- Фиксация времени начала
- Расчет ожидаемого времени возврата

**Критерии приемки:**

- Невозможность удалить активную аренду
- Отображение активной аренды в списке текущих
- Оборудование недоступно для новых аренд

**Связанные требования:** FR-RN-005

## Thought Process

Rental activation is the point of no return - equipment leaves the shop and time starts counting. This triggers multiple
system-wide changes through events.

**Key Actions:**

1. Change rental status DRAFT → ACTIVE
2. Change equipment status AVAILABLE → RENTED
3. Set actual start time
4. Calculate expected return time
5. Publish RentalStarted event

**Preconditions:**

- Rental must be in DRAFT status
- Customer selected
- Equipment selected and AVAILABLE
- Tariff selected
- Prepayment received

**Events Published:**

- RentalStarted (rental module)
- EquipmentStatusChanged (equipment module listens)

## Implementation Plan

- [x] Create StartRentalUseCase (implemented in UpdateRentalService.startRental())
- [x] Implement activation validation (Rental.canBeActivated(), Rental.activate())
- [x] Add status transition logic (DRAFT → ACTIVE)
- [x] Publish RentalStarted event (eventPublisher.publish())
- [x] Create equipment status update event handler (RentalEventListener in equipment module)
- [x] Create REST endpoint (PATCH /api/rentals/{id} with status=ACTIVE)
- [x] Add transaction management (@Transactional)
- [x] Create component tests (rental.feature scenarios)
- [x] Write unit tests (UpdateRentalServiceTest)
- [x] Add component test verification for equipment status update

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                            | Status    | Updated    | Notes                                                                  |
|-----|----------------------------------------|-----------|------------|------------------------------------------------------------------------|
| 5.1 | Create start rental use case           | Completed | 2026-02-16 | Implemented in UpdateRentalService.startRental()                       |
| 5.2 | Implement validation                   | Completed | 2026-02-16 | Rental.canBeActivated(), Rental.activate() with validation             |
| 5.3 | Publish events                         | Completed | 2026-02-16 | RentalStarted event published via eventPublisher                       |
| 5.4 | Create REST endpoint                   | Completed | 2026-02-16 | PATCH /api/rentals/{id} with JSON Patch (status=ACTIVE)                |
| 5.5 | Create tests                           | Completed | 2026-02-16 | Unit tests (UpdateRentalServiceTest), Component tests (rental.feature) |
| 5.6 | Create equipment status update handler | Completed | 2026-02-16 | RentalEventListener in equipment/infrastructure/eventlistener          |
| 5.7 | Component tests for equipment update   | Completed | 2026-02-16 | Added verification in rental.feature activation scenario               |

## Progress Log

### 2026-02-16

- **Status:** Completed
- **Implementation:**
    - ✅ Rental activation logic in `UpdateRentalService.startRental()`
    - ✅ Domain validation in `Rental.activate()` method
    - ✅ Prepayment validation before activation
    - ✅ RentalStarted event publishing
    - ✅ REST endpoint via JSON Patch (PATCH /api/rentals/{id} with status=ACTIVE)
    - ✅ Unit tests for activation flow
    - ✅ Component tests for activation scenarios
    - ✅ **RentalEventListener** in `equipment/infrastructure/eventlistener/` - listens to RentalStarted, changes
      equipment status to RENTED via UpdateEquipmentUseCase
    - ✅ Component test verification: activation scenario now includes async DB verification for equipment status=RENTED
    - ✅ **Architecture fixes:**
        - Moved `RentalStarted` event to `shared.domain.event` to break circular dependency (equipment ↔ rental)
        - Fixed `@NamedInterface` placement: moved from `rental/package-info.java` to `rental/event/package-info.java`
        - Refactored `RentalEventListener` to use `EquipmentCommandToDomainMapper` for command creation
        - All ModulithBoundariesTest passing (no cycles, proper module boundaries)

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending
- Part of Phase 3: Main Rental Process

## Technical Details

**RentalEventListener (Equipment Module):**

- **Location:** `equipment/infrastructure/eventlistener/RentalEventListener.java`
- **Trigger:** `RentalStarted` event (Spring ApplicationEventPublisher)
- **Phase:** `@TransactionalEventListener(phase = AFTER_COMMIT)` - runs after rental transaction commits
- **Action:** Loads equipment by ID, builds UpdateEquipmentCommand with status "RENTED", calls UpdateEquipmentUseCase
- **Error handling:** Catches exceptions, logs errors, does not rethrow (keeps main flow intact)
- **Idempotency:** Skips if equipment already RENTED

**API Endpoint:**

- `POST /api/rentals/{id}/start` - Activate rental

**Domain Event:**

```java
record RentalStarted(
        UUID rentalId,
        UUID customerId,
        UUID equipmentId,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt
) {
}
```

**Use Case:**

```java

@Service
public class StartRentalUseCase {

    @Transactional
    public Rental execute(UUID rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));

        // Validate can be started
        if (!rental.canBeActivated()) {
            throw new CannotStartRentalException("Rental cannot be started");
        }

        // Activate rental
        rental.activate();

        // Publish event (equipment status will be updated by listener)
        eventPublisher.publish(new RentalStarted(
                rental.getId(),
                rental.getCustomerId(),
                rental.getEquipmentId(),
                rental.getStartedAt(),
                rental.getExpectedReturnAt()
        ));

        return rentalRepository.save(rental);
    }
}
```

## References

- User Story File: [docs/tasks/us/US-RN-005/us-rn-005.md](../../../docs/tasks/us/US-RN-005/us-rn-005.md)
- Dependencies: US-RN-001, US-RN-004, US-EQ-004
