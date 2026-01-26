# [US-AD-002] - Управление ролями и правами доступа (Role and Permission Management)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** admin  
**Dependencies:** US-AD-001

## Original Request

**Как** Администратор  
**Я хочу** управлять ролями и правами доступа  
**Чтобы** обеспечить безопасность системы и разграничить доступ

## User Story Details

**Описание:**  
Система должна поддерживать ролевую модель доступа к функциям.

**Разрешения по ролям:**

**Оператор проката:**

- Создание/редактирование аренд
- Поиск/создание клиентов
- Возврат оборудования
- Прием оплаты
- Просмотр справочников (только чтение)

**Технический персонал:**

- Работа с оборудованием (ТО, ремонт)
- Изменение статусов оборудования
- Просмотр отчетов по загрузке

**Бухгалтерия:**

- Просмотр финансовых отчетов
- Экспорт данных
- Доступ к истории операций (только чтение)

**Администратор:**

- Полный доступ ко всем функциям

**Связанные требования:** FR-AD-002

## Thought Process

This builds upon US-AD-001 by adding granular permission management beyond basic roles. Considerations:

1. **RBAC Model**: Role-Based Access Control with predefined roles and permissions
2. **Spring Security Integration**: Use @PreAuthorize with custom SpEL expressions
3. **Permission Granularity**: Define fine-grained permissions beyond just roles
4. **UI/UX**: Frontend needs to hide/disable features based on permissions
5. **Scalability**: Design allows adding new permissions without code changes

**Architecture Decisions:**

- Define Permission enum with all system permissions
- Each UserRole maps to a set of Permissions
- Use method-level security (@PreAuthorize)
- Consider hierarchical roles (e.g., ADMIN inherits all permissions)
- Possibly allow custom role creation in future (not in this US)

**Permission Design:**

```java
public enum Permission {
    // Customer permissions
    CUSTOMER_READ, CUSTOMER_CREATE, CUSTOMER_UPDATE,

    // Rental permissions
    RENTAL_CREATE, RENTAL_UPDATE, RENTAL_COMPLETE, RENTAL_CANCEL,

    // Equipment permissions
    EQUIPMENT_READ, EQUIPMENT_CREATE, EQUIPMENT_UPDATE,
    EQUIPMENT_STATUS_CHANGE, EQUIPMENT_DECOMMISSION,

    // Payment permissions
    PAYMENT_CREATE, PAYMENT_VIEW, REFUND_PROCESS,

    // Reporting permissions
    REPORT_FINANCIAL_VIEW, REPORT_EQUIPMENT_VIEW, REPORT_EXPORT,

    // Maintenance permissions
    MAINTENANCE_CREATE, MAINTENANCE_UPDATE, MAINTENANCE_VIEW,

    // Admin permissions
    USER_MANAGEMENT, ROLE_MANAGEMENT, SYSTEM_SETTINGS,
    TARIFF_MANAGEMENT, BACKUP_CREATE, AUDIT_VIEW
}
```

## Implementation Plan

- [ ] Define Permission enum with all system permissions
- [ ] Update UserRole to include permission mappings
- [ ] Create PermissionService for checking permissions
- [ ] Implement method-level security with @PreAuthorize
- [ ] Create custom SpEL expressions for complex permission checks
- [ ] Add API endpoint to get user's permissions
- [ ] Create role permission matrix documentation
- [ ] Update security configuration
- [ ] Create component tests for permission checks
- [ ] Write unit tests for permission logic
- [ ] Write WebMvc tests with different user roles
- [ ] Document permission structure

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                        | Status      | Updated    | Notes |
|-----|------------------------------------|-------------|------------|-------|
| 2.1 | Define Permission enum             | Not Started | 2026-01-26 |       |
| 2.2 | Map roles to permissions           | Not Started | 2026-01-26 |       |
| 2.3 | Implement method security          | Not Started | 2026-01-26 |       |
| 2.4 | Create permission checking service | Not Started | 2026-01-26 |       |
| 2.5 | Add permission query endpoint      | Not Started | 2026-01-26 |       |
| 2.6 | Create tests                       | Not Started | 2026-01-26 |       |
| 2.7 | Document permission matrix         | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-AD-001 completion
- Part of Phase 2: Basic Module Functions

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.admin
├── web.query
│   ├── PermissionQueryController
│   └── dto.UserPermissionsResponse
├── application
│   └── service.PermissionService
├── domain
│   ├── model.UserRole (update with permissions)
│   └── model.Permission (enum)
└── infrastructure
    └── security
        ├── SecurityConfig (update)
        └── PermissionEvaluator (custom)
