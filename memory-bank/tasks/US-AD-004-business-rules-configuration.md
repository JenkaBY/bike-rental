# [US-AD-004] - Настройка бизнес-правил (Business Rules Configuration)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** admin  
**Dependencies:** US-AD-001

## Original Request

**Как** Администратор  
**Я хочу** иметь возможность настраивать бизнес-параметры системы  
**Чтобы** адаптировать поведение приложения без перекомпиляции/деплоя

## User Story Details

Система должна позволять администраторам управлять глобальными системными настройками, которые влияют на:

- расчеты стоимости аренды (округления, пороги, шаги расчета времени),
- правила возврата и таймауты платежей,
- интервалы обслуживания для разных типов оборудования.

Ключевые требования:

- типобезопасный доступ к настройкам (int/decimal/boolean/string),
- проверка диапазонов и валидация значений при обновлении,
- история изменений (audit) с информацией кто и когда изменил значение,
- кэширование настроек для производительности и инвалидирование при обновлении,
- API для получения и изменения значений (admin-only).

## Acceptance Criteria

- CRUD для системных настроек доступен только администраторам
- Значения имеют типы и валидируются при сохранении
- Изменения записываются в историю с updatedBy/updatedAt
- Новые значения применяются к новым операциям (hot reload via cache invalidation)
- Набор настроек включает: payment timeout, refund window, overtime rounding, forgiveness threshold, time increment

## Thought Process

Business rules must be runtime-configurable to allow quick changes without code deploys. Settings are global (not
tenant-aware) in this version. Use a strongly-typed enum of keys (`SettingKey`) with default/min/max values and a
`SystemSettingsService` that exposes typed getters and an update method with validation and cache eviction.

Design considerations:

- store as key-value in DB with metadata (type, min, max, description)
- expose typed getters: `getSetting(SettingKey, Class<T>)` or domain wrapper `BusinessRulesConfig`
- cache settings and evict on update
- keep changes applied only to new rentals (no retroactive effect)

## Implementation Plan

- [ ] Create `SettingKey` enum with key, default, min, max, type metadata
- [ ] Implement `SystemSetting` JPA entity and `SystemSettingsRepository`
- [ ] Implement `SystemSettingsService` with typed getters and `updateSetting()` with validation and cache eviction
- [ ] Create `SettingsController` (admin-only) with endpoints:
    - GET `/api/admin/settings` - list settings
    - GET `/api/admin/settings/{key}` - get specific setting
    - PUT `/api/admin/settings/{key}` - update setting
    - GET `/api/admin/settings/{key}/history` - change history
- [ ] Implement `SystemSettingsHistory` for audit records
- [ ] Add component tests (admin flows) and unit tests (validation, typed getters)
- [ ] Add Liquibase migration for `system_settings` and `system_settings_history`
- [ ] Document config keys and defaults in memory bank

## Subtasks

| ID  | Description                     | Status      | Updated    | Notes                                 |
|-----|---------------------------------|-------------|------------|---------------------------------------|
| 4.1 | Create settings domain model    | Not Started | 2026-01-26 | SettingKey enum, SystemSetting entity |
| 4.2 | Implement configuration service | Not Started | 2026-01-26 | typed getters + cache                 |
| 4.3 | Create admin endpoints          | Not Started | 2026-01-26 | SettingsController (admin-only)       |
| 4.4 | Add caching mechanism           | Not Started | 2026-01-26 | Spring Cache + eviction on update     |
| 4.5 | Implement validation            | Not Started | 2026-01-26 | range/type checks                     |
| 4.6 | Create audit trail              | Not Started | 2026-01-26 | system_settings_history table         |
| 4.7 | Create tests                    | Not Started | 2026-01-26 | unit, WebMvc, component               |

**Overall Status:** Not Started - 0%

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure. Status: Pending.

## Technical Details

### Example - `SettingKey` enum (sketch)

```java
public enum SettingKey {
    MAINTENANCE_INTERVAL_SCOOTER_HOURS("maintenance.interval.scooter", 50, 10, 500, Integer.class),
    MAINTENANCE_INTERVAL_BICYCLE_HOURS("maintenance.interval.bicycle", 100, 10, 1000, Integer.class),

    PAYMENT_TIMEOUT_MINUTES("finance.payment.timeout", 15, 5, 120, Integer.class),
    REFUND_WINDOW_MINUTES("finance.refund.window", 10, 1, 60, Integer.class),

    OVERTIME_ROUNDING_MINUTES("rental.overtime.rounding", 10, 5, 60, Integer.class),
    FORGIVENESS_THRESHOLD_MINUTES("rental.forgiveness.threshold", 7, 0, 30, Integer.class),
    TIME_CALCULATION_INCREMENT_MINUTES("rental.time.increment", 5, 1, 60, Integer.class);

    private final String key;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    private final Class<?> type;

    // constructor + getters
}
```

### Example - `SystemSettingsService` API (sketch)

```java
@Component
public class SystemSettingsService {

    private final SystemSettingsRepository repository;
    private final CacheManager cacheManager;

    @Cacheable(value = "system-settings", key = "#key")
    public <T> T getSetting(SettingKey key, Class<T> type) {
        return repository.findById(key.getKey())
                .map(setting -> convertValue(setting.getValue(), type))
                .orElse((T) key.getDefaultValue());
    }

    @CacheEvict(value = "system-settings", key = "#key")
    public void updateSetting(SettingKey key, Object newValue, UUID updatedBy) {
        validateValue(key, newValue);
        var setting = repository.findById(key.getKey()).orElse(new SystemSetting(key.getKey()));
        String oldValue = setting.getValue();
        setting.setValue(convertToString(newValue));
        setting.setUpdatedBy(updatedBy);
        setting.setUpdatedAt(LocalDateTime.now());
        repository.save(setting);
        saveHistory(key.getKey(), oldValue, setting.getValue(), updatedBy);
    }

    // helper methods: convertValue, convertToString, validateValue, saveHistory
}
```

### Database schema (example)

```sql
CREATE TABLE system_settings (
  key VARCHAR(100) PRIMARY KEY,
  value TEXT NOT NULL,
  value_type VARCHAR(20) NOT NULL,
  description TEXT,
  min_value TEXT,
  max_value TEXT,
  default_value TEXT,
  updated_by UUID, -- references app_users(id)
  updated_at TIMESTAMP
);

CREATE TABLE system_settings_history (
  id UUID PRIMARY KEY,
  setting_key VARCHAR(100) NOT NULL,
  old_value TEXT,
  new_value TEXT,
  changed_at TIMESTAMP NOT NULL,
  changed_by UUID -- references app_users(id)
);

CREATE INDEX idx_settings_history_key ON system_settings_history(setting_key, changed_at DESC);
```

## API Endpoints (admin only)

- `GET /api/admin/settings` - list all settings
- `GET /api/admin/settings/{key}` - get specific setting
- `PUT /api/admin/settings/{key}` - update setting value
- `GET /api/admin/settings/{key}/history` - get change history

## Notes

- Settings should include sensible defaults so the system works without explicit configuration.
- Cache invalidation on update is required to apply changes immediately to new operations.
- For critical settings that affect monetary calculations, add additional review/approval workflow if needed.

## References

- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- User Story File: [docs/tasks/us/US-AD-004/us-ad-004.md](../../../docs/tasks/us/US-AD-004/us-ad-004.md)
- Dependency: US-AD-001 (User management for admin access)
