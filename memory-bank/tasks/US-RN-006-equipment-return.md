# [US-RN-006] - Возврат оборудования (Equipment Return)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** rental  
**Dependencies:** US-RN-005, US-EQ-003, US-RN-007, US-TR-002

## Original Request

**Как** Оператор проката  
**Я хочу** обработать возврат оборудования клиентом  
**Чтобы** завершить аренду и рассчитать итоговую стоимость

## User Story Details

**Описание:**  
Система должна обрабатывать процесс возврата оборудования клиентом.

**Последовательность действий:**

1. Сканирование метки оборудования
2. Автоматическое определение аренды
3. Фиксация времени возврата
4. Расчет фактического времени аренды
5. Расчет итоговой стоимости
6. Расчет доплаты (если есть)
7. Прием доплаты (если требуется)
8. Закрытие аренды

**Критерии приемки:**

- Автоматический расчет всех показателей
- Отображение детализации стоимости
- Формирование итогового чека

**Связанные требования:** FR-RN-006

## Thought Process

Equipment return is the culmination of the rental process. Integrates: tag scanning, duration calculation, cost
calculation, payment processing.

**Workflow:**

1. Scan equipment (US-EQ-003)
2. Calculate duration (US-RN-007)
3. Calculate cost (US-TR-002)
4. If additional payment needed → record payment (US-FN-001)
5. Complete rental (ACTIVE → COMPLETED)
6. Update equipment status (RENTED → AVAILABLE)
7. Publish RentalCompleted event

**Events:**

- RentalCompleted
- EquipmentStatusChanged (via event handler)

## Implementation Plan

- [ ] Create ReturnEquipmentUseCase
- [ ] Integrate tag scanning
- [ ] Calculate final cost
- [ ] Handle additional payment
- [ ] Update rental status
- [ ] Publish events
- [ ] Generate final receipt
- [ ] Create component tests
- [ ] Write integration tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 6.1 | Create return use case     | Not Started | 2026-01-26 |       |
| 6.2 | Integrate cost calculation | Not Started | 2026-01-26 |       |
| 6.3 | Handle additional payment  | Not Started | 2026-01-26 |       |
| 6.4 | Update statuses            | Not Started | 2026-01-26 |       |
| 6.5 | Publish events             | Not Started | 2026-01-26 |       |
| 6.6 | Create tests               | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `POST /api/rentals/{id}/return` - Process return
- Request: `{ "uid": "ABC123", "returnTime": "2026-01-26T15:30:00" }`

**Use Case:**

```java

@Service
@Transactional
public class ReturnEquipmentUseCase {

    public RentalReturnResult execute(ReturnEquipmentCommand command) {
        // 1. Find and validate rental
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow();

        // 2. Calculate cost
        CostBreakdown cost = costCalculator.calculate(
                rental, command.returnTime()
        );

        // 3. Record additional payment if needed
        Money additionalPayment = cost.totalCost()
                .subtract(rental.getPrepayment());

        if (additionalPayment.isPositive()) {
            recordPayment(rental, additionalPayment, command.paymentMethod());
        }

        // 4. Complete rental
        rental.complete(command.returnTime(), cost.totalCost());

        // 5. Publish event (equipment status updated by listener)
        eventPublisher.publish(new RentalCompleted(
                rental.getId(),
                rental.getEquipmentId(),
                command.returnTime(),
                cost.totalCost()
        ));

        return new RentalReturnResult(rental, cost);
    }
}
```

## References

- User Story File: [docs/tasks/us/US-RN-006/us-rn-006.md](../../../docs/tasks/us/US-RN-006/us-rn-006.md)
- Dependencies: US-RN-005, US-EQ-003, US-RN-007, US-TR-002
