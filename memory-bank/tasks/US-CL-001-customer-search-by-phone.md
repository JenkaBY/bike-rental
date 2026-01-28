# [US-CL-001] - Поиск клиента по номеру телефона (Customer Search by Phone)

**Status:** Completed  
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

- [x] Create Cucumber feature for customer search scenarios
- [x] Implement query use case (SearchCustomersByPhoneUseCase)
- [x] Create query repository method with indexed lookup
- [x] Implement REST endpoint (GET /api/customers?phone={digits})
- [x] Add database index migration for phone field
- [x] Optimize query performance (indexed lookup + result limit)
- [ ] Add pagination support if needed
- [x] Write unit tests for search logic
- [x] Write WebMvc tests for query endpoint
- [x] Verify performance requirements (<1 second)

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                | Status   | Updated    | Notes                           |
|-----|----------------------------|----------|------------|---------------------------------|
| 1.1 | Create component test      | Complete | 2026-01-28 |                                 |
| 1.2 | Implement search use case  | Complete | 2026-01-28 |                                 |
| 1.3 | Create query endpoint      | Complete | 2026-01-28 |                                 |
| 1.4 | Add database index         | Complete | 2026-01-28 | Exists in v1 migration          |
| 1.5 | Optimize query performance | Complete | 2026-01-28 | Indexed lookup + limit          |
| 1.6 | Write tests                | Complete | 2026-01-28 | Unit + WebMvc + component tests |

## Progress Log

### 2026-01-28

- Completed US-CL-001 implementation and testing
- Finalized search endpoint and result limit configuration

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

- `GET /api/customers?phone={digits}`
- Query param: `phone` (4-11 digits)
- Response: `200 OK` with array of matching customers (max 10)
- Response format: `[{ "id": "uuid", "phone": "string", "firstName": "string", "lastName": "string" }]`

**Database Considerations:**

- Add index: `CREATE INDEX idx_customers_phone ON customers(phone)`
- Consider GIN index with pg_trgm for fuzzy matching
- Query: `SELECT * FROM customers WHERE phone LIKE '%{digits}%' LIMIT 10`

## Known Issues

None
