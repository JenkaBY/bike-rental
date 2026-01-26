# [US-AD-001] - Управление пользователями (User Management)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** admin  
**Dependencies:** None

## Original Request

**Как** Администратор  
**Я хочу** управлять учетными записями пользователей  
**Чтобы** контролировать доступ к системе

## User Story Details

**Описание:**  
Система должна предоставлять возможность управления учетными записями пользователей.

**Действия:**

- Создание нового пользователя
- Редактирование данных пользователя
- Назначение ролей
- Блокировка/разблокировка пользователя
- Сброс пароля

**Роли:**

- Администратор
- Оператор проката
- Технический персонал
- Бухгалтерия (только чтение отчетов)

**Критерии приемки:**

- Список всех пользователей
- CRUD операции над пользователями
- Разграничение прав по ролям

**Связанные требования:** FR-AD-001

## Thought Process

User management is foundational for security and access control. This integrates with Spring Security:

1. **Spring Security Integration**: Use existing Spring Security framework
2. **Role-Based Access**: Four distinct roles with different permissions
3. **Password Management**: Secure password hashing (BCrypt)
4. **Account Status**: Active/blocked user management

**Architecture Decisions:**

- Integrate with Spring Security for authentication
- Use `@PreAuthorize` annotations for role-based access
- Store users in database (UserDetails implementation)
- Password reset generates temporary password or reset token
- Audit trail for user management operations

**Security Considerations:**

- Passwords must be hashed (BCryptPasswordEncoder)
- Admin role required for user management operations
- Prevent admin from disabling their own account
- Session management for blocked users

**Domain Model:**

```
AppUser (Aggregate Root)
├── id: UUID
├── username: String (unique)
├── passwordHash: String
├── role: UserRole (enum)
├── isActive: Boolean
├── createdAt: LocalDateTime
└── lastLoginAt: LocalDateTime
```

## Implementation Plan

- [ ] Create AppUser domain model
- [ ] Create UserRole enum with permissions
- [ ] Implement UserDetails integration with Spring Security
- [ ] Create user repository
- [ ] Implement CRUD use cases
- [ ] Create password reset use case
- [ ] Implement block/unblock use cases
- [ ] Create REST endpoints (admin-only)
- [ ] Add database migration for users table
- [ ] Configure Spring Security with roles
- [ ] Add method-level security annotations
- [ ] Create component tests for user management
- [ ] Write unit tests for security logic
- [ ] Write WebMvc tests with security context

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                 | Status      | Updated    | Notes |
|-----|-----------------------------|-------------|------------|-------|
| 1.1 | Create domain model         | Not Started | 2026-01-26 |       |
| 1.2 | Integrate Spring Security   | Not Started | 2026-01-26 |       |
| 1.3 | Implement CRUD use cases    | Not Started | 2026-01-26 |       |
| 1.4 | Create REST endpoints       | Not Started | 2026-01-26 |       |
| 1.5 | Add database migration      | Not Started | 2026-01-26 |       |
| 1.6 | Configure role-based access | Not Started | 2026-01-26 |       |
| 1.7 | Create component tests      | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, independent task but critical for security

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.admin
├── web.command
│   ├── UserManagementController
│   ├── dto.CreateUserRequest
│   └── dto.UpdateUserRequest
├── application
│   ├── usecase.CreateUserUseCase
│   ├── usecase.UpdateUserUseCase
│   ├── usecase.BlockUserUseCase
│   ├── usecase.ResetPasswordUseCase
│   └── service.*
├── domain
│   ├── model.AppUser
│   ├── model.UserRole (enum)
│   ├── repository.UserRepository
│   └── security.UserDetailsServiceImpl
└── infrastructure
    └── security (Spring Security config)
```

**API Endpoints:**

- `POST /api/admin/users` - Create user (ADMIN only)
- `PUT /api/admin/users/{id}` - Update user (ADMIN only)
- `GET /api/admin/users` - List users (ADMIN only)
- `GET /api/admin/users/{id}` - Get user details (ADMIN only)
- `PATCH /api/admin/users/{id}/block` - Block user (ADMIN only)
- `PATCH /api/admin/users/{id}/unblock` - Unblock user (ADMIN only)
- `POST /api/admin/users/{id}/reset-password` - Reset password (ADMIN only)

**Database Schema:**

```sql
CREATE TABLE app_users
(
    id            UUID PRIMARY KEY,
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(50)         NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP           NOT NULL,
    last_login_at TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_users_username ON app_users (username);
CREATE INDEX idx_users_role ON app_users (role);
```

**UserRole Enum:**

```java
public enum UserRole {
    ADMIN(Set.of(
            Permission.USER_MANAGEMENT,
            Permission.SYSTEM_CONFIG,
            Permission.ALL_OPERATIONS
    )),
    OPERATOR(Set.of(
            Permission.RENTAL_MANAGEMENT,
            Permission.CUSTOMER_MANAGEMENT,
            Permission.PAYMENT_PROCESSING
    )),
    TECHNICIAN(Set.of(
            Permission.EQUIPMENT_MANAGEMENT,
            Permission.MAINTENANCE_RECORDS
    )),
    ACCOUNTANT(Set.of(
            Permission.REPORTS_READ_ONLY,
            Permission.FINANCIAL_REPORTS
    ));

    private final Set<Permission> permissions;
}
```

**Spring Security Configuration:**

```java

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ACCOUNTANT")
                        .requestMatchers("/api/**").authenticated()
                );
    }
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-AD-001/us-ad-001.md](../../../docs/tasks/us/US-AD-001/us-ad-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Critical dependency for: US-AD-002, US-AD-004, US-AD-005
- Security Integration: Spring Security documentation
