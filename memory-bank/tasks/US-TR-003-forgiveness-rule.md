# [US-TR-003] - Правило "прощения" просрочки (Forgiveness Rule for Late Returns)

- Used by: US-TR-002
- User Story File: [docs/tasks/us/US-TR-003/us-tr-003.md](../../../docs/tasks/us/US-TR-003/us-tr-003.md)

## References

```
) {}
    String message
    int overtimeMinutes,
    boolean forgivenessApplied,
    Money totalCost,
    Money overtimeCost,
    Money baseCost,
public record CostBreakdown(
```java
**Cost Breakdown Extension:**

```

}
return overtimeMinutes > 0 && overtimeMinutes <= threshold;
int threshold = config.getForgivenessThresholdMinutes();
public boolean isForgivenessApplicable(int overtimeMinutes) {

```java
**Forgiveness Check:**

## Technical Details

| 3.3 | Create tests                  | Not Started | 2026-01-26 |       |
| 3.2 | Add audit logging             | Not Started | 2026-01-26 |       |
| 3.1 | Implement forgiveness logic   | Not Started | 2026-01-26 |       |
|-----|-------------------------------|-------------|------------|-------|
| ID  | Description                   | Status      | Updated    | Notes |

### Subtasks

**Overall Status:** Not Started - 0%

## Progress Tracking

- [ ] Create unit tests
- [ ] Add reporting metrics
- [ ] Display "Forgiveness applied" message
- [ ] Add audit log for forgiven overtimes
- [ ] Create forgiveness indicator in breakdown
- [ ] Add forgiveness logic to cost calculator

## Implementation Plan

- Record forgiveness in rental history
- If difference <= threshold → no extra charge
- Check actual time vs planned time
- Part of RentalCostCalculator
**Implementation:**

- Must be logged for reporting and analytics
- Applies automatically during cost calculation
- Threshold: 7 minutes (default, configurable via US-AD-004)
**Configuration:**

Forgiveness rule improves customer satisfaction by not penalizing minor delays. This is a configurable business rule.

## Thought Process

**Связанные требования:** FR-TR-003

- Сохранение информации о просрочке в истории
- Отображение сообщения "Просрочка прощена"
- Автоматическое применение правила
- Просрочка отображается, но не влияет на стоимость
- Клиент платит только оплаченную сумму
- Просрочка до 7 минут включительно не тарифицируется
**Критерии приемки:**

Система должна применять правило "прощения" для небольших просрочек.
**Описание:**  

## User Story Details

**Чтобы** не наказывать клиентов за незначительные задержки
**Я хочу** применять правило "прощения" для небольших просрочек  
**Как** Система  

## Original Request

**Dependencies:** US-TR-002
**Module:** tariff  
**Priority:** High  
**Updated:** 2026-01-26  
**Added:** 2026-01-26  
**Status:** Pending  

