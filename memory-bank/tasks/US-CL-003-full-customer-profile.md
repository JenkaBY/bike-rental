# [US-CL-003] - Полное создание/редактирование профиля клиента (Full Customer Profile Management)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** customer  
**Dependencies:** None

## Original Request

**Как** Оператор проката  
**Я хочу** вводить и редактировать полную информацию о клиенте  
**Чтобы** иметь полный профиль клиента для улучшения качества обслуживания

## User Story Details

**Описание:**  
Система должна предоставлять возможность ввода и редактирования полной информации о клиенте.

**Поля профиля:**

- Номер телефона (обязательное)
- Имя
- Фамилия
- Email
- Дата рождения
- Комментарии
- Дата регистрации (автоматически)

**Критерии приемки:**

- Все поля доступны для редактирования кроме даты регистрации
- Валидация email и телефона
- Сохранение истории изменений

**Связанные требования:** FR-CL-003

## Thought Process

This extends US-CL-002 (quick customer creation) by adding full profile management capabilities. Key considerations:

1. **Update Operations**: Need PUT/PATCH endpoint for customer updates
2. **Audit Trail**: "Сохранение истории изменений" suggests we need change tracking
3. **Validation**: Email and phone validation already exists from US-CL-002
4. **Comments Field**: New field not in US-CL-002, needs to be added

**Architecture Considerations:**

- Use domain events for audit trail (CustomerUpdated event)
- Consider using Spring Data Envers for automatic audit history
- Protect createdAt field from updates (immutable)
- Validate that phone number changes don't create duplicates

**Event Considerations:**

- Publish `CustomerUpdated` event when profile changes
- Other modules may need to react to customer profile changes

## Implementation Plan

- [ ] Extend Customer domain model with comments field
- [ ] Create UpdateCustomerUseCase
- [ ] Implement PUT endpoint for full update
- [ ] Implement PATCH endpoint for partial update
- [ ] Add audit trail mechanism (Envers or custom)
- [ ] Create Cucumber tests for update scenarios
- [ ] Add validation for immutable fields
- [ ] Publish CustomerUpdated domain event
- [ ] Write unit tests for update logic
- [ ] Write WebMvc tests for update endpoints
- [ ] Create Liquibase migration for comments field

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 1.1 | Extend domain model        | Not Started | 2026-01-26 |       |
| 1.2 | Create update use case     | Not Started | 2026-01-26 |       |
| 1.3 | Implement update endpoints | Not Started | 2026-01-26 |       |
| 1.4 | Add audit trail mechanism  | Not Started | 2026-01-26 |       |
| 1.5 | Create component tests     | Not Started | 2026-01-26 |       |
| 1.6 | Publish domain events      | Not Started | 2026-01-26 |       |
| 1.7 | Add database migration     | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, awaiting completion of US-CL-002

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.customer
├── web.command
│   ├── CustomerCommandController (extend with PUT/PATCH)
│   ├── dto.UpdateCustomerRequest
│   └── mapper.CustomerCommandMapper
├── application
│   ├── usecase.UpdateCustomerUseCase
│   └── service.UpdateCustomerService
├── domain
│   ├── model.Customer (add comments field)
│   └── event.CustomerUpdated
└── infrastructure
    └── audit (Envers configuration or custom audit)
```

**API Endpoints:**

- `PUT /api/customers/{id}` - Full update (all fields required)
- `PATCH /api/customers/{id}` - Partial update (only changed fields)
- Request body:
  `{ "firstName": "string", "lastName": "string", "email": "string", "birthDate": "date", "comments": "string" }`
- Response: `200 OK` with updated customer details

**Database Schema Changes:**

- Add column: `comments TEXT`
- Consider: audit tables or use Envers for change history

**Domain Events:**

```java
record CustomerUpdated(
        UUID customerId,
        LocalDateTime updatedAt,
        Map<String, Object> changes
) {
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-CL-003/us-cl-003.md](../../../docs/tasks/us/US-CL-003/us-cl-003.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Related: US-CL-002 (customer creation - should be complete first)
