# [US-RP-002] - Отчет по загрузке оборудования (Equipment Utilization Report)

- Dependencies: US-EQ-005, US-RN-006
- User Story File: [docs/tasks/us/US-RP-002/us-rp-002.md](../../../docs/tasks/us/US-RP-002/us-rp-002.md)

## References

```
}
    return (usageHours / (double) availableHours) * 100.0;
    int availableHours = periodDays * 24; // Assuming 24/7 availability
    int periodDays = (int) ChronoUnit.DAYS.between(start, end);
public double calculateUtilization(int usageHours, LocalDate start, LocalDate end) {
```java
**Calculation:**

```

) {}
int idleHours
Money totalRevenue,
double utilizationPercent,
int rentalCount,
int totalUsageHours,
String equipmentType,
String serialNumber,
UUID equipmentId,
public record EquipmentUtilization(

) {}
List<EquipmentUtilization> equipment
LocalDate periodEnd,
LocalDate periodStart,
public record EquipmentUtilizationReport(

```java
**Response DTO:**

- `GET /api/reports/equipment-utilization?from=2026-01-01&to=2026-01-31&sortBy=utilization`
**API Endpoint:**

## Technical Details

| 2.5 | Create tests                  | Not Started | 2026-01-26 |       |
| 2.4 | Create REST endpoint          | Not Started | 2026-01-26 |       |
| 2.3 | Add sorting/filtering         | Not Started | 2026-01-26 |       |
| 2.2 | Calculate metrics             | Not Started | 2026-01-26 |       |
| 2.1 | Create utilization query      | Not Started | 2026-01-26 |       |
|-----|-------------------------------|-------------|------------|-------|
| ID  | Description                   | Status      | Updated    | Notes |

### Subtasks

**Overall Status:** Not Started - 0%

## Progress Tracking

- [ ] Write unit tests
- [ ] Create component tests
- [ ] Implement export functionality
- [ ] Create REST endpoint
- [ ] Add sorting capabilities
- [ ] Aggregate revenue per equipment
- [ ] Calculate utilization percentage
- [ ] Calculate usage hours per equipment
- [ ] Create EquipmentUtilizationQuery use case

## Implementation Plan

- Sort by utilization, revenue, rental count
- Identify over-utilized equipment (candidates for expansion)
- Identify underutilized equipment (candidates for removal)
**Analysis Features:**

- **Idle Time**: Available but not rented
- **Revenue**: Total income from equipment
- **Utilization %**: Usage hours / available hours * 100
- **Rental Count**: Number of rentals in period
- **Usage Hours**: Total time equipment was rented
**Key Metrics:**

Utilization reporting helps optimize equipment fleet and identify underperforming assets. Uses data from US-EQ-005 (usage tracking).

## Thought Process

**Связанные требования:** FR-RP-002

- Экспорт отчета
- Выявление недозагруженного оборудования
- Сортировка по загрузке
- Список всего оборудования с показателями
**Критерии приемки:**

- Время простоя
- Доход от единицы оборудования
- Процент загрузки от общего времени
- Количество аренд за период
- Общее время в аренде (часы)
**Показатели по единице:**

Система должна показывать статистику использования каждой единицы оборудования.
**Описание:**  

## User Story Details

**Чтобы** оптимизировать парк оборудования и планировать закупки
**Я хочу** видеть статистику использования каждой единицы оборудования  
**Как** Администратор / Технический персонал  

## Original Request

**Dependencies:** US-EQ-005, US-RN-006
**Module:** reporting  
**Priority:** Low  
**Updated:** 2026-01-26  
**Added:** 2026-01-26  
**Status:** Pending  

