# [US-TR-002] - Расчет стоимости аренды (Calculate Rental Cost)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** tariff  
**Dependencies:** US-TR-001, US-RN-007

## Original Request

**Как** Система  
**Я хочу** автоматически рассчитывать стоимость аренды  
**Чтобы** определить итоговую сумму к оплате с учетом фактического времени

## User Story Details

**Описание:**  
Система должна автоматически рассчитывать стоимость аренды на основе тарифа и фактического времени.

**Бизнес-правило:**

- Расчет с кратностью 5 минут
- Если фактическое время <= запланированного + 7 минут — стоимость не меняется
- Если превышение > 7 минут — начисляется доплата

**Критерии приемки:**

- Автоматический расчет при возврате
- Отображение детализации расчета
- Правильное применение правила "прощения"

**Связанные требования:** FR-TR-002

## Thought Process

Rental cost calculation is the core billing logic. Must integrate multiple business rules: time rounding, forgiveness
threshold, overtime charges.

**Key Components:**

1. **Base Cost**: Tariff base price for selected period
2. **Duration Calculation**: Uses US-RN-007 for billable minutes
3. **Forgiveness Rule**: US-TR-003 - up to 7 minutes forgiven
4. **Overtime Calculation**: US-TR-004 - charges for excess time
5. **Configuration**: Uses business rules from US-AD-004

**Calculation Algorithm:**

```
1. Get actual duration (from US-RN-007)
2. Calculate billable minutes (rounded to 5-minute increments)
3. Compare with planned duration
4. If within forgiveness threshold (7 min) → base cost only
5. If exceeded → base cost + overtime cost
6. Return detailed breakdown
```

## Implementation Plan

- [ ] Create RentalCostCalculator service
- [ ] Implement cost calculation algorithm
- [ ] Integrate with forgiveness rule (US-TR-003)
- [ ] Integrate with overtime calculation (US-TR-004)
- [ ] Create cost breakdown DTO
- [ ] Add unit tests for all scenarios
- [ ] Document calculation examples
- [ ] Create integration tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 2.1 | Create calculator service  | Not Started | 2026-01-26 |       |
| 2.2 | Implement algorithm        | Not Started | 2026-01-26 |       |
| 2.3 | Integrate forgiveness rule | Not Started | 2026-01-26 |       |
| 2.4 | Create cost breakdown      | Not Started | 2026-01-26 |       |
| 2.5 | Create tests               | Not Started | 2026-01-26 |       |

## Technical Details

**Cost Calculator:**

```java

@Service
public class RentalCostCalculator {

    private final BusinessRulesConfig config;
    private final RentalDurationCalculator durationCalculator;

    public CostBreakdown calculate(Rental rental, LocalDateTime returnTime) {
        // Calculate billable time
        int actualMinutes = ChronoUnit.MINUTES.between(
                rental.getStartedAt(), returnTime
        );
        int billableMinutes = durationCalculator.calculateBillableMinutes(
                rental.getStartedAt(), returnTime
        );

        // Check forgiveness threshold
        int plannedMinutes = rental.getPlannedDurationMinutes();
        int overtimeMinutes = actualMinutes - plannedMinutes;

        Money baseCost = rental.getTariff().getBasePrice();
        Money overtimeCost = Money.ZERO;

        if (overtimeMinutes <= config.getForgivenessThresholdMinutes()) {
            // Within forgiveness - no extra charge
            return new CostBreakdown(baseCost, overtimeCost, "Forgiven");
        } else {
            // Calculate overtime
            overtimeCost = calculateOvertimeCost(rental, overtimeMinutes);
            return new CostBreakdown(baseCost, overtimeCost, "Overtime");
        }
    }
}
```

## References

- User Story File: [docs/tasks/us/US-TR-002/us-tr-002.md](../../../docs/tasks/us/US-TR-002/us-tr-002.md)
- Dependencies: US-TR-001, US-RN-007
- Integrates: US-TR-003, US-TR-004
