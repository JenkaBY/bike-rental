# [US-RP-003] - Финансовая сверка для бухгалтерии (Financial Reconciliation Report)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** reporting  
**Dependencies:** US-FN-003, US-FN-004

## Original Request

**Как** Бухгалтерия  
**Я хочу** получать детальный финансовый отчет  
**Чтобы** вести бухгалтерский учет и сверять данные

## User Story Details

**Описание:**  
Система должна формировать детальный финансовый отчет для бухгалтерского учета.

**Содержание отчета:**

- Все операции прихода/расхода
- Разбивка по способам оплаты
- Наличные/безналичные
- Открытые/закрытые смены
- Акты сверки касс
- Незакрытые аренды

**Критерии приемки:**

- Соответствие бухгалтерским стандартам
- Детализация до уровня операции
- Экспорт в формате Excel
- Возможность выгрузки в учетные системы (1С)

**Связанные требования:** FR-RP-003

## Thought Process

Financial reconciliation report is critical for accounting compliance. Must provide transaction-level detail and cash
register reconciliation.

**Key Components:**

1. **Transaction Log**: All payments and refunds
2. **Payment Method Breakdown**: Cash vs card vs electronic
3. **Cash Register Reconciliation**: Shift summaries with discrepancies
4. **Open Rentals**: Unpaid or partially paid rentals
5. **Export**: Excel format, potentially 1C integration

**Integration Points:**

- US-FN-003: Rental financial history
- US-FN-004: Cash register data

## Implementation Plan

- [ ] Create FinancialReconciliationQuery use case
- [ ] Aggregate all financial transactions
- [ ] Include cash register reconciliation
- [ ] Identify open/unpaid rentals
- [ ] Implement Excel export
- [ ] Add 1C export format (optional)
- [ ] Create REST endpoint
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                 | Status      | Updated    | Notes |
|-----|-----------------------------|-------------|------------|-------|
| 3.1 | Create reconciliation query | Not Started | 2026-01-26 |       |
| 3.2 | Add cash register data      | Not Started | 2026-01-26 |       |
| 3.3 | Identify open rentals       | Not Started | 2026-01-26 |       |
| 3.4 | Implement Excel export      | Not Started | 2026-01-26 |       |
| 3.5 | Create tests                | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/reports/financial-reconciliation?from=2026-01-01&to=2026-01-31&format=excel`

**Response DTO:**

```java
public record FinancialReconciliationReport(
        LocalDate periodStart,
        LocalDate periodEnd,
        List<TransactionEntry> transactions,
        PaymentMethodBreakdown paymentBreakdown,
        List<ShiftSummary> shiftSummaries,
        List<OpenRental> openRentals,
        Money totalIncome,
        Money totalRefunded,
        Money netIncome
) {
}

public record PaymentMethodBreakdown(
        Money cash,
        Money card,
        Money electronic
) {
}

public record ShiftSummary(
        UUID shiftId,
        UUID operatorId,
        LocalDateTime opened,
        LocalDateTime closed,
        Money expected,
        Money actual,
        Money discrepancy
) {
}
```

## References

- User Story File: [docs/tasks/us/US-RP-003/us-rp-003.md](../../../docs/tasks/us/US-RP-003/us-rp-003.md)
- Dependencies: US-FN-003, US-FN-004
