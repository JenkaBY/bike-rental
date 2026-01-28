# [US-TR-004] - Расчет доплаты за просрочку (Calculate Overtime Charge)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** tariff  
**Dependencies:** US-TR-002, US-TR-003

## Original Request

**Как** Система  
**Я хочу** рассчитывать доплату при превышении времени аренды  
**Чтобы** справедливо компенсировать дополнительное время использования

## User Story Details

**Описание:**  
Система должна рассчитывать доплату при превышении времени аренды более чем на 7 минут.

**Критерии приемки:**

- Просрочка > 7 минут округляется до 10 минут
- Далее просрочка рассчитывается с кратностью 5 минут
- Применяется тариф доп. времени из справочника
- Правильное округление просрочки
- Детализированный расчет
- Отображение клиенту

**Связанные требования:** FR-TR-004

## Thought Process

Overtime calculation applies after forgiveness threshold is exceeded. Requires special rounding rules.

**Rounding Rules:**

1. If overtime 8-10 minutes → round to 10 minutes
2. Then apply 5-minute increment rounding
3. Use extraTimePricePer5Min from tariff

**Examples:**

- 8 min overtime → 10 min → charge for 10 min
- 12 min overtime → 15 min → charge for 15 min
- 23 min overtime → 25 min → charge for 25 min

## Implementation Plan

- [ ] Create overtime calculator
- [ ] Implement rounding logic
- [ ] Get extra time price from tariff
- [ ] Calculate overtime cost
- [ ] Add unit tests for all scenarios
- [ ] Document examples

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                   | Status      | Updated    | Notes |
|-----|-------------------------------|-------------|------------|-------|
| 4.1 | Implement overtime calculator | Not Started | 2026-01-26 |       |
| 4.2 | Create rounding logic         | Not Started | 2026-01-26 |       |
| 4.3 | Create tests                  | Not Started | 2026-01-26 |       |

## Technical Details

**Overtime Calculator:**

```java
public Money calculateOvertimeCost(Rental rental, int overtimeMinutes) {
    int forgiveness = config.getForgivenessThresholdMinutes();

    if (overtimeMinutes <= forgiveness) {
        return Money.ZERO;
    }

    // Overtime beyond forgiveness
    int chargeableOvertime = overtimeMinutes - forgiveness;

    // Round to 10 minutes if 1-10
    if (chargeableOvertime <= 10) {
        chargeableOvertime = 10;
    } else {
        // Round to nearest 5 minutes
        int increment = config.getTimeIncrementMinutes();
        chargeableOvertime = ((chargeableOvertime + increment - 1) / increment) * increment;
    }

    // Calculate cost
    int intervals = chargeableOvertime / 5; // per 5-minute intervals
    Money pricePerInterval = rental.getTariff().getExtraTimePricePer5Min();

    return pricePerInterval.multiply(intervals);
}
```

## References

- User Story File: [docs/tasks/us/US-TR-004/us-tr-004.md](../../../docs/tasks/us/US-TR-004/us-tr-004.md)
- Used by: US-TR-002
