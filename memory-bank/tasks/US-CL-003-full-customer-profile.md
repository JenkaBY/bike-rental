# [US-CL-003] - Полное создание/редактирование профиля клиента (Full Customer Profile Management)

**Status:** Completed ✅  
**Added:** 2026-01-21  
**Updated:** 2026-01-29  
**Completed:** 2026-01-29  
**Priority:** High  
**Module:** customer  
**Dependencies:** US-CL-002 (Complete)  
**Effort:** 1 day (Jan 29, 2026)

## Original Request

**Как** Оператор проката  
**Я хочу** вводить и редактировать полную информацию о клиенте  
**Чтобы** иметь полный профиль клиента для улучшения качества обслуживания

## User Story Details

**Описание:**  
Система должна предоставлять возможность ввода и редактирования полной информации о клиенте.

**Поля профиля:**

- Номер телефона (обязательное)
- Имя (обязательное)
- Фамилия (обязательное)
- Email (опциональное)
- Дата рождения (опциональное)
- Комментарии (опциональное)
- Дата регистрации (автоматически, immutable)

**Критерии приемки:**

- Все поля доступны для редактирования кроме даты регистрации
- Валидация email и телефона
- ~~Сохранение истории изменений~~ (moved to US-CL-006)

**Связанные требования:** FR-CL-003

## Thought Process

This extends US-CL-002 (quick customer creation) by adding full profile management capabilities. Key considerations:

1. **Update Operations**: PUT endpoint only for full profile updates (PATCH excluded)
2. **Audit Trail**: Deferred to separate task US-CL-006 for cleaner implementation
3. **Validation**: Email and phone validation already exists from US-CL-002
4. **Comments Field**: New field not in US-CL-002, needs to be added to domain and database
5. **Phone Uniqueness**: When updating phone number, must validate it's not taken by another customer (409 Conflict)
6. **Required Fields**: PUT requires all mandatory fields (phone, firstName, lastName) to be present

**Architecture Considerations:**

- Protect createdAt field from updates (immutable)
- Validate that phone number changes don't create duplicates with other customers
- PUT endpoint requires all fields (phone, firstName, lastName mandatory; email, birthDate, comments optional/nullable)
- Use standard REST semantics: 200 OK for successful update, 404 for not found, 409 for duplicate phone

**Event Considerations:**

- Publish `CustomerUpdated` event when profile changes
- Other modules may need to react to customer profile changes (deferred for now)

## Implementation Plan

- [x] Extend Customer domain model with comments field
- [x] Create UpdateCustomerUseCase
- [x] Implement PUT endpoint for full update (all required fields must be present)
- [x] Create Cucumber component tests for update scenarios
- [x] Add validation for immutable fields (createdAt cannot be changed)
- [x] Add validation for phone uniqueness across customers (409 Conflict)
- [x] Write unit tests for update logic
- [x] Write WebMvc tests for PUT endpoint validation
- [x] Create Liquibase migration for comments field
- [ ] ~~Publish CustomerUpdated domain event~~ (deferred)
- [ ] ~~Add audit trail mechanism~~ (moved to US-CL-006)

## Progress Tracking

**Overall Status:** Completed ✅ - 100%

### Subtasks

| ID   | Description                       | Status   | Updated    | Notes                                  |
|------|-----------------------------------|----------|------------|----------------------------------------|
| 1.1  | Extend domain model with comments | Complete | 2026-01-29 | Added to Customer entity               |
| 1.2  | Create update use case            | Complete | 2026-01-29 | UpdateCustomerService implemented      |
| 1.3  | Implement PUT endpoint            | Complete | 2026-01-29 | All required fields validated          |
| 1.4  | Create component tests            | Complete | 2026-01-29 | BDD scenarios for update operations    |
| 1.5  | Write unit tests                  | Complete | 2026-01-29 | 11 tests for UpdateCustomerService     |
| 1.6  | Write WebMvc tests                | Complete | 2026-01-29 | 15 tests for PUT endpoint              |
| 1.7  | Add database migration            | Complete | 2026-01-29 | Liquibase migration for comments field |
| 1.8  | Create VO mappers                 | Complete | 2026-01-29 | PhoneNumberMapper, EmailAddressMapper  |
| 1.9  | Refactor existing mappers         | Complete | 2026-01-29 | All mappers use shared VO mappers      |
| 1.10 | Add controller logging            | Complete | 2026-01-29 | Logging for POST/PUT/GET endpoints     |
| 1.11 | Add email validation              | Complete | 2026-01-29 | @Email annotation with tests           |

## Progress Log

### 2026-01-29

**Implementation Completed** ✅

1. **Domain Layer Updates**
    - Extended Customer entity with `comments` field
    - Updated Customer builder to include comments
    - Maintained immutability of createdAt field

2. **Application Layer - Update Use Case**
    - Created `UpdateCustomerUseCase` interface with `UpdateCustomerCommand` record
    - Implemented `UpdateCustomerService` with:
        - Customer existence validation (throws ResourceNotFoundException)
        - Phone uniqueness validation (allows same customer to keep phone, throws DuplicatePhoneException if phone
          belongs to another customer)
        - Phone normalization via PhoneNumberMapper
        - Mapping via CustomerCommandToDomainMapper
    - Injected dependencies: CustomerRepository, CustomerCommandToDomainMapper, PhoneNumberMapper

