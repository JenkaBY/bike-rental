# [US-CL-001] - Поиск клиента по номеру телефона (Customer Search by Phone)

**Status:** In Progress  
**Added:** 2026-01-21  
**Updated:** 2026-01-28  
**Priority:** High  
**Module:** customer  
**Dependencies:** None

## Original Request

**Как** Оператор проката  
**Я хочу** найти клиента по последним 4 цифрам телефона  
**Чтобы** быстро идентифицировать клиента при оформлении аренды

## User Story Details

**Описание:**  
Система должна предоставлять возможность поиска клиента по частичному совпадению номера телефона.

**Критерии приемки:**

- Ввод 4 цифр возвращает всех клиентов с совпадением
- Поддержка поиска от 4 до 11 цифр
- Время отклика < 1 секунды
- Поиск работает в режиме реального времени (при вводе)
- Система отображает список всех совпадений

**Связанные требования:** FR-CL-001

## Thought Process

This user story is critical for the rental workflow as it's the first step when a customer arrives to rent equipment.
The implementation should focus on:

1. **Performance**: Sub-second response time requires efficient database indexing
2. **Flexibility**: Support for partial matching from 4 to 11 digits
3. **User Experience**: Real-time search as user types
4. **Database**: Consider using PostgreSQL pattern matching or full-text search

**Key Technical Considerations:**

- Use database indexing on phone number field for performance
- Implement query optimization for LIKE queries
- Consider using PostgreSQL's pg_trgm extension for similarity search
- Add debouncing on frontend to reduce unnecessary queries
- Return limited results (e.g., top 50) to prevent performance issues

**Architecture Alignment:**

- Query endpoint in `customer` module
- Read-only operation, use query-side pattern (CQRS)
- No events needed for search operations

## Implementation Plan

- [ ] Create Cucumber feature for customer search scenarios
- [ ] Implement query use case (SearchCustomersByPhoneUseCase)
- [ ] Create query repository method with indexed lookup
- [ ] Implement REST endpoint (GET /api/customers/search)
- [ ] Add database index migration for phone field
- [ ] Optimize query performance (EXPLAIN ANALYZE)
- [ ] Add pagination support if needed
- [ ] Write unit tests for search logic
- [ ] Write WebMvc tests for query endpoint
- [ ] Verify performance requirements (<1 second)

## Progress Tracking

**Overall Status:** In Progress - 15%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 1.1 | Create component test      | Complete    | 2026-01-28 |       |
| 1.2 | Implement search use case  | Not Started | 2026-01-26 |       |
| 1.3 | Create query endpoint      | Not Started | 2026-01-26 |       |
| 1.4 | Add database index         | Not Started | 2026-01-26 |       |
| 1.5 | Optimize query performance | Not Started | 2026-01-26 |       |
| 1.6 | Write tests                | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, awaiting completion of US-CL-002

### 2026-01-28

- Added component test feature for phone search with 10-item limit and minimum length validation
- Added component test steps for list size and list content assertions

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.customer
├── web.query
│   ├── CustomerQueryController
│   ├── dto.CustomerSearchRequest
│   └── dto.CustomerSearchResponse
├── application
│   ├── usecase.SearchCustomersByPhoneUseCase
│   └── service.CustomerSearchService
└── domain
    └── repository.CustomerRepository (add search method)
```

**API Endpoint:**

- `GET /api/customers/search?phone={digits}`
- Query param: `phone` (4-11 digits)
- Response: `200 OK` with array of matching customers
- Response format: `[{ "id": "uuid", "phone": "string", "firstName": "string", "lastName": "string" }]`

**Database Considerations:**

- Add index: `CREATE INDEX idx_customers_phone ON customers(phone)`
- Consider GIN index with pg_trgm for fuzzy matching
- Query: `SELECT * FROM customers WHERE phone LIKE '%{digits}' LIMIT 50`

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-CL-001/us-cl-001.md](../../../docs/tasks/us/US-CL-001/us-cl-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-CL-002 should be complete for consistent customer module structure
