# [US-TR-005] - Возврат средств при отмене (Refund on Cancellation)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** tariff  
**Dependencies:** US-RN-008, US-FN-002

## Original Request

**Как** Оператор проката  
**Я хочу** выполнить возврат средств при отмене в течение 10 минут  
**Чтобы** обеспечить справедливое обслуживание клиентов

## User Story Details

**Описание:**  
Система должна выполнять возврат средств при отмене или замене в течение 10 минут.

**Критерии приемки:**

- Автоматическая проверка времени с начала аренды
- Если прошло <= 10 минут - возврат полной суммы предоплаты
- Или перенос на новое оборудование без доплаты
- Формирование документа на возврат
- Учет в финансовой отчетности
- Изменение статуса аренды на "Отменена"

**Связанные требования:** FR-TR-005

## Thought Process

Refund on cancellation is the financial side of early cancellation (US-RN-008). Implements business policy for customer
satisfaction.

**Refund Rules:**

- Within cancellation window (10 minutes) → full refund
- After window → calculate actual usage cost
- Refund = prepayment - actual cost

**Integration:**

- Called by US-RN-008 (cancel/replace)
- Uses US-FN-002 (refund processing)
- Applies refund reason: RENTAL_CANCELLED

## Implementation Plan

- [ ] Create RefundOnCancellationService
- [ ] Implement refund calculation
- [ ] Integrate with finance refund (US-FN-002)
- [ ] Add refund documentation
- [ ] Create unit tests
- [ ] Create integration tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description              | Status      | Updated    | Notes |
|-----|--------------------------|-------------|------------|-------|
| 5.1 | Create refund service    | Not Started | 2026-01-26 |       |
| 5.2 | Implement calculation    | Not Started | 2026-01-26 |       |
| 5.3 | Integrate with US-FN-002 | Not Started | 2026-01-26 |       |
| 5.4 | Create tests             | Not Started | 2026-01-26 |       |

## Technical Details

**Refund Service:**

```java

@Service
public class CancellationRefundService {

    public Money calculateRefundAmount(Rental rental) {
        int minutesSinceStart = ChronoUnit.MINUTES.between(
                rental.getStartedAt(), LocalDateTime.now()
        );

        if (minutesSinceStart <= config.getCancellationWindowMinutes()) {
            // Full refund
            return rental.getPrepayment();
        } else {
            // Partial refund based on actual usage
            CostBreakdown cost = costCalculator.calculate(
                    rental, LocalDateTime.now()
            );
            Money refund = rental.getPrepayment().subtract(cost.totalCost());
            return refund.isPositive() ? refund : Money.ZERO;
        }
    }

    public void processRefund(Rental rental, String reason) {
        Money refundAmount = calculateRefundAmount(rental);

        if (refundAmount.isPositive()) {
            // Get original payment
            UUID paymentId = rental.getPaymentIds().get(0);

            // Process refund via finance module
            processRefundUseCase.execute(new ProcessRefundCommand(
                    paymentId,
                    refundAmount,
                    RefundReason.RENTAL_CANCELLED,
                    reason
            ));
        }
    }
}
```

**Integration Point:**

```java
// Called from US-RN-008
public void cancelRental(UUID rentalId, String reason) {
    Rental rental = rentalRepository.findById(rentalId).orElseThrow();

    // Process refund
    cancellationRefundService.processRefund(rental, reason);

    // Cancel rental
    rental.cancel(reason);
    rentalRepository.save(rental);
}
```

## References

- User Story File: [docs/tasks/us/US-TR-005/us-tr-005.md](../../../docs/tasks/us/US-TR-005/us-tr-005.md)
- Used by: US-RN-008
- Depends on: US-FN-002
