# [US-CL-005] - Статистика по клиенту (Customer Statistics)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** customer  
**Dependencies:** US-CL-004

## Original Request

**Как** Оператор проката / Администратор  
**Я хочу** видеть статистику использования услуг клиентом  
**Чтобы** понимать ценность клиента и его поведение

## User Story Details

**Описание:**  
Система должна показывать статистику использования услуг клиентом.

**Показатели:**

- Общее количество аренд
- Общая сумма оплат
- Средняя длительность аренды
- Дата последней аренды
- Статус лояльности (новый/постоянный)

**Критерии приемки:**

- Все показатели отображаются в карточке клиента
- Статистика рассчитывается автоматически
- Статус лояльности определяется на основе количества аренд

**Связанные требования:** FR-CL-005

## Thought Process

Customer statistics provide insights for customer relationship management and loyalty programs. Aggregates rental
history data into meaningful metrics.

**Key Metrics:**

1. **Total Rentals**: Count of all completed rentals
2. **Total Revenue**: Sum of all payments from customer
3. **Average Duration**: Mean rental duration in hours
4. **Last Rental Date**: Most recent rental
5. **Loyalty Status**: Segmentation based on rental count

**Loyalty Segments:**

- New: 1-2 rentals
- Regular: 3-10 rentals
- Loyal: 11+ rentals

**Technical Approach:**

- Aggregate queries on rental data
- Cache statistics for performance
- Recalculate on rental completion
- Event-driven updates

## Implementation Plan

- [ ] Create CustomerStatistics domain model
- [ ] Implement statistics calculation service
- [ ] Add loyalty status determination
- [ ] Create REST endpoint
- [ ] Add caching mechanism
- [ ] Listen to RentalCompleted events
- [ ] Update statistics automatically
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                   | Status      | Updated    | Notes |
|-----|-------------------------------|-------------|------------|-------|
| 5.1 | Create statistics model       | Not Started | 2026-01-26 |       |
| 5.2 | Implement calculation service | Not Started | 2026-01-26 |       |
| 5.3 | Add loyalty status logic      | Not Started | 2026-01-26 |       |
| 5.4 | Create REST endpoint          | Not Started | 2026-01-26 |       |
| 5.5 | Add caching                   | Not Started | 2026-01-26 |       |
| 5.6 | Create tests                  | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/customers/{id}/statistics` - Get customer statistics

**Response DTO:**

```java
public record CustomerStatistics(
        UUID customerId,
        int totalRentals,
        int completedRentals,
        int cancelledRentals,
        Money totalRevenue,
        double averageDurationHours,
        LocalDateTime lastRentalDate,
        LocalDateTime firstRentalDate,
        LoyaltyStatus loyaltyStatus,
        String loyaltyDescription
) {
}

public enum LoyaltyStatus {
    NEW("New Customer", 1, 2),
    REGULAR("Regular Customer", 3, 10),
    LOYAL("Loyal Customer", 11, Integer.MAX_VALUE);

    private final String description;
    private final int minRentals;
    private final int maxRentals;

    public static LoyaltyStatus fromRentalCount(int count) {
        for (LoyaltyStatus status : values()) {
            if (count >= status.minRentals && count <= status.maxRentals) {
                return status;
            }
        }
        return NEW;
    }
}
```

**Statistics Service:**

```java

@Service
public class CustomerStatisticsService {

    @Cacheable("customer-statistics")
    public CustomerStatistics calculate(UUID customerId) {
        List<Rental> rentals = rentalRepository.findByCustomerId(customerId);

        int total = rentals.size();
        int completed = (int) rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .count();
        int cancelled = (int) rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.CANCELLED)
                .count();

        Money totalRevenue = rentals.stream()
                .filter(r -> r.getFinalCost() != null)
                .map(Rental::getFinalCost)
                .reduce(Money.ZERO, Money::add);

        double avgDuration = rentals.stream()
                .filter(r -> r.getActualDurationMinutes() != null)
                .mapToInt(Rental::getActualDurationMinutes)
                .average()
                .orElse(0.0) / 60.0; // Convert to hours

        LocalDateTime lastRental = rentals.stream()
                .map(Rental::getStartedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime firstRental = rentals.stream()
                .map(Rental::getStartedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LoyaltyStatus loyalty = LoyaltyStatus.fromRentalCount(completed);

        return new CustomerStatistics(
                customerId, total, completed, cancelled, totalRevenue,
                avgDuration, lastRental, firstRental, loyalty, loyalty.getDescription()
        );
    }
}
```

**Event Handler:**

```java

@EventListener
@CacheEvict(value = "customer-statistics", key = "#event.customerId")
public void onRentalCompleted(RentalCompleted event) {
    // Cache is evicted, will be recalculated on next access
}
```

**SQL Aggregation:**

```sql
SELECT COUNT(*)                                         as total_rentals,
       COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_rentals,
       COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_rentals,
       SUM(final_cost)                                  as total_revenue,
       AVG(actual_duration_minutes) / 60.0              as avg_duration_hours,
       MAX(started_at)                                  as last_rental_date,
       MIN(started_at)                                  as first_rental_date
FROM rentals
WHERE customer_id = ?;
```

## References

- User Story File: [docs/tasks/us/US-CL-005/us-cl-005.md](../../../docs/tasks/us/US-CL-005/us-cl-005.md)
- Dependencies: US-CL-004
- Used by: US-RP-004 (customer analytics)
