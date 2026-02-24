# [US-TR-002] - Расчет стоимости аренды (Calculate Rental Cost)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-24  
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

- [x] Create RentalCostCalculator service
- [x] Implement cost calculation algorithm
- [x] Integrate with forgiveness rule (7 minutes threshold)
- [x] Implement proportional overtime calculation based on period price
- [x] Create cost breakdown DTO (RentalCostResult)
- [x] Add unit tests for all scenarios
- [x] Integrate with TariffFacade
- [x] Create component test feature file

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                 | Status    | Updated    | Notes                                                |
|-----|-----------------------------|-----------|------------|------------------------------------------------------|
| 2.1 | Create calculator service   | Completed | 2026-02-24 | RentalCostCalculator port + RentalCostCalculatorImpl |
| 2.2 | Implement algorithm         | Completed | 2026-02-24 | Proportional calculation based on period price       |
| 2.3 | Integrate forgiveness rule  | Completed | 2026-02-24 | 7 minutes threshold via RentalProperties             |
| 2.4 | Create cost breakdown       | Completed | 2026-02-24 | RentalCostResult + BaseRentalCostResult              |
| 2.5 | Create tests                | Completed | 2026-02-24 | Comprehensive unit tests + component test feature    |
| 2.6 | Integrate with TariffFacade | Completed | 2026-02-24 | Added calculateFinalCost() method                    |

## Technical Details

**Implementation:**

- **Use Case Pattern**: `CalculateRentalCostUseCase` interface in `tariff.application.usecase`
- **Application Layer**: `CalculateRentalCostService` implementation in `tariff.application.service`
- **Strategy Pattern**:
   - `ForgivenessStrategy` interface with `ThresholdForgivenessStrategy` implementation
   - `OvertimeCalculationStrategy` interface with `ProportionalOvertimeCalculationStrategy` implementation
   - Configuration via `RentalCostCalculationConfig` bean factory
- **Result Objects**: `RentalCost` interface (public API in module root) with `BaseRentalCostResult` record (domain
  service)
- **Command Object**: `CalculateRentalCostCommand` with `Duration actualDuration`, `int billableMinutes`,
  `Duration plannedDuration`
- **Integration**: `TariffFacade.calculateFinalCost()` method accepts primitive types to avoid cyclic dependencies

**Key Features:**

1. **Base Cost Calculation**: Uses `TariffPeriodSelector` to determine period (HALF_HOUR/HOUR/DAY) based on **actual
   duration** (not planned), then uses `Tariff.getPriceForPeriod()` to get base price
2. **Forgiveness Rule**: Configurable via `RentalProperties.ForgivenessProperties` (default: 7 minutes threshold)
   implemented via `ThresholdForgivenessStrategy`
3. **Proportional Overtime**: Calculates overtime cost proportionally based on period base price via
   `ProportionalOvertimeCalculationStrategy`:
   - Price per 5 minutes = `basePrice / period_minutes * 5`
   - Chargeable overtime = `overtimeMinutes - forgivenMinutes`
   - Number of intervals = `ceil(chargeableOvertime / roundingInterval)` (uses `app.rental.time-increment`)
   - Overtime cost = `pricePer5Min * intervals` (rounded via `Money.of()`)
4. **Strategy Pattern**: Extensible design allows swapping forgiveness and overtime calculation strategies without
   modifying core service
5. **Money Encapsulation**: `Money.of(BigDecimal)` automatically rounds to 2 decimal places using `HALF_UP` rounding
   mode

**Architecture:**

- Follows Use Case pattern (interface in `application.usecase`, implementation in `application.service`)
- Uses Strategy Pattern for flexible business rules (forgiveness and overtime calculation)
- Accepts primitive types (`Duration actualDuration`, `int billableMinutes`) to avoid cyclic dependencies
- No direct dependency on `RentalDuration` from rental module - decoupled via primitives
- Configuration-driven via `RentalProperties` (forgiveness threshold and time increment)
- Result Object Pattern: `RentalCost` interface in module root (public API), `BaseRentalCostResult` in `domain.service`

**Files Created:**

