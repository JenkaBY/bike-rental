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
### 2026-01-26

## Progress Log

| 7.4 | Create tests                  | Not Started | 2026-01-26 |       |
| 7.3 | Add configuration support     | Not Started | 2026-01-26 |       |
| 7.2 | Implement rounding logic      | Not Started | 2026-01-26 |       |
| 7.1 | Create duration calculator    | Not Started | 2026-01-26 |       |
|-----|-------------------------------|-------------|------------|-------|
| ID  | Description                   | Status      | Updated    | Notes |

### Subtasks

**Overall Status:** Not Started - 0%

## Progress Tracking

- [ ] Document calculation examples
- [ ] Add unit tests for rounding scenarios
- [ ] Create value object for Duration
- [ ] Add configuration support
- [ ] Implement rounding logic
- [ ] Create RentalDurationCalculator service

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

**Dependencies:** US-RN-003
**Module:** rental  
**Priority:** High  
**Updated:** 2026-01-26  
**Added:** 2026-01-26  
**Status:** Pending  

