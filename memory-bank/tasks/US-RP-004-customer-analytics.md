# [US-RP-004] - Аналитика по клиентам (Customer Analytics)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** reporting  
**Dependencies:** US-CL-005

## Original Request

**Как** Администратор  
**Я хочу** получать аналитическую информацию о клиентах  
**Чтобы** понимать клиентскую базу и планировать маркетинговые активности

## User Story Details

**Описание:**  
Система должна предоставлять аналитическую информацию о клиентах.

**Показатели:**

- ТОП клиентов по количеству аренд
- ТОП клиентов по сумме оплат
- Новые клиенты за период
- Вернувшиеся клиенты
- Средняя частота аренды
- Сегментация клиентов (новые/постоянные/VIP)

**Критерии приемки:**

- Визуализация данных
- Фильтрация по периодам
- Экспорт списка клиентов по сегментам

**Связанные требования:** FR-RP-004

## Thought Process

Customer analytics enables data-driven marketing and customer relationship management. Builds on customer statistics (
US-CL-005).

**Key Analytics:**

1. **Top Customers**: By rental count and revenue
2. **Customer Acquisition**: New customers in period
3. **Customer Retention**: Returning customers
4. **Segmentation**: Based on loyalty status
5. **Frequency Analysis**: Average rental frequency

**Business Value:**

- Identify VIP customers for special treatment
- Target marketing campaigns by segment
- Measure customer retention
- Understand customer lifetime value

## Implementation Plan

- [ ] Create CustomerAnalyticsQuery use case
- [ ] Implement top customers queries
- [ ] Calculate new vs returning customers
- [ ] Add segmentation analysis
- [ ] Create visualization data
- [ ] Create REST endpoint
- [ ] Implement export functionality
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description             | Status      | Updated    | Notes |
|-----|-------------------------|-------------|------------|-------|
| 4.1 | Create analytics query  | Not Started | 2026-01-26 |       |
| 4.2 | Implement top customers | Not Started | 2026-01-26 |       |
| 4.3 | Add segmentation        | Not Started | 2026-01-26 |       |
| 4.4 | Create REST endpoint    | Not Started | 2026-01-26 |       |
| 4.5 | Create tests            | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/reports/customer-analytics?from=2026-01-01&to=2026-01-31`

**Response DTO:**

```java
public record CustomerAnalyticsReport(
        LocalDate periodStart,
        LocalDate periodEnd,
        List<TopCustomer> topByRentals,
        List<TopCustomer> topByRevenue,
        CustomerAcquisition acquisition,
        SegmentationBreakdown segmentation,
        double averageRentalFrequency
) {
}

public record TopCustomer(
        UUID customerId,
        String name,
        String phone,
        int rentalCount,
        Money totalRevenue,
        LoyaltyStatus loyaltyStatus
) {
}

public record CustomerAcquisition(
        int newCustomers,
        int returningCustomers,
        double retentionRate
) {
}

public record SegmentationBreakdown(
        int newCustomers,
        int regularCustomers,
        int loyalCustomers
) {
}
```

## References

- User Story File: [docs/tasks/us/US-RP-004/us-rp-004.md](../../../docs/tasks/us/US-RP-004/us-rp-004.md)
- Dependencies: US-CL-005
