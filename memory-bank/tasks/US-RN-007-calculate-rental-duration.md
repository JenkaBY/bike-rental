# [US-RN-007] - Расчет времени аренды (Calculate Rental Duration)

- Used by: US-TR-002 (cost calculation)
- User Story File: [docs/tasks/us/US-RN-007/us-rn-007.md](../../../docs/tasks/us/US-RN-007/us-rn-007.md)

## References

- 120 minutes actual → 120 minutes billable (exact)
- 61 minutes actual → 65 minutes billable
- 23 minutes actual → 25 minutes billable (5-minute increment)
  **Examples:**

```
}
    }
        return (int) ((actualMinutes + increment - 1) / increment) * increment;
        // Round up to nearest increment
        
        int increment = config.getTimeIncrementMinutes();
        long actualMinutes = ChronoUnit.MINUTES.between(start, end);
    public int calculateBillableMinutes(LocalDateTime start, LocalDateTime end) {
    
    private final BusinessRulesConfig config;
    
public class RentalDurationCalculator {
@Service
```java
**Duration Calculator:**

## Technical Details

- Part of Phase 3: Main Rental Process
- Task created in Memory Bank structure
- Updated 2026-02-16: Dependency changed from US-RN-003 to US-RN-005 (startTime устанавливается автоматически при активации)

### 2026-02-18
- **COMPLETED**: Implementation finished
  - Created RentalDurationCalculator port interface in domain.service (Dependency Inversion pattern)
  - Created RentalDurationCalculatorImpl implementation in application.service
  - Created RentalDurationResult interface and BaseRentalDurationResult record in domain.service
  - RentalDurationResult with actualMinutes() (default method computed from actualDuration), billableMinutes(), actualDuration() methods
  - BaseRentalDurationResult record with 2 fields: billableMinutes and actualDuration (actualMinutes computed from actualDuration)
  - Created RentalProperties with @ConfigurationProperties(prefix = "app.rental")
  - Added app.rental.time-increment: 5m to application.yaml
  - Added calculateActualDuration() method to Rental entity (uses domain port interface)
  - Comprehensive parameterized unit tests using @ParameterizedTest with @ValueSource, @CsvSource, @MethodSource
  - Formula: (actualMinutes + increment - 1) / increment * increment for rounding up
  - Supports durations from 0 minutes to multiple days
  - Prepared structure for future app.rental.forgiveness.overtime-duration property
  - **Architecture**: Domain layer defines port, application provides implementation (follows StatusTransitionPolicy pattern)

### 2026-02-16
- Dependency updated: US-RN-003 → US-RN-005 (startTime устанавливается автоматически при активации аренды через US-RN-005)

### 2026-01-26

## Progress Log

| 7.4 | Create tests                  | Completed   | 2026-02-18 | Comprehensive unit tests with all edge cases |
| 7.3 | Add configuration support     | Completed   | 2026-02-18 | RentalProperties with application.yaml |
| 7.2 | Implement rounding logic      | Completed   | 2026-02-18 | Rounding formula implemented |
| 7.1 | Create duration calculator    | Completed   | 2026-02-18 | RentalDurationCalculator port (domain) + RentalDurationCalculatorImpl (application) with RentalDurationResult |
|-----|-------------------------------|-------------|------------|-------|
| ID  | Description                   | Status      | Updated    | Notes |

### Subtasks

**Overall Status:** Completed - 100%

## Progress Tracking

- [x] Document calculation examples
- [x] Add unit tests for rounding scenarios
- [x] Create value object for Duration (RentalDurationResult interface + BaseRentalDurationResult record in domain.service)
- [x] Add configuration support (RentalProperties with @ConfigurationProperties)
- [x] Implement rounding logic
- [x] Create RentalDurationCalculator port interface in domain.service
- [x] Create RentalDurationCalculatorImpl implementation in application.service

## Implementation Plan

- Default: 5 minutes
- Time increment configurable via business rules (US-AD-004)
**Configuration:**

- Example: 23 minutes → 25 minutes, 61 minutes → 65 minutes
- Round up to nearest 5-minute increment
- Calculate exact duration between start and return
**Business Rules:**

Duration calculation is critical for cost calculation. Must follow business rules for rounding.

## Thought Process

**Связанные требования:** FR-RN-007

- Отображение в понятном формате (часы, минуты)
- Округление вверх (например, 23 минуты = 25 минут)
- Расчет времени с кратностью 5 минут
- Точный расчет времени между началом и возвратом
**Критерии приемки:**

Система должна автоматически рассчитывать фактическое время аренды.
**Описание:**  

## User Story Details

**Чтобы** корректно определить стоимость аренды
**Я хочу** автоматически рассчитывать фактическое время аренды  
**Как** Система  

## Original Request

**Dependencies:** US-RN-005 (startTime устанавливается автоматически при активации аренды)
**Module:** rental  
**Priority:** High  
**Updated:** 2026-02-18  
**Added:** 2026-01-26  
**Status:** Completed  

