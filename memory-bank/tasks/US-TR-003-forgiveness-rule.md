# [US-TR-003] - Правило "прощения" просрочки (Forgiveness Rule for Late Returns)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-25  
**Completed:** 2026-02-25  
**Priority:** High  
**Module:** tariff  
**Dependencies:** US-TR-002  
**Used by:** US-TR-002

## Original Request

**Как** Система  
**Я хочу** применять правило "прощения" для небольших просрочек  
**Чтобы** не наказывать клиентов за незначительные задержки

## User Story Details

**Описание:**  
Система должна применять правило "прощения" для небольших просрочек.

**Критерии приемки:**

- Просрочка до 7 минут включительно не тарифицируется
- Клиент платит только оплаченную сумму
- Просрочка отображается, но не влияет на стоимость
- Автоматическое применение правила
- Отображение сообщения "Просрочка прощена"
- Сохранение информации о просрочке в истории

**Связанные требования:** FR-TR-003  
**User Story File:** [docs/tasks/us/US-TR-003/us-tr-003.md](../../../docs/tasks/us/US-TR-003/us-tr-003.md)

## Thought Process

Forgiveness rule improves customer satisfaction by not penalizing minor delays. This is a configurable business rule
with localized messages.

**Key Considerations:**

1. **Integration with US-TR-002**: Forgiveness logic was initially implemented as part of US-TR-002 (Calculate Rental
   Cost) using Strategy Pattern
2. **Localization**: Messages should be stored in properties files for easy maintenance and future i18n support
3. **Configuration**: Default locale should be configurable via application properties
4. **Message Service**: Created MessageService to encapsulate MessageSource access and simplify usage across the
   application
5. **AcceptHeaderLocaleResolver**: Uses HTTP Accept-Language header to determine locale, with fallback to configured
   default

**Deferred Items:**

- Audit logging deferred to US-AD-005 (Audit Log)
- Rental history recording deferred to US-RN-006 (Equipment Return) or US-FN-003 (Financial History)
- Reporting metrics deferred to future task

## Implementation Plan

- [x] Implement forgiveness logic (Completed in US-TR-002)
- [x] Create MessageSource configuration
- [x] Create properties files for messages (messages.properties, messages_ru.properties)
- [x] Create MessageService interface and implementation
- [x] Update ThresholdForgivenessStrategy to use MessageService
- [x] Configure AcceptHeaderLocaleResolver
- [x] Add default locale configuration via AppProperties
- [x] Update unit tests with localized messages
- [ ] Add audit logging (Deferred to US-AD-005)
- [ ] Add reporting metrics (Deferred to future task)

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                         | Status    | Updated    | Notes                                                     |
|-----|-------------------------------------|-----------|------------|-----------------------------------------------------------|
| 3.1 | Implement forgiveness logic         | Completed | 2026-02-24 | Implemented in US-TR-002 via ThresholdForgivenessStrategy |
| 3.2 | Create MessageSource configuration  | Completed | 2026-02-25 | MessageSourceConfig with AcceptHeaderLocaleResolver       |
| 3.3 | Create properties files             | Completed | 2026-02-25 | messages.properties (EN) and messages_ru.properties (RU)  |
| 3.4 | Create MessageService               | Completed | 2026-02-25 | MessageService interface and MessageServiceImpl           |
| 3.5 | Update ThresholdForgivenessStrategy | Completed | 2026-02-25 | Uses MessageService instead of direct MessageSource       |
| 3.6 | Configure default locale            | Completed | 2026-02-25 | AppProperties with app.default-locale: en                 |
| 3.7 | Update unit tests                   | Completed | 2026-02-25 | All tests updated with Russian messages                   |
| 3.8 | Add audit logging                   | Deferred  | 2026-02-25 | Deferred to US-AD-005 (Audit Log)                         |
| 3.9 | Add reporting metrics               | Deferred  | 2026-02-25 | Deferred to future task                                   |

## Progress Log

### 2026-02-25

