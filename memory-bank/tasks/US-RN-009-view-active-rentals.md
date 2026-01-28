# [US-RN-009] - Просмотр активных аренд (View Active Rentals)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** rental  
**Dependencies:** US-RN-005

## Original Request

**Как** Оператор проката / Администратор  
**Я хочу** видеть список всех активных аренд  
**Чтобы** контролировать текущие аренды и отслеживать просрочки

## User Story Details

**Описание:**  
Система должна отображать список всех активных аренд.

**Информация в списке:**

- Клиент (имя, телефон)
- Оборудование
- Время начала
- Ожидаемое время возврата
- Время просрочки (если есть)
- Статус (в срок / просрочено)

**Критерии приемки:**

- Обновление в реальном времени
- Цветовая индикация просрочек
- Быстрый доступ к возврату
- Фильтрация по оборудованию/клиенту

**Связанные требования:** FR-RN-009

## Thought Process

Active rentals dashboard is critical for operations monitoring. Must be fast and provide clear overdue indicators.

**Key Features:**

- List all ACTIVE rentals
- Calculate overdue time for each
- Sort by overdue duration (most overdue first)
- Filter and search capabilities
- Pagination for performance

## Implementation Plan

- [ ] Create active rentals query
- [ ] Implement overdue calculation
- [ ] Add sorting and filtering
- [ ] Create REST endpoint
- [ ] Add pagination
- [ ] Create DTO with enriched data
- [ ] Create component tests
- [ ] Write query optimization

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                   | Status      | Updated    | Notes |
|-----|-------------------------------|-------------|------------|-------|
| 9.1 | Create query use case         | Not Started | 2026-01-26 |       |
| 9.2 | Implement overdue calculation | Not Started | 2026-01-26 |       |
| 9.3 | Add filtering                 | Not Started | 2026-01-26 |       |
| 9.4 | Create REST endpoint          | Not Started | 2026-01-26 |       |
| 9.5 | Create tests                  | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Part of Phase 3: Main Rental Process

## Technical Details

**API Endpoint:**

- `GET /api/rentals/active` - List active rentals
- Query params: `?page=0&size=20&sortBy=overdue&customerId={id}&equipmentId={id}`

**Response DTO:**

```java
public record ActiveRentalResponse(
        UUID id,
        CustomerSummary customer,
        EquipmentSummary equipment,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt,
        Duration overdueTime,
        boolean isOverdue
) {
}
```

**Query:**

```sql
SELECT r.*, c.first_name, c.last_name, c.phone, e.serial_number
FROM rentals r
         JOIN customers c ON r.customer_id = c.id
         JOIN equipment e ON r.equipment_id = e.id
WHERE r.status = 'ACTIVE'
ORDER BY CASE
             WHEN r.expected_return_at < CURRENT_TIMESTAMP
                 THEN r.expected_return_at
             ELSE CURRENT_TIMESTAMP
             END ASC;
```

## References

- User Story File: [docs/tasks/us/US-RN-009/us-rn-009.md](../../../docs/tasks/us/US-RN-009/us-rn-009.md)
- Dependency: US-RN-005
