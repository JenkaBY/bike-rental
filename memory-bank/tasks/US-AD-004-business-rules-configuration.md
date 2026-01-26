# [US-AD-004] - Настройка бизнес-правил (Business Rules Configuration)

- Affects: US-RN-008, US-TR-005 (refund windows use these settings)
- Affects: US-TR-002, US-TR-003, US-TR-004 (tariff calculation uses these settings)
- Dependency: US-AD-001 (User management for admin access)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- User Story File: [docs/tasks/us/US-AD-004/us-ad-004.md](../../../docs/tasks/us/US-AD-004/us-ad-004.md)

## References

None yet - task not started

## Known Issues

```
}
    }
        // Use configured values in calculation
        
        int rounding = config.getOvertimeRoundingMinutes();
        int forgiveness = config.getForgivenessThresholdMinutes();
        int increment = config.getTimeIncrementMinutes();
    public Money calculateCost(Rental rental) {
    
    private final BusinessRulesConfig config;
    
public class RentalCostCalculator {
@Service
```java
**Usage in Rental Calculation:**

```

}
// ... other typed getters

    }
        );
            Integer.class
            SettingKey.FORGIVENESS_THRESHOLD_MINUTES, 
        return settingsService.getSetting(
    public int getForgivenessThresholdMinutes() {
    
    }
        );
            Integer.class
            SettingKey.TIME_CALCULATION_INCREMENT_MINUTES, 
        return settingsService.getSetting(
    public int getTimeIncrementMinutes() {
    
    private final SystemSettingsService settingsService;

public class BusinessRulesConfig {
@Component

```java
**Strongly-
Typed Access:**

```

}
}
// Business logic validation
// Range validation (min/max)
// Type validation
private void validateValue(SettingKey key, Object value) {

    }
        saveHistory(key.getKey(), oldValue, setting.getValue(), updatedBy);
        // Save history
        
        repository.save(setting);
        
        setting.setUpdatedBy(updatedBy);
        setting.setUpdatedAt(LocalDateTime.now());
        setting.setValue(convertToString(newValue));
        String oldValue = setting.getValue();
        
            .orElse(new SystemSetting(key.getKey()));
        SystemSetting setting = repository.findById(key.getKey())
        
        validateValue(key, newValue);
    public void updateSetting(SettingKey key, Object newValue, UUID updatedBy) {
    @CacheEvict(value = "system-settings", key = "#key")
    
    }
            .orElse((T) key.getDefaultValue());
            .map(setting -> convertValue(setting.getValue(), type))
        return repository.findById(key.getKey())
    public <T> T getSetting(SettingKey key, Class<T> type) {
    @Cacheable(value = "system-settings", key = "#key")
    
    private final CacheManager cacheManager;
    private final SystemSettingsRepository repository;

public class SystemSettingsService {
@Service

```java
**Configuration Service:**

```

}
private final Object maxValue;
private final Object minValue;
private final Object defaultValue;
private final String key;

    MAINTENANCE_INTERVAL_SCOOTER_HOURS("maintenance.interval.scooter", 50, 10, 500);
    MAINTENANCE_INTERVAL_BICYCLE_HOURS("maintenance.interval.bicycle", 100, 10, 1000),
    // Maintenance
    
    PAYMENT_TIMEOUT_MINUTES("finance.payment.timeout", 15, 5, 120),
    REFUND_WINDOW_MINUTES("finance.refund.window", 10, 1, 60),
    // Financial
    
    OVERTIME_ROUNDING_MINUTES("rental.overtime.rounding", 10, 5, 60),
    FORGIVENESS_THRESHOLD_MINUTES("rental.forgiveness.threshold", 7, 0, 30),
    TIME_CALCULATION_INCREMENT_MINUTES("rental.time.increment", 5, 1, 60),
    // Rental timing

public enum SettingKey {

```java
**Setting Keys
Enum:**

```

CREATE INDEX idx_settings_history_key ON system_settings_history(setting_key, changed_at DESC);

);
changed_by UUID REFERENCES app_users(id)
changed_at TIMESTAMP NOT NULL,
new_value TEXT,
old_value TEXT,
setting_key VARCHAR(100) NOT NULL,
id UUID PRIMARY KEY,
CREATE TABLE system_settings_history (

);
updated_by UUID REFERENCES app_users(id)
updated_at TIMESTAMP NOT NULL,
max_value TEXT, -- For validation
min_value TEXT, -- For validation
description TEXT,
value_type VARCHAR(20) NOT NULL, -- INTEGER, DECIMAL, STRING, BOOLEAN
value TEXT NOT NULL,
key VARCHAR(100) PRIMARY KEY,
CREATE TABLE system_settings (

```sql
*
*Database Schema:**