- ✅ **Completed**: Full localization implementation with MessageSource and properties files
- ✅ Created `MessageSourceConfig` with `AcceptHeaderLocaleResolver` for HTTP header-based locale resolution
- ✅ Created properties files: `messages.properties` (English fallback) and `messages_ru.properties` (Russian)
- ✅ Created `MessageService` interface and `MessageServiceImpl` to encapsulate MessageSource access
- ✅ Updated `ThresholdForgivenessStrategy` to use `MessageService` instead of direct `MessageSource` access
- ✅ Created `AppProperties` with `app.default-locale: en` configuration property
- ✅ Created `defaultLocale` bean in `MessageSourceConfig` that reads from `AppProperties`
- ✅ Updated `MessageServiceImpl` to use `AppProperties` for default locale
- ✅ All unit tests updated with Russian messages, all 17 tests passing
- ✅ Properties files saved in UTF-8 encoding for proper Cyrillic support
- ✅ Default locale configurable via `app.default-locale` property in `application.yaml`

**Implementation Highlights:**

- **MessageService Pattern**: Encapsulates MessageSource access, simplifies usage, improves testability
- **AcceptHeaderLocaleResolver**: Determines locale from HTTP `Accept-Language` header with fallback to configured
  default
- **Configuration-Driven**: Default locale configurable via `app.default-locale` property
- **UTF-8 Encoding**: Properties files properly encoded for Cyrillic characters
- **Strategy Pattern**: Forgiveness logic uses MessageService for localized messages

### 2026-02-24

- ✅ Forgiveness logic implemented as part of US-TR-002
- ✅ `ThresholdForgivenessStrategy` created with 7 minutes threshold
- ✅ Integrated into `CalculateRentalCostService` via Strategy Pattern

## Technical Details

**Implementation:**

- **Forgiveness Logic**: Implemented in `ThresholdForgivenessStrategy` (completed in US-TR-002)
- **Integration**: Integrated into `CalculateRentalCostService` via Strategy Pattern
- **Localization**: Messages stored in properties files via Spring MessageSource
- **MessageService**: `MessageService` interface and `MessageServiceImpl` encapsulate MessageSource access
- **Locale Resolution**: `AcceptHeaderLocaleResolver` determines locale from HTTP `Accept-Language` header
- **Default Locale**: Configurable via `app.default-locale` property (default: `en`)

**Configuration:**

- Threshold: 7 minutes (configurable via `app.rental.forgiveness.overtime-duration`)
- Default locale: English (`en`) - configurable via `app.default-locale`
- Supported locales: `ru`, `en`
- Messages stored in properties files for easy maintenance and future i18n support

**Message Keys:**

- `forgiveness.message.on-time` - "Возврат вовремя или досрочно" (RU) / "On time or early return" (EN)
- `forgiveness.message.forgiven` - "Просрочка прощена ({0} минут просрочки)" (RU) / "Forgiven ({0} minutes overtime)" (
  EN)

**Files Created:**

- `shared/config/MessageSourceConfig.java` - MessageSource and LocaleResolver configuration
- `shared/config/AppProperties.java` - Application properties with default locale
- `shared/application/service/MessageService.java` - MessageService interface
- `shared/application/service/MessageServiceImpl.java` - MessageService implementation
- `resources/messages.properties` - English messages (fallback)
- `resources/messages_ru.properties` - Russian messages

**Files Modified:**

- `tariff/application/strategy/ThresholdForgivenessStrategy.java` - Uses MessageService instead of MessageSource
- `tariff/application/config/RentalCostCalculationConfig.java` - Updated to inject MessageService
- `application.yaml` - Added `app.default-locale: en` property
- `test/CalculateRentalCostServiceTest.java` - Updated with Russian messages

**Architecture:**

- Follows Service Pattern: MessageService encapsulates MessageSource access
- Uses AcceptHeaderLocaleResolver for HTTP-based locale resolution
- Configuration-driven via AppProperties
- UTF-8 encoding for properties files
- Fallback to default locale when HTTP context unavailable  

