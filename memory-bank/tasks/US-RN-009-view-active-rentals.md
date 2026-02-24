# [US-RN-009] - Просмотр активных аренд (View Active Rentals)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-18  
**Completed:** 2026-02-18  
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

- ID аренды
- ID клиента (customerId)
- ID оборудования (equipmentId)
- Время начала
- Ожидаемое время возврата
- Время просрочки в минутах (если есть)
- Статус просрочки вычисляется на клиенте: если overdueMinutes > 0, то просрочено
- Стоимость (estimatedCost, finalCost)

**Критерии приемки:**

- Обновление в реальном времени
- Цветовая индикация просрочек
- Быстрый доступ к возврату
- Фильтрация по статусу и опционально по customerId (на данном этапе без фильтра по equipmentId)

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

- [x] Create active rentals query - `FindRentalsUseCase` and `FindRentalsService` implemented
- [x] Implement overdue calculation - `RentalOverdueCalculator` with Clock injection
- [x] Add sorting and filtering - Filtering by status and customerId, default sorting by expectedReturnAt
- [x] Create REST endpoint - `GET /api/rentals?status=ACTIVE&customerId={uuid}`
- [x] Add pagination - Spring Data Pageable with default page size 20
- [x] Create DTO with enriched data - `RentalSummaryResponse` with overdueMinutes
- [x] Create component tests - Cucumber feature tests with `RentalSummaryResponseTransformer`
- [x] Write query optimization - Repository methods without JOINs, proper indexing support

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                   | Status   | Updated    | Notes                                               |
|-----|-------------------------------|----------|------------|-----------------------------------------------------|
| 9.1 | Create query use case         | Complete | 2026-02-18 | FindRentalsUseCase with FindRentalsQuery            |
| 9.2 | Implement overdue calculation | Complete | 2026-02-18 | RentalOverdueCalculator with Clock injection        |
| 9.3 | Add filtering                 | Complete | 2026-02-18 | Filter by status (RentalStatus enum) and customerId |
| 9.4 | Create REST endpoint          | Complete | 2026-02-18 | GET /api/rentals?status=ACTIVE&customerId={uuid}    |
| 9.5 | Create tests                  | Complete | 2026-02-18 | Unit, WebMvc, and component tests                   |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Part of Phase 3: Main Rental Process

### 2026-02-18

- **Task completed successfully**
- **Implementation Summary:**
  - Created `RentalQueryController` with RESTful endpoint `GET /api/rentals?status=ACTIVE`
  - Implemented `FindRentalsUseCase` and `FindRentalsService` with query pattern
  - Created `RentalSummaryResponse` DTO (short response for list view)
  - Implemented `RentalOverdueCalculator` with `Clock` injection for testable time-dependent logic
  - Created `RentalOverdueMapper` (abstract class) and integrated into `RentalQueryMapper`
  - Added filtering by `status` (RentalStatus enum) and `customerId` (UUID)
  - Default sorting by `expectedReturnAt` field (applied at service/repository level)
  - Used `Page.map()` method for transforming `Page<Rental>` to `Page<RentalSummaryResponse>`
  - Implemented repository methods: `findByStatus`, `findByCustomerId`, `findByStatusAndCustomerId`
  - Created `RentalSummaryResponseTransformer` for component tests
  - Added comprehensive tests: unit tests for service, WebMvc tests for controller, component tests
- **Architectural Decisions:**
  - No SQL JOINs across module boundaries (strict modular monolith)
  - No enrichment of DTOs with data from other modules (only IDs as references)
  - Overdue calculation returns `Integer overdueMinutes` (0 if not overdue, not null)
  - `isOverdue` field removed (computed client-side: `overdueMinutes > 0`)
  - Controller logic minimized: sorting and filtering at service/repository level
  - Domain objects (`Rental`) returned from use case, DTO conversion in web layer
- **Technical Debt Created:**
  - [TECH-006] Integrate specification-arg-resolver for rental filtering - Created task for future refactoring to
    replace manual filtering logic with declarative Specifications
- **Files Created/Modified:**
  - `RentalQueryController.java` - REST endpoint
  - `FindRentalsUseCase.java` - Use case interface
  - `FindRentalsService.java` - Service implementation
  - `RentalSummaryResponse.java` - Response DTO
  - `RentalQueryMapper.java` - DTO mapper (extends RentalOverdueMapper)
  - `RentalOverdueMapper.java` - Overdue calculation mapper
  - `RentalOverdueCalculator.java` - Overdue calculation service with Clock
  - `RentalRepository.java` - Added query methods
  - `RentalRepositoryAdapter.java` - Implemented query methods
  - `RentalJpaRepository.java` - JPA repository methods
  - `RentalQueryControllerTest.java` - WebMvc tests
  - `FindRentalsServiceTest.java` - Unit tests
  - `rental-query.feature` - Component tests
  - `RentalSummaryResponseTransformer.java` - Cucumber transformer

## Technical Details

**API Endpoint:**

- `GET /api/rentals?status=ACTIVE` - List active rentals (RESTful подход)
- Query params: `?status=ACTIVE&customerId={uuid}&page=0&size=20`
- **Примечание:** Фильтр по equipmentId не реализуется на данном этапе
- Сортировка скрыта, применяется по умолчанию (просроченные первыми)

**Response DTO:**

Создать короткий `RentalSummaryResponse` для спискового представления:

```java
public record RentalSummaryResponse(
        Long id,
        UUID customerId,
        Long equipmentId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt,
        Integer overdueMinutes  // только для ACTIVE, 0 если нет просрочки
) {}

// Примечание: 
// - isOverdue вычисляется на клиенте: overdueMinutes > 0
// - Существующий RentalResponse используется для детального просмотра (GET /api/rentals/{id})
```

**Query (БЕЗ JOIN'ов):**

```sql
-- Без фильтра по customerId
SELECT r.*
FROM rentals r
WHERE r.status = 'ACTIVE'
ORDER BY CASE
             WHEN r.expected_return_at < CURRENT_TIMESTAMP
                 THEN r.expected_return_at
             ELSE CURRENT_TIMESTAMP
             END ASC
LIMIT ? OFFSET ?;

-- С фильтром по customerId
SELECT r.*
FROM rentals r
WHERE r.status = 'ACTIVE'
  AND r.customer_id = ?
ORDER BY CASE
             WHEN r.expected_return_at < CURRENT_TIMESTAMP
                 THEN r.expected_return_at
             ELSE CURRENT_TIMESTAMP
             END ASC
LIMIT ? OFFSET ?;
```

**Примечание:** Фильтр по equipment_id не реализуется на данном этапе. Сортировка применяется по умолчанию в application
слое.

**Архитектурные ограничения:**

- **НИКАКИХ JOIN'ов** - модуль rental не имеет доступа к базе других модулей
- **НЕТ обогащения данными** customer/equipment - response содержит только данные из rental модуля
- Если нужны данные customer/equipment, клиент делает отдельные запросы к соответствующим модулям

## References

- User Story File: [docs/tasks/us/US-RN-009/us-rn-009.md](../../../docs/tasks/us/US-RN-009/us-rn-009.md)
- Dependency: US-RN-005
