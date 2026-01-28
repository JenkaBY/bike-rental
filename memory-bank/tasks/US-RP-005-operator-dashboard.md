# [US-RP-005] - Дашборд оператора (Operator Dashboard)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** reporting  
**Dependencies:** US-RN-009, US-FN-001, US-EQ-001

## Original Request

**Как** Оператор проката / Администратор  
**Я хочу** видеть основные показатели текущего дня на главном экране  
**Чтобы** быстро оценить текущую ситуацию

## User Story Details

**Описание:**  
Система должна отображать основные показатели текущего дня на главном экране.

**Показатели:**

- Количество активных аренд
- Количество просроченных аренд
- Доход за сегодня
- Количество новых клиентов
- Доступное оборудование (по типам)
- Оборудование на обслуживании

**Критерии приемки:**

- Обновление в реальном времени
- Визуальные индикаторы
- Быстрые действия (кнопки)

**Связанные требования:** FR-RP-005

## Thought Process

Operator dashboard provides real-time operational overview. Must be fast, auto-refreshing, and actionable.

**Key Features:**

1. **Real-time Updates**: WebSocket or polling for live data
2. **KPIs**: Active rentals, overdue, revenue, equipment availability
3. **Visual Indicators**: Color coding for alerts (overdue in red)
4. **Quick Actions**: Buttons to process returns, view details
5. **Performance**: Cached queries, optimized for speed

**Technical Approach:**

- Aggregate queries with caching
- WebSocket for real-time updates (optional)
- REST endpoint for dashboard data
- Frontend auto-refresh every 30 seconds

## Implementation Plan

- [ ] Create DashboardQuery use case
- [ ] Implement KPI calculations
- [ ] Add caching for performance
- [ ] Create REST endpoint
- [ ] Add WebSocket support (optional)
- [ ] Implement quick action endpoints
- [ ] Create component tests
- [ ] Write unit tests
- [ ] Frontend integration

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 5.1 | Create dashboard query     | Not Started | 2026-01-26 |       |
| 5.2 | Implement KPI calculations | Not Started | 2026-01-26 |       |
| 5.3 | Add caching                | Not Started | 2026-01-26 |       |
| 5.4 | Create REST endpoint       | Not Started | 2026-01-26 |       |
| 5.5 | Add real-time updates      | Not Started | 2026-01-26 |       |
| 5.6 | Create tests               | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/dashboard` - Get current dashboard data

**Response DTO:**

```java
public record OperatorDashboard(
        LocalDate date,
        int activeRentals,
        int overdueRentals,
        Money todayRevenue,
        int newCustomersToday,
        EquipmentAvailability equipmentAvailability,
        List<OverdueRental> topOverdue,
        LocalDateTime lastUpdated
) {
}

public record EquipmentAvailability(
        int totalAvailable,
        int totalRented,
        int totalMaintenance,
        Map<String, Integer> availableByType
) {
}

public record OverdueRental(
        UUID rentalId,
        String customerName,
        String equipmentSerial,
        int overdueMinutes
) {
}
```

**Caching Strategy:**

```java

@Service
public class DashboardService {

    @Cacheable(value = "dashboard", key = "'current'")
    @CacheEvict(value = "dashboard", key = "'current'",
            beforeInvocation = false,
            condition = "#result != null")
    @Scheduled(fixedDelay = 30000) // Refresh every 30 seconds
    public OperatorDashboard getCurrentDashboard() {
        // Query and aggregate data
    }
}
```

## References

- User Story File: [docs/tasks/us/US-RP-005/us-rp-005.md](../../../docs/tasks/us/US-RP-005/us-rp-005.md)
- Dependencies: US-RN-009, US-FN-001, US-EQ-001