3. **Web Layer - PUT Endpoint**
    - Added `PUT /api/customers/{id}` endpoint to CustomerCommandController
    - Uses unified `CustomerRequest` DTO for both POST and PUT
    - Validates all required fields: phone, firstName, lastName
    - Optional fields: email, birthDate, comments
    - Added logging: `[PUT] Updating customer with id: {id}` and success log
    - Returns 200 OK with updated CustomerResponse
    - Error handling: 404 Not Found, 409 Conflict, 400 Bad Request

4. **Mapper Refactoring - Major Improvement**
    - Created shared VO mappers in `customer.shared.mapper`:
        - `PhoneNumberMapper` - String ↔ PhoneNumber conversions
        - `EmailAddressMapper` - String ↔ EmailAddress conversions
    - Refactored all mappers to use shared VO mappers:
        - `CustomerCommandToDomainMapper` - Uses VO mappers via MapStruct
        - `CustomerCommandMapper` - Uses VO mappers, MapStruct-generated toUpdateCommand
        - `CustomerJpaMapper` - Uses VO mappers, removed manual methods
        - `CustomerMapper` - Uses VO mappers
    - Benefits: DRY principle, single source of truth, maintainability

5. **Testing - Comprehensive Coverage**
    - **Unit Tests (11)**: UpdateCustomerServiceTest
        - Success scenarios: full update, same phone, minimal fields
        - Individual optional fields: email, birthDate, comments
        - Validation: customer not found, duplicate phone, phone normalization
        - Data integrity verification with ArgumentCaptor
    - **WebMvc Tests (15)**: Added to CustomerCommandControllerTest
        - Success: all fields, minimal fields, various phone formats, comments
        - Validation errors: blank fields, invalid phone, invalid email, future birthDate
        - Error cases: empty body, malformed JSON, missing fields, invalid UUID
    - **Component Tests**: BDD scenarios in customer-profile-management.feature
        - Consolidated from separate files into single feature
        - Scenario outline with 5 examples for various updates
        - Error scenarios for duplicate phone and not found
        - CustomerResponseTransformer created for cleaner assertions

6. **Database Migration**
    - Liquibase changelog: `customers.update-table_add-comments-column.xml`
    - Added `comments` TEXT column to customers table
    - Nullable field for optional customer notes

7. **Additional Improvements**
    - Added `@Email` validation annotation to CustomerRequest DTO
    - Added email format validation tests (6 invalid formats) for POST and PUT
    - Added controller logging to all methods (POST, PUT, GET)
    - Logging format: `[{HTTP_METHOD}] {Action} {identifier}`
    - Fixed CreateCustomerServiceTest with updated dependencies
    - Updated all tests to include comments parameter

**Test Results:**

- ✅ All unit tests passing (22 total: 11 Create + 11 Update)
- ✅ All WebMvc tests passing (26 total: 11 POST + 15 PUT)
- ✅ All component tests passing (consolidated feature file)
- ✅ Zero compilation errors
- ✅ MapStruct generation successful

**Code Quality Metrics:**

- Implementation: ~400 lines (service, controller, mappers)
- Tests: ~900 lines (unit + WebMvc)
- Test-to-code ratio: 2.25:1
- Test coverage: Success paths, validation, error handling, edge cases
- Architecture: Hexagonal architecture maintained
- Follows Spring Boot and MapStruct best practices

**Total Effort:** 1 day (Jan 29, 2026)  
**Subtasks Completed:** 11/11 (100%)

### 2026-01-28

- Updated task based on implementation decisions:
    - PATCH endpoint excluded - PUT only for full updates
    - Audit trail functionality moved to separate task US-CL-006
    - Clarified PUT requires all mandatory fields (phone, firstName, lastName)
    - Confirmed phone uniqueness validation needed (409 Conflict if phone taken by another customer)
    - Domain events deferred to later phase
- Status: Ready to start with component test (TDD approach)

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.customer
├── web.command
│   ├── CustomerCommandController (extend with PUT)
│   ├── dto.UpdateCustomerRequest
│   └── mapper.CustomerCommandMapper
├── application
│   ├── usecase.UpdateCustomerUseCase
│   └── service.UpdateCustomerService
└── domain
    └── model.Customer (add comments field)
```

**API Endpoints:**

- `PUT /api/customers/{id}` - Full update (all required fields must be present)
    - Request body:
      `{ "phone": "string", "firstName": "string", "lastName": "string", "email": "string|null", "birthDate": "date|null", "comments": "string|null" }`
    - Response: `200 OK` with updated customer details
    - Error responses:
        - `404 Not Found` - Customer ID does not exist
        - `409 Conflict` - Phone number already used by another customer
        - `400 Bad Request` - Validation errors (invalid phone format, invalid email, future birthDate, etc.)

**Database Schema Changes:**

- Add column: `comments TEXT`

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-CL-003/us-cl-003.md](../../../docs/tasks/us/US-CL-003/us-cl-003.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Related: US-CL-002 (customer creation - should be complete first)