- `tariff/application/usecase/CalculateRentalCostUseCase.java` - Use Case interface
- `tariff/application/service/CalculateRentalCostService.java` - Use Case implementation
- `tariff/RentalCost.java` - Public API interface (module root)
- `tariff/domain/service/BaseRentalCostResult.java` - Result record implementation
- `tariff/application/strategy/ForgivenessStrategy.java` - Strategy interface
- `tariff/application/strategy/ThresholdForgivenessStrategy.java` - Threshold-based forgiveness
- `tariff/application/strategy/OvertimeCalculationStrategy.java` - Strategy interface
- `tariff/application/strategy/ProportionalOvertimeCalculationStrategy.java` - Proportional calculation
- `tariff/application/config/RentalCostCalculationConfig.java` - Strategy bean configuration
- `tariff/application/service/CalculateRentalCostServiceTest.java` - Comprehensive unit tests
- `component-test/src/test/resources/features/tariff/rental-cost-calculation.feature.example` - Component test (
  disabled)

**Files Modified:**

- `rental/application/config/RentalProperties.java` - added `ForgivenessProperties` nested class
- `application.yaml` - forgiveness configuration (7 minutes threshold)
- `tariff/TariffFacade.java` - updated `calculateFinalCost()` to accept primitive types
- `tariff/TariffFacadeImpl.java` - updated implementation for new signature
- `shared/domain/model/vo/Money.java` - added automatic rounding in `Money.of(BigDecimal)`

## Progress Log

### 2026-02-24

- ✅ **Completed**: Full implementation of rental cost calculation with Strategy Pattern
- ✅ Created Use Case pattern: `CalculateRentalCostUseCase` interface and `CalculateRentalCostService` implementation
- ✅ Implemented Strategy Pattern for flexible business rules:
   - `ForgivenessStrategy` interface with `ThresholdForgivenessStrategy` implementation
   - `OvertimeCalculationStrategy` interface with `ProportionalOvertimeCalculationStrategy` implementation
- ✅ Created `RentalCost` interface (public API) and `BaseRentalCostResult` record (domain service)
- ✅ Integrated forgiveness rule (7 minutes threshold) via `RentalProperties.ForgivenessProperties`
- ✅ Added `calculateFinalCost()` method to `TariffFacade` for cross-module access
- ✅ Tariff selection based on actual duration (not planned duration)
- ✅ Money encapsulation: automatic rounding in `Money.of(BigDecimal)` factory method
- ✅ Uses existing `app.rental.time-increment` property for overtime rounding
- ✅ Resolved cyclic dependency by accepting primitive types (`Duration actualDuration`, `int billableMinutes`)
- ✅ Created comprehensive unit tests with parameterized tests covering all scenarios
- ✅ Component test feature file renamed to `.example` for future use
- ✅ All tests passing, no compilation errors, no unnecessary stubbings

**Implementation Highlights:**

- **Use Case Pattern**: `CalculateRentalCostUseCase` interface in `application.usecase`, service in
  `application.service`
- **Strategy Pattern**: Flexible forgiveness and overtime calculation strategies for future extensibility
- **Tariff Selection**: Uses `TariffPeriodSelector` based on actual duration (not planned)
- **Proportional Overtime**: Calculates based on period base price: `(basePrice / period_minutes) * 5 * intervals`
- **Forgiveness Rule**: Configurable threshold (default 7 minutes) via `RentalProperties`
- **Money Encapsulation**: Automatic rounding to 2 decimal places in `Money.of()` factory method
- **Module Boundaries**: No cyclic dependencies - accepts primitive types instead of domain objects
- **Configuration**: Uses `app.rental.time-increment` (5 minutes) for rounding intervals
- **Result Object**: `RentalCost` interface in module root, `BaseRentalCostResult` in `domain.service`
- Ready for integration with US-RN-006 (Equipment Return) for full return flow

## References

- User Story File: [docs/tasks/us/US-TR-002/us-tr-002.md](../../../docs/tasks/us/US-TR-002/us-tr-002.md)
- Dependencies: US-TR-001, US-RN-007
- Integrates: US-TR-003 (forgiveness rule implemented), US-TR-004 (overtime calculation implemented)
- Used by: US-RN-006 (Equipment Return - will use `TariffFacade.calculateFinalCost()`)
