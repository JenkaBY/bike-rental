# [US-FN-003] - Финансовая история по аренде (Rental Financial History)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** finance  
**Dependencies:** US-FN-001, US-FN-002, US-RN-004, US-RN-006

## Original Request

**Как** Оператор проката / Бухгалтерия  
**Я хочу** видеть полную финансовую историю каждой аренды  
**Чтобы** отслеживать все платежи и возвраты по аренде

## User Story Details

**Описание:**  
Система должна хранить полную финансовую историю каждой аренды.

**Информация:**

- Предоплата (сумма, способ, дата/время)
- Доплата (сумма, способ, дата/время)
- Возвраты (сумма, причина, дата/время)
- Итоговая сумма
- Статус оплаты

**Критерии приемки:**

- Хронологический список всех операций
- Детализация по типам операций
- Связь с чеками/документами

**Связанные требования:** FR-FN-003

## Thought Process

Financial history provides complete audit trail for each rental's financial transactions. Essential for accounting and
dispute resolution.

**Key Requirements:**

1. **Chronological View**: All transactions ordered by time
2. **Transaction Types**: Payments (prepayment, additional) and refunds
3. **Receipt Links**: Reference to receipt numbers
4. **Aggregation**: Total amounts, balance calculations

**Technical Approach:**

- Query payments and refunds by rentalId
- Aggregate data from Payment and Refund tables
- Calculate running balance
- Group by transaction type

**Data Sources:**

- Payments table (from US-FN-001)
- Refunds table (from US-FN-002)
- Rentals table (for rental details)

## Implementation Plan

- [ ] Create RentalFinancialHistoryQuery use case
- [ ] Implement query joining payments and refunds
- [ ] Create DTO for financial transaction history
- [ ] Add balance calculation logic
- [ ] Create REST endpoint
- [ ] Add pagination support
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                 | Status      | Updated    | Notes |
|-----|-----------------------------|-------------|------------|-------|
| 3.1 | Create query use case       | Not Started | 2026-01-26 |       |
| 3.2 | Implement transaction query | Not Started | 2026-01-26 |       |
| 3.3 | Add balance calculation     | Not Started | 2026-01-26 |       |
| 3.4 | Create REST endpoint        | Not Started | 2026-01-26 |       |
| 3.5 | Create tests                | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/rentals/{id}/financial-history` - Get rental financial history

**Response DTO:**

```java
public record RentalFinancialHistory(
        UUID rentalId,
        Money totalPaid,
        Money totalRefunded,
        Money netAmount,
        List<FinancialTransaction> transactions
) {
}

public record FinancialTransaction(
        UUID transactionId,
        TransactionType type, // PAYMENT, REFUND
        String subType, // PREPAYMENT, ADDITIONAL_PAYMENT, RENTAL_CANCELLED
        Money amount,
        PaymentMethod method,
        LocalDateTime timestamp,
        String receiptNumber,
        String notes
) {
}
```

**Query Implementation:**

```java

@Service
public class RentalFinancialHistoryQuery {

    public RentalFinancialHistory execute(UUID rentalId) {
        // Get all payments for rental
        List<Payment> payments = paymentRepository.findByRentalId(rentalId);

        // Get all refunds for rental
        List<Refund> refunds = refundRepository.findByRentalIdThroughPayments(rentalId);

        // Combine and sort chronologically
        List<FinancialTransaction> transactions = new ArrayList<>();

        payments.forEach(p -> transactions.add(toTransaction(p)));
        refunds.forEach(r -> transactions.add(toTransaction(r)));

        transactions.sort(Comparator.comparing(FinancialTransaction::timestamp));

        // Calculate totals
        Money totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(Money.ZERO, Money::add);

        Money totalRefunded = refunds.stream()
                .map(Refund::getAmount)
                .reduce(Money.ZERO, Money::add);

        Money netAmount = totalPaid.subtract(totalRefunded);

        return new RentalFinancialHistory(
                rentalId, totalPaid, totalRefunded, netAmount, transactions
        );
    }
}
```

**SQL Query:**

```sql
-- Get all financial transactions for a rental
SELECT p.id,
       'PAYMENT'      as type,
       p.payment_type as sub_type,
       p.amount,
       p.payment_method,
       p.created_at,
       p.receipt_number,
       NULL           as notes
FROM payments p
WHERE p.rental_id = ?
UNION ALL
SELECT r.id,
       'REFUND'        as type,
       r.refund_reason as sub_type,
       r.amount,
       r.refund_method,
       r.created_at,
       r.receipt_number,
       r.notes
FROM refunds r
         JOIN payments p ON r.payment_id = p.id
WHERE p.rental_id = ?
ORDER BY created_at;
```

## References

- User Story File: [docs/tasks/us/US-FN-003/us-fn-003.md](../../../docs/tasks/us/US-FN-003/us-fn-003.md)
- Dependencies: US-FN-001, US-FN-002, US-RN-004, US-RN-006
- Used by: US-RP-003 (financial reconciliation)