- `GET /api/admin/settings/{key}/history` - Get change history (ADMIN only)
- `PUT /api/admin/settings/{key}` -
Update setting (ADMIN only)
    - `GET /api/admin/settings/{key}` - Get specific setting (ADMIN only)
    - `GET /api/admin/settings` - Get all settings (ADMIN only)
    **API Endpoints:**

```

    └── cache (Spring Cache configuration)

└── infrastructure
│ └── repository.SystemSettingsRepository
│ ├── model.SettingKey (enum)
│ ├── model.SystemSetting
├── domain
│ └── config.BusinessRulesConfig
│ ├── service.SystemSettingsService
├── application
│ └── dto.SettingsResponse
│ ├── SettingsQueryController
├── web.query
│ └── dto.UpdateSettingRequest
│ ├── SettingsController
├── web.command
com.github.jenkaby.bikerental.admin

```
**Package Structure:**

## Technical Details

- Part of Phase 2: Basic Module Functions
- Status: Pending, depends on US-AD-001 completion
- Task created in Memory Bank structure
### 2026-01-26

## Progress Log

| 4.7 | Create tests                         | Not Started | 2026-01-26 |       |
| 4.6 | Create audit trail                   | Not Started | 2026-01-26 |       |
| 4.5 | Implement validation                 | Not Started | 2026-01-26 |       |
| 4.4 | Add caching mechanism                | Not Started | 2026-01-26 |       |
| 4.3 | Create admin endpoints               | Not Started | 2026-01-26 |       |
| 4.2 | Implement configuration service      | Not Started | 2026-01-26 |       |
| 4.1 | Create settings domain model         | Not Started | 2026-01-26 |       |
|-----|--------------------------------------|-------------|------------|-------|
| ID  | Description                          | Status      | Updated    | Notes |

### Subtasks

**Overall Status:** Not Started - 0%

## Progress Tracking

- [ ] Document all configurable parameters
- [ ] Write WebMvc tests for admin endpoints
- [ ] Write unit tests for validation
- [ ] Create component tests for settings
- [ ] Implement cache invalidation on update
- [ ] Add database migration for settings table
- [ ] Create settings history/audit mechanism
- [ ] Add validation for each setting type
- [ ] Implement admin endpoints for settings management
- [ ] Create typed configuration service with caching
- [ ] Implement settings repository
- [ ] Create SystemSettings domain model

## Implementation Plan

- Settings are global (not tenant-specific in this version)
- Invalidate cache on settings update
- Use caching to avoid database hits on every rental
- Store in database (not application.yaml) for runtime changes
**Architecture Decisions:**

- Consider @ConfigurationProperties for strongly-typed access
- Settings apply to new rentals (don't affect in-progress)
- Admin API to update settings
- Typed configuration service with caching
- Database table for system_settings (key-value pairs with metadata)
**Technical Approach:**

- **System**: General operational parameters
- **Maintenance**: Service intervals by equipment type
- **Financial**: Refund windows, payment timeouts
- **Rental Timing**: Time calculation, forgiveness, rounding
**Configuration Categories:**

6. **Defaults**: System should work with default values if not configured
5. **Audit Trail**: Track who changed what and when
4. **Hot Reload**: Settings changes should apply immediately (or with cache refresh)
3. **Validation**: Ensure values are within reasonable ranges
2. **Type Safety**: Each setting has a specific type (integer, decimal, boolean, etc.)
1. **Key-Value Store**: Store settings as configurable parameters

Business rules configuration allows customization without code changes. This is critical for business flexibility. Considerations:

## Thought Process

**Связанные требования:** FR-AD-004

- История изменений параметров
- Применение новых значений к новым арендам
- Валидация значений (положительные числа, разумные диапазоны)
- Интерфейс настроек
**Критерии приемки:**

- Периодичность ТО по умолчанию (для разных типов оборудования)
- Время для возврата денег при отмене (по умолчанию 10 минут)
- Округление просрочки (по умолчанию 10 минут)
- Порог "прощения" просрочки (по умолчанию 7 минут)
- Кратность расчета времени (по умолчанию 5 минут)
**Настраиваемые параметры:**

Система должна позволять настраивать основные бизнес-параметры.
**Описание:**  

## User Story Details

**Чтобы** адаптировать систему под бизнес-процессы
**Я хочу** настраивать основные бизнес-параметры  
**Как** Администратор  

## Original Request

**Dependencies:** US-AD-001
**Module:** admin  
**Priority:** Medium  
**Updated:** 2026-01-26  
**Added:** 2026-01-26  
**Status:** Pending  

