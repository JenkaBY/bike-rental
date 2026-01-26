# [US-CL-002] - Быстрое создание клиента (Quick Customer Creation)

**Status:** In Progress  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** customer  
**Dependencies:** None

## Original Request

**Как** Оператор проката  
**Я хочу** быстро создать профиль клиента только с номером телефона  
**Чтобы** не задерживать процесс оформления аренды при отсутствии клиента в системе

## User Story Details

**Описание:**  
Система должна позволять быстро создать профиль клиента с минимальными данными (только номер телефона).

**Критерии приемки:**

- Возможность создания клиента только с номером телефона
- Валидация формата номера телефона
- Автоматическое присвоение уникального ID клиента
- Время создания < 2 секунд

**Связанные требования:** FR-CL-002

## Thought Process

This is the first user story being implemented in the BikeRent system. The approach follows TDD methodology:

1. **Test-First Approach**: Start with component tests (Cucumber) to define the API contract and behavior
2. **Clean Architecture**: Implement using Spring Modulith with clear separation:
    - `web` layer for REST controllers
    - `application` layer for use cases
    - `domain` layer for business logic and entities
    - `infrastructure` layer for database access
3. **Domain-Driven Design**: Use value objects (PhoneNumber, EmailAddress) to encapsulate validation logic
4. **Event-Driven**: Prepare for inter-module communication using Spring Application Events

**Key Technical Decisions:**

- Use Liquibase for database migrations to support version control of schema changes
- Implement phone number validation with normalization to support various input formats
- Use Jakarta Bean Validation for request validation at the controller level
- Repository pattern with Spring Data JPA for data persistence

## Implementation Plan

- [x] Create Cucumber feature file for customer creation scenarios
- [x] Implement domain model (Customer entity, value objects)
- [x] Create repository interface and implementation
- [x] Implement use case (CreateCustomerUseCase)
- [x] Create REST controller with validation
- [x] Add Liquibase migration for customers table
- [ ] Fix compilation errors (Spring Security configuration)
- [ ] Run and verify all tests pass
- [ ] Update documentation

## Progress Tracking

**Overall Status:** In Progress - 67%

### Subtasks

| ID  | Description               | Status      | Updated    | Notes                                            |
|-----|---------------------------|-------------|------------|--------------------------------------------------|
| 1.1 | Create component test     | Complete    | 2026-01-25 | Cucumber feature test created                    |
| 1.2 | Implement API endpoint    | Complete    | 2026-01-25 | POST /api/customers endpoint implemented         |
| 1.3 | Implement validation      | Complete    | 2026-01-25 | Phone number format validation and normalization |
| 1.4 | Create database migration | Complete    | 2026-01-25 | Liquibase migration added                        |
| 1.5 | Fix compilation errors    | In Progress | 2026-01-26 | Spring Security and repository access issues     |
| 1.6 | Run tests                 | Not Started | 2026-01-26 | Verify all tests pass                            |

## Progress Log

### 2026-01-26

- Migrated task tracking from docs/tasks/user-stories.md to Memory Bank structure
- Identified current blockers: compilation errors related to Spring Security configuration
- Status: 4 out of 6 subtasks complete (67% done)
- Next step: Fix compilation errors, then run tests

### 2026-01-25

- Completed core implementation:
    - Domain model: Customer entity with PhoneNumber and EmailAddress value objects
    - Application layer: CreateCustomerService implementing CreateCustomerUseCase
    - Web layer: CustomerCommandController with validation
    - Infrastructure: CustomerRepository interface
    - Database: Liquibase migration for customers table
- Component tests written using Cucumber BDD framework
- Encountered compilation issues that need to be resolved

### 2026-01-21

- User story created and prioritized as first Phase 1 task
- Status changed to In-Progress
- Initial planning completed

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.customer
├── web.command
│   ├── CustomerCommandController
│   ├── dto.CreateCustomerRequest
│   └── mapper.CustomerCommandMapper
├── application
│   ├── usecase.CreateCustomerUseCase
│   └── service.CreateCustomerService
├── domain
│   ├── model.Customer
│   ├── model.vo.PhoneNumber
│   ├── model.vo.EmailAddress
│   ├── repository.CustomerRepository
│   └── exception.DuplicatePhoneException
└── infrastructure
    └── persistence (to be implemented)
```

**API Endpoint:**

- `POST /api/customers`
- Request body:
  `{ "phone": "string", "firstName": "string", "lastName": "string", "email": "string", "birthDate": "date" }`
- Response: `201 Created` with customer details including generated ID

**Database Schema:**

- Table: `customers`
- Columns: id (UUID), phone (varchar unique), first_name, last_name, email, birth_date, created_at, updated_at

## Known Issues

1. **Compilation Errors**: Spring Security configuration conflicts with repository access
2. **Test Execution**: Cannot verify tests until compilation errors are resolved

## References

- User Story File: [docs/tasks/us/US-CL-002/us-cl-002.md](../../../docs/tasks/us/US-CL-002/us-cl-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Module Structure: [docs/single-module-details-v1.md](../../../docs/single-module-details-v1.md)
