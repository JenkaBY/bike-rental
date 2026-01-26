# [US-RP-001] - Отчет по доходам за период (Revenue Report by Period)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** reporting  
**Dependencies:** US-FN-001, US-FN-002

## Original Request

**Как** Администратор  
**Я хочу** формировать отчет о доходах за выбранный период  
**Чтобы** анализировать финансовые показатели бизнеса

## User Story Details

**Описание:**  
Система должна формировать отчет о доходах за выбранный период.

**Показатели:**

- Общая сумма доходов
- Доходы по типам оборудования
- Доходы по тарифам
- Количество аренд
- Средний чек
- Доплаты за просрочки
- Возвраты

**Критерии приемки:**

- Выбор периода (день, неделя, месяц, произвольный)
- Экспорт в Excel/PDF
- Графическое представление данных
- Сравнение с предыдущим периодом

**Связанные требования:** FR-RP-001

## Thought Process

Revenue reporting is essential for business analytics and financial planning. Must aggregate payment data across
multiple dimensions.

**Key Features:**

1. **Period Selection**: Flexible date range filtering
2. **Multi-dimensional Analysis**: By equipment type, tariff, time period
3. **KPIs**: Total revenue, rental count, average check, overtime charges
4. **Export**: Excel and PDF formats
5. **Visualization**: Charts and graphs
6. **Comparison**: Period-over-period analysis

**Technical Approach:**

- Query payments and refunds for date range
- Aggregate by equipment type, tariff
- Calculate KPIs (average, totals)
- Generate charts using library
- Export using Apache POI (Excel) and iText (PDF)

## Implementation Plan

- [ ] Create RevenueReportQuery use case
- [ ] Implement aggregation queries
- [ ] Calculate KPIs (total, average, overtime)
- [ ] Create report DTO structure
- [ ] Implement Excel export (Apache POI)
- [ ] Implement PDF export (iText/Flying Saucer)
- [ ] Add chart generation
- [ ] Create REST endpoint
- [ ] Add period comparison logic
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description            | Status      | Updated    | Notes |
|-----|------------------------|-------------|------------|-------|
| 1.1 | Create report query    | Not Started | 2026-01-26 |       |
| 1.2 | Implement aggregations | Not Started | 2026-01-26 |       |
| 1.3 | Add Excel export       | Not Started | 2026-01-26 |       |
| 1.4 | Add PDF export         | Not Started | 2026-01-26 |       |
| 1.5 | Create visualizations  | Not Started | 2026-01-26 |       |
| 1.6 | Create tests           | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/reports/revenue?from=2026-01-01&to=2026-01-31&format=json|excel|pdf`

**Response DTO:**

```java
public record RevenueReport(
        LocalDate periodStart,
        LocalDate periodEnd,
        Money totalRevenue,
        Money totalRefunded,
        Money netRevenue,
        int totalRentals,
        Money averageCheck,
        Money overtimeCharges,
        List<RevenueByEquipmentType> byEquipmentType,
        List<RevenueByTariff> byTariff,
        PeriodComparison comparison
) {
}
```

**SQL Aggregation:**

```sql
SELECT et.name           as equipment_type,
       COUNT(r.id)       as rental_count,
       SUM(r.final_cost) as total_revenue,
       AVG(r.final_cost) as average_revenue
FROM rentals r
         JOIN equipment e ON r.equipment_id = e.id
         JOIN equipment_types et ON e.equipment_type_id = et.id
WHERE r.started_at BETWEEN ? AND ?
  AND r.status = 'COMPLETED'
GROUP BY et.id, et.name;
```

## References

- User Story File: [docs/tasks/us/US-RP-001/us-rp-001.md](../../../docs/tasks/us/US-RP-001/us-rp-001.md)
- Dependencies: US-FN-001, US-FN-002
