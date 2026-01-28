# [US-RN-005] - Запуск аренды (Start Rental)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
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

- [ ] Create StartRentalUseCase
- [ ] Implement activation validation
- [ ] Add status transition logic
- [ ] Publish RentalStarted event
- [ ] Create equipment status update event handler
- [ ] Create REST endpoint
- [ ] Add transaction management
- [ ] Create component tests
- [ ] Write unit tests
- [ ] Write integration tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                  | Status      | Updated    | Notes |
|-----|------------------------------|-------------|------------|-------|
| 5.1 | Create start rental use case | Not Started | 2026-01-26 |       |
| 5.2 | Implement validation         | Not Started | 2026-01-26 |       |
| 5.3 | Publish events               | Not Started | 2026-01-26 |       |
| 5.4 | Create REST endpoint         | Not Started | 2026-01-26 |       |
| 5.5 | Create tests                 | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending
- Part of Phase 3: Main Rental Process

## Technical Details

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
