# [US-CL-004] - История аренд клиента (Customer Rental History)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** customer  
**Dependencies:** US-RN-001, US-RN-006

## Original Request

**Как** Оператор проката / Бухгалтерия  
**Я хочу** просматривать историю всех аренд клиента  
**Чтобы** видеть полную информацию о взаимодействии клиента с сервисом

## User Story Details

**Описание:**  
Система должна отображать историю всех аренд клиента.

**Критерии приемки:**

- Список всех аренд с датами начала и окончания
- Информация об арендованном оборудовании
- Сумма оплаты и доплат
- Статус аренды (активная, завершена, отменена)
- Возможность фильтрации по периоду

**Связанные требования:** FR-CL-004

## Thought Process

Customer rental history provides complete view of customer's interaction with the service. Useful for customer service
and loyalty analysis.

**Key Features:**

1. **Complete History**: All rentals regardless of status
2. **Equipment Details**: What was rented
3. **Financial Summary**: Total costs per rental
4. **Status Tracking**: Current and historical states
5. **Date Filtering**: Filter by date range

**Technical Approach:**

- Query rentals by customerId
- Join with equipment and tariff data
- Aggregate financial information
- Support pagination and filtering
- Order by rental date descending

## Implementation Plan

- [ ] Create CustomerRentalHistoryQuery use case
- [ ] Implement query with joins
- [ ] Create DTO for rental history
- [ ] Add date range filtering
- [ ] Create REST endpoint
- [ ] Add pagination support
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description            | Status      | Updated    | Notes |
|-----|------------------------|-------------|------------|-------|
| 4.1 | Create query use case  | Not Started | 2026-01-26 |       |
| 4.2 | Implement rental query | Not Started | 2026-01-26 |       |
| 4.3 | Add filtering          | Not Started | 2026-01-26 |       |
| 4.4 | Create REST endpoint   | Not Started | 2026-01-26 |       |
| 4.5 | Create tests           | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `GET /api/customers/{id}/rentals` - Get customer rental history
- Query params: `?from=2026-01-01&to=2026-01-31&status=COMPLETED&page=0&size=20`

**Response DTO:**

```java
public record CustomerRentalHistory(
        UUID customerId,
        int totalRentals,
        List<RentalSummary> rentals
) {
}

public record RentalSummary(
        UUID rentalId,
        EquipmentSummary equipment,
        LocalDateTime startedAt,
        LocalDateTime returnedAt,
        int durationMinutes,
        Money totalCost,
        RentalStatus status
) {
}
```

**Query Implementation:**

```java

@Service
public class CustomerRentalHistoryQuery {

    public Page<RentalSummary> execute(UUID customerId, RentalHistoryFilter filter, Pageable pageable) {
        return rentalRepository.findByCustomerIdWithFilters(customerId, filter, pageable)
                .map(this::toRentalSummary);
    }

    private RentalSummary toRentalSummary(Rental rental) {
        Equipment equipment = equipmentRepository.findById(rental.getEquipmentId())
                .orElseThrow();

        return new RentalSummary(
                rental.getId(),
                new EquipmentSummary(equipment.getSerialNumber(), equipment.getModel()),
                rental.getStartedAt(),
                rental.getActualReturnAt(),
                rental.getActualDurationMinutes(),
                rental.getFinalCost(),
                rental.getStatus()
        );
    }
}
```

**SQL Query:**

```sql
SELECT r.*, e.serial_number, e.model
FROM rentals r
         JOIN equipment e ON r.equipment_id = e.id
WHERE r.customer_id = ?
  AND (? IS NULL OR r.started_at >= ?)
  AND (? IS NULL OR r.started_at <= ?)
  AND (? IS NULL OR r.status = ?)
ORDER BY r.started_at DESC LIMIT ?
OFFSET ?;
```

## References

- User Story File: [docs/tasks/us/US-CL-004/us-cl-004.md](../../../docs/tasks/us/US-CL-004/us-cl-004.md)
- Dependencies: US-RN-001, US-RN-006
- Used by: US-CL-005 (customer statistics)
