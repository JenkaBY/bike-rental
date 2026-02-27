# [US-RN-006] - Возврат оборудования (Equipment Return)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-27  
**Priority:** High  
**Module:** rental  
**Dependencies:** US-RN-005, TECH-007, US-RN-007, US-TR-002

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

1. Client scans equipment tag → gets UID
2. Find active rental by equipmentUid: `GET /api/rentals?status=ACTIVE&equipmentUid={uid}`
3. Calculate duration (US-RN-007)
4. Calculate cost (US-TR-002)
5. If additional payment needed → record payment (US-FN-001)
6. Complete rental (ACTIVE → COMPLETED)
7. Update equipment status (RENTED → AVAILABLE) via RentalCompleted event
8. Publish RentalCompleted event

**Events:**

- RentalCompleted
- EquipmentStatusChanged (via event handler)

## Implementation Plan

- [x] Create ReturnEquipmentUseCase
- [x] Support finding rental by equipmentUid / equipmentId / rentalId (via TECH-007)
- [x] Calculate final cost via TariffFacade.calculateRentalCost()
- [x] Handle additional payment
- [x] Update rental status (ACTIVE → COMPLETED)
- [x] Publish RentalCompleted event
- [x] POST /api/rentals/return endpoint with RentalReturnResponse + CostBreakdown
- [x] Create WebMvc tests
- [x] Create component tests (rental-return.feature)
- [ ] Unit tests for ReturnEquipmentService

## Progress Tracking

**Overall Status:** Completed - 95%

### Subtasks

| ID   | Description                           | Status      | Updated    | Notes                                             |
|------|---------------------------------------|-------------|------------|---------------------------------------------------|
| 6.1  | Create ReturnEquipmentUseCase         | Complete    | 2026-02-26 | Interface + ReturnEquipmentCommand + Result       |
| 6.2  | Implement ReturnEquipmentService      | Complete    | 2026-02-26 | Full 10-step flow                                 |
| 6.3  | Integrate cost calculation            | Complete    | 2026-02-26 | TariffFacade.calculateRentalCost() used           |
| 6.4  | Handle additional payment             | Complete    | 2026-02-26 | FinanceFacade.recordAdditionalPayment()           |
| 6.5  | Update rental status                  | Complete    | 2026-02-26 | rental.complete(totalCost) → COMPLETED            |
| 6.6  | Publish RentalCompleted event         | Complete    | 2026-02-26 | Via EventPublisher to "rental-events" exchanger   |
| 6.7  | POST /api/rentals/return endpoint     | Complete    | 2026-02-26 | Universal: rentalId / equipmentId / equipmentUid  |
| 6.8  | Response DTO with CostBreakdown       | Complete    | 2026-02-26 | RentalReturnResponse with full cost breakdown     |
| 6.9  | WebMvc tests                          | Complete    | 2026-02-26 | RentalCommandControllerTest — valid/invalid cases |
| 6.10 | Component tests                       | Complete    | 2026-02-26 | rental-return.feature — 5 scenarios               |
| 6.11 | Unit tests for ReturnEquipmentService | Not Started | 2026-02-27 | No ReturnEquipmentServiceTest.java yet            |

## Progress Log

### 2026-02-26

- Implemented full `ReturnEquipmentService` with 10-step flow: find rental → validate status → calculate duration →
  calculate cost → get prepayment → calculate additional payment → record payment → complete rental → save → publish
  event
- `POST /api/rentals/return` endpoint unified: accepts rentalId, equipmentId, or equipmentUid (priority: rentalId >
  equipmentUid > equipmentId)
- `ReturnEquipmentResult` record: rental + RentalCost + additionalPayment + PaymentInfo
- `RentalReturnResponse` with `CostBreakdown` (baseCost, overtimeCost, finalCost, actualMinutes, billableMinutes,
  plannedMinutes, overtimeMinutes, forgivenessApplied, calculationMessage)
- WebMvc tests for valid/invalid return requests in `RentalCommandControllerTest`
- Component test feature file `rental-return.feature` with 5 scenarios: return by rentalId, return by equipmentUid,
  return with overtime + additional payment, rental not found (404), rental not in ACTIVE status (422)
- `TariffFacade.calculateRentalCost()` is the unified cost calculation method — `CalculateEstimatedCostService` /
  `CalculateEstimatedCostUseCase` removed, all delegated to `CalculateRentalCostUseCase`

### 2026-02-27

- Task status updated from Pending/Not Started to Completed/95%
- Identified remaining gap: unit tests for ReturnEquipmentService (subtask 6.11)

## Technical Details

**API Endpoint:**

- `POST /api/rentals/{id}/return` - Process return by rental ID
- `POST /api/rentals/return-by-equipment-uid` - Process return by equipment UID (alternative)
- Request: `{ "returnTime": "2026-01-26T15:30:00" }` (equipmentUid can be found via GET
  /api/rentals?status=ACTIVE&equipmentUid={uid})

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
- Dependencies: US-RN-005, TECH-007, US-RN-007, US-TR-002
- Related: US-EQ-003 removed (scanning happens on client side, UID lookup via GET /api/rentals?equipmentUid={uid})
