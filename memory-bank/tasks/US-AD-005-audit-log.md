# [US-AD-005] - Журнал аудита (Audit Log)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** admin  
**Dependencies:** US-AD-001

## Original Request

**Как** Администратор  
**Я хочу** просматривать журнал всех значимых действий пользователей  
**Чтобы** отслеживать активность и обеспечивать безопасность

## User Story Details

**Описание:**  
Система должна вести журнал всех значимых действий пользователей.

**Логируемые события:**

- Вход/выход пользователя
- Создание/изменение/удаление записей
- Изменение настроек
- Финансовые операции
- Попытки несанкционированного доступа

**Информация в журнале:**

- Дата/время
- Пользователь
- Тип события
- Описание действия
- Затронутые объекты (ID)
- IP-адрес (для веб-версии)

**Критерии приемки:**

- Автоматическое логирование
- Поиск и фильтрация по журналу
- Экспорт журнала
- Хранение не менее 1 года

**Связанные требования:** FR-AD-005

## Thought Process

Audit logging is critical for security, compliance, and forensic analysis. Must capture all significant system actions.

**Key Requirements:**

1. **Comprehensive Coverage**: Log all CRUD operations, auth events, financial transactions
2. **Immutability**: Audit logs cannot be modified or deleted
3. **Performance**: Async logging to avoid impacting operations
4. **Searchability**: Efficient querying and filtering
5. **Retention**: Long-term storage (1+ years)

**Technical Approach:**

- Spring AOP for automatic method-level auditing
- Spring Data Envers for entity-level auditing
- Custom audit event publisher
- Async event processing
- Separate audit database or table partitioning

**Event Categories:**

- **Authentication**: Login, logout, failed attempts
- **Authorization**: Access denied events
- **Data Changes**: Create, update, delete
- **Financial**: Payments, refunds, cash register operations
- **Configuration**: Settings changes, user management

## Implementation Plan

- [ ] Create AuditLog domain model
- [ ] Implement AOP-based audit interceptor
- [ ] Add Spring Data Envers integration
- [ ] Create audit event publisher
- [ ] Implement async event processing
- [ ] Add IP address capture
- [ ] Create search/filter queries
- [ ] Create REST endpoints
- [ ] Implement export functionality
- [ ] Add database migration
- [ ] Add retention policy job
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description               | Status      | Updated    | Notes |
|-----|---------------------------|-------------|------------|-------|
| 5.1 | Create audit domain model | Not Started | 2026-01-26 |       |
| 5.2 | Implement AOP interceptor | Not Started | 2026-01-26 |       |
| 5.3 | Add Envers integration    | Not Started | 2026-01-26 |       |
| 5.4 | Create event publisher    | Not Started | 2026-01-26 |       |
| 5.5 | Implement search/filter   | Not Started | 2026-01-26 |       |
| 5.6 | Add export functionality  | Not Started | 2026-01-26 |       |
| 5.7 | Create tests              | Not Started | 2026-01-26 |       |

## Technical Details

**Domain Model:**

```java

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue
    private UUID id;

    private LocalDateTime timestamp;
    private UUID userId;
    private String username;
    private AuditEventType eventType;
    private String action;
    private String entityType;
    private UUID entityId;
    private String description;
    private String ipAddress;
    private String oldValue; // JSON
    private String newValue; // JSON

    // Immutable - no setters after creation
}

public enum AuditEventType {
    AUTHENTICATION,
    AUTHORIZATION,
    ENTITY_CREATED,
    ENTITY_UPDATED,
    ENTITY_DELETED,
    CONFIGURATION_CHANGED,
    FINANCIAL_OPERATION
}
```

**AOP Interceptor:**

```java

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    @AfterReturning(
            pointcut = "@annotation(audited)",
            returning = "result"
    )
    public void auditMethod(JoinPoint joinPoint, Audited audited, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        AuditLog log = AuditLog.builder()
                .timestamp(LocalDateTime.now())
                .userId(getCurrentUserId())
                .username(getCurrentUsername())
                .eventType(audited.eventType())
                .action(signature.getName())
                .description(audited.description())
                .ipAddress(getClientIpAddress())
                .build();

        auditService.logAsync(log);
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    AuditEventType eventType();

    String description() default "";
}
```

**Usage Example:**

```java

@Service
public class UserService {

    @Audited(
            eventType = AuditEventType.ENTITY_CREATED,
            description = "User created"
    )
    public User createUser(CreateUserCommand command) {
        // Create user logic
        return user;
    }

    @Audited(
            eventType = AuditEventType.CONFIGURATION_CHANGED,
            description = "Business rule updated"
    )
    public void updateBusinessRule(String key, String value) {
        // Update logic
    }
}
```

**Async Audit Service:**

```java

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("auditExecutor")
    public void logAsync(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> search(AuditSearchCriteria criteria, Pageable pageable) {
        // Complex query with filters
        return auditLogRepository.findByCriteria(criteria, pageable);
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-");
        executor.initialize();
        return executor;
    }
}
```

**Event Listeners:**

```java

@Component
public class AuditEventListener {

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        auditService.logAsync(AuditLog.builder()
                .eventType(AuditEventType.AUTHENTICATION)
                .action("LOGIN_SUCCESS")
                .username(event.getAuthentication().getName())
                .ipAddress(getClientIp())
                .build());
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureEvent event) {
        auditService.logAsync(AuditLog.builder()
                .eventType(AuditEventType.AUTHENTICATION)
                .action("LOGIN_FAILED")
                .username(event.getAuthentication().getName())
                .description("Failed login attempt")
                .ipAddress(getClientIp())
                .build());
    }

    @EventListener
    public void onAccessDenied(AccessDeniedEvent event) {
        auditService.logAsync(AuditLog.builder()
                .eventType(AuditEventType.AUTHORIZATION)
                .action("ACCESS_DENIED")
                .description(event.getAccessDeniedException().getMessage())
                .ipAddress(getClientIp())
                .build());
    }
}
```

**API Endpoints:**

- `GET /api/audit` - Search audit logs with filters
- `GET /api/audit/export` - Export audit logs (CSV/Excel)
- Query params: `?userId={id}&eventType={type}&from={date}&to={date}&page=0&size=50`

**Database Schema:**

```sql
CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY,
    timestamp   TIMESTAMP    NOT NULL,
    user_id     UUID,
    username    VARCHAR(255),
    event_type  VARCHAR(50)  NOT NULL,
    action      VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   UUID,
    description TEXT,
    ip_address  VARCHAR(45),
    old_value   TEXT,
    new_value   TEXT
);

CREATE INDEX idx_audit_timestamp ON audit_logs (timestamp DESC);
CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_event_type ON audit_logs (event_type);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);

-- Consider table partitioning by month for large volumes
```

**Retention Policy:**

```java

@Scheduled(cron = "0 0 2 1 * ?") // First day of month at 2 AM
public void cleanupOldAuditLogs() {
    LocalDateTime retentionDate = LocalDateTime.now().minusYears(1);
    int deleted = auditLogRepository.deleteByTimestampBefore(retentionDate);
    log.info("Deleted {} old audit logs", deleted);
}
```

## References

- User Story File: [docs/tasks/us/US-AD-005/us-ad-005.md](../../../docs/tasks/us/US-AD-005/us-ad-005.md)
- Dependencies: US-AD-001 (user management)
- Security best practice for compliance