```

**API Endpoint:**

- `GET /api/auth/permissions` - Get current user's permissions
- Response: `200 OK` with list of permissions

```json
{
  "userId": "uuid",
  "role": "OPERATOR",
  "permissions": [
    "CUSTOMER_CREATE",
    "CUSTOMER_READ",
    "RENTAL_CREATE",
    "PAYMENT_CREATE"
  ]
}
```

**Permission Enum:**

```java
public enum Permission {
    // Customer
    CUSTOMER_READ, CUSTOMER_CREATE, CUSTOMER_UPDATE,

    // Rental
    RENTAL_CREATE, RENTAL_UPDATE, RENTAL_COMPLETE, RENTAL_CANCEL,

    // Equipment
    EQUIPMENT_READ, EQUIPMENT_CREATE, EQUIPMENT_UPDATE,
    EQUIPMENT_STATUS_CHANGE, EQUIPMENT_DECOMMISSION,

    // Payment
    PAYMENT_CREATE, PAYMENT_VIEW, REFUND_PROCESS,

    // Reporting
    REPORT_FINANCIAL_VIEW, REPORT_EQUIPMENT_VIEW, REPORT_EXPORT,

    // Maintenance
    MAINTENANCE_CREATE, MAINTENANCE_UPDATE, MAINTENANCE_VIEW,

    // Admin
    USER_MANAGEMENT, ROLE_MANAGEMENT, SYSTEM_SETTINGS,
    TARIFF_MANAGEMENT, BACKUP_CREATE, AUDIT_VIEW
}
```

**Role-Permission Mapping:**

```java
public enum UserRole {
    OPERATOR(Set.of(
            Permission.CUSTOMER_READ, Permission.CUSTOMER_CREATE, Permission.CUSTOMER_UPDATE,
            Permission.RENTAL_CREATE, Permission.RENTAL_UPDATE, Permission.RENTAL_COMPLETE,
            Permission.EQUIPMENT_READ, Permission.PAYMENT_CREATE, Permission.REFUND_PROCESS
    )),
    TECHNICIAN(Set.of(
            Permission.EQUIPMENT_READ, Permission.EQUIPMENT_UPDATE, Permission.EQUIPMENT_STATUS_CHANGE,
            Permission.MAINTENANCE_CREATE, Permission.MAINTENANCE_UPDATE, Permission.MAINTENANCE_VIEW,
            Permission.REPORT_EQUIPMENT_VIEW
    )),
    ACCOUNTANT(Set.of(
            Permission.PAYMENT_VIEW, Permission.REPORT_FINANCIAL_VIEW,
            Permission.REPORT_EQUIPMENT_VIEW, Permission.REPORT_EXPORT
    )),
    ADMIN(Permission.allPermissions());

    private final Set<Permission> permissions;

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
```

**Method Security:**

```java

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    @PostMapping
    @PreAuthorize("hasAuthority('RENTAL_CREATE')")
    public ResponseEntity<RentalResponse> createRental(@RequestBody CreateRentalRequest request) {
        // ...
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('RENTAL_COMPLETE')")
    public ResponseEntity<RentalResponse> completeRental(@PathVariable UUID id) {
        // ...
    }
}
```

**Custom Permission Evaluator:**

```java

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !(permission instanceof String)) {
            return false;
        }
        UserRole role = getUserRole(authentication);
        return role.hasPermission(Permission.valueOf((String) permission));
    }
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-AD-002/us-ad-002.md](../../../docs/tasks/us/US-AD-002/us-ad-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-AD-001 (User management - must be complete)
- Spring Security Documentation: Method Security
- Related: All feature endpoints will use these permissions
