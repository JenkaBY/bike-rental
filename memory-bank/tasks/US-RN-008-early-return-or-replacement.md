# [US-RN-008] - Ранний возврат или замена оборудования (Early Return or Equipment Replacement)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** rental  
**Dependencies:** US-RN-005

## Original Request

**Как** Оператор проката  
**Я хочу** вернуть деньги при раннем возврате или заменить оборудование  
**Чтобы** обеспечить гибкость в обслуживании клиентов

## User Story Details

**Описание:**  
Система должна поддерживать возврат денег при раннем возврате или замене оборудования.

**Критерии приемки:**

- Автоматическая проверка времени с начала аренды
- Если прошло менее 10 минут - возможность отмены аренды
- Возможность замены оборудования без доплаты
- Фиксация причины возврата
- Полный возврат денег или перенос на новое оборудование

**Связанные требования:** FR-RN-008

## Thought Process

Early return/replacement policy improves customer satisfaction. Two scenarios: full refund or equipment swap.

**Business Rules:**

- Within 10 minutes of start → full cancellation possible
- Reason must be recorded (equipment issue, wrong item, etc.)
- Options: full refund OR swap to different equipment
- If swap: transfer prepayment to new rental

**Configuration:**

- Cancellation window: 10 minutes (configurable via US-AD-004)

## Implementation Plan

- [ ] Create CancelRentalUseCase
- [ ] Create ReplaceEquipmentUseCase
- [ ] Implement time window validation
- [ ] Integrate with refund processing (US-FN-002)
- [ ] Create new rental for replacement
- [ ] Add reason tracking
- [ ] Create API endpoints
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                 | Status      | Updated    | Notes |
|-----|-----------------------------|-------------|------------|-------|
| 8.1 | Create cancel use case      | Not Started | 2026-01-26 |       |
| 8.2 | Create replace use case     | Not Started | 2026-01-26 |       |
| 8.3 | Implement validation        | Not Started | 2026-01-26 |       |
| 8.4 | Integrate refund processing | Not Started | 2026-01-26 |       |
| 8.5 | Create tests                | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoints:**

- `POST /api/rentals/{id}/cancel` - Cancel rental (within window)
- `POST /api/rentals/{id}/replace-equipment` - Replace equipment

**Cancel Rental:**

```java

@Service
public class CancelRentalUseCase {

    @Transactional
    public void execute(UUID rentalId, String reason) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow();

        // Validate within cancellation window
        int minutesSinceStart = ChronoUnit.MINUTES.between(
                rental.getStartedAt(), LocalDateTime.now()
        );

        if (minutesSinceStart > config.getCancellationWindowMinutes()) {
            throw new CancellationWindowExpiredException();
        }

        // Cancel rental
        rental.cancel(reason);

        // Process refund
        processRefund(rental);

        // Update equipment status back to AVAILABLE
        equipmentStatusService.changeStatus(
                rental.getEquipmentId(),
                EquipmentStatus.AVAILABLE,
                "Rental cancelled"
        );

        rentalRepository.save(rental);
    }
}
```

**Replace Equipment:**

```java

@Transactional
public Rental replaceEquipment(UUID rentalId, UUID newEquipmentId, String reason) {
    // Validate time window
    // Cancel original rental
    // Create new rental with same customer/tariff
    // Transfer prepayment
    // Return new rental
}
```

## References

- User Story File: [docs/tasks/us/US-RN-008/us-rn-008.md](../../../docs/tasks/us/US-RN-008/us-rn-008.md)
- Integrates: US-FN-002 (refund), US-TR-005 (refund logic)
