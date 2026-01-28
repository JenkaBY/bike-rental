# [US-RN-002] - Автоматический подбор тарифа (Automatic Tariff Selection)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** rental  
**Dependencies:** US-TR-001

## Original Request

**Как** Оператор проката  
**Я хочу** чтобы система автоматически подбирала тариф  
**Чтобы** не тратить время на ручной выбор тарифа

## User Story Details

**Описание:**  
Система должна автоматически подбирать тариф на основе типа оборудования и выбранного времени аренды.

**Критерии приемки:**

- После выбора оборудования и времени система показывает тариф
- Отображение стоимости за выбранный период
- Возможность ручного изменения тарифа (для администратора)
- Тариф зависит от типа оборудования (велосипед/самокат/другое)

**Связанные требования:** FR-RN-002

## Thought Process

Automatic tariff selection improves UX by reducing operator workload. The system should intelligently select the best
matching tariff. Considerations:

1. **Selection Criteria**: Equipment type + rental duration
2. **Active Tariffs Only**: Only select from currently active tariffs
3. **Validity Period**: Tariff must be valid for rental start date
4. **Multiple Matches**: If multiple tariffs match, use priority/cost rules
5. **Override Capability**: Admin can manually change selected tariff

**Selection Algorithm:**

```
1. Get equipment type
2. Get planned rental duration
3. Find all active tariffs for equipment type
4. Filter tariffs valid for rental date
5. Match tariffs by duration (HOUR_1, HOUR_2, DAY, etc.)
6. If multiple matches, select lowest cost
7. Return selected tariff
```

**Architecture Decisions:**

- Selection logic in tariff module (TariffSelectionService)
- Rental module calls tariff selection service
- Service returns TariffRef DTO
- Cache frequently used tariff queries
- Allow manual override via separate command

## Implementation Plan

- [ ] Create TariffSelectionService in tariff module
- [ ] Implement selection algorithm with ranking
- [ ] Add caching for tariff selection queries
- [ ] Implement API endpoint for tariff suggestions
- [ ] Add manual tariff override capability
- [ ] Create component tests for selection scenarios
- [ ] Write unit tests for selection algorithm
- [ ] Write tests for edge cases (no matching tariff)
- [ ] Document selection logic

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                   | Status      | Updated    | Notes |
|-----|-------------------------------|-------------|------------|-------|
| 2.1 | Create selection service      | Not Started | 2026-01-26 |       |
| 2.2 | Implement selection algorithm | Not Started | 2026-01-26 |       |
| 2.3 | Add caching                   | Not Started | 2026-01-26 |       |
| 2.4 | Create suggestion endpoint    | Not Started | 2026-01-26 |       |
| 2.5 | Implement manual override     | Not Started | 2026-01-26 |       |
| 2.6 | Create tests                  | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-TR-001
- Part of Phase 3: Main Rental Process

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.tariff
├── application
│   ├── service.TariffSelectionService
│   └── dto.TariffSelectionCriteria
├── web.query
│   ├── TariffQueryController
│   └── dto.TariffSuggestionResponse
└── domain
    └── model.TariffSelectionStrategy
```

**API Endpoint:**

- `GET /api/tariffs/suggest?equipmentTypeId={id}&durationMinutes={minutes}` - Get suggested tariff
- Response:

```json
{
  "selectedTariff": {
    "id": "uuid",
    "name": "Hourly Rate",
    "basePrice": 300.00,
    "period": "HOUR_1"
  },
  "alternatives": [
    {
      "id": "uuid",
      "name": "Two Hour Special",
      "basePrice": 500.00,
      "period": "HOUR_2"
    }
  ]
}
```

**Selection Service:**

```java

@Service
public class TariffSelectionService {

    @Cacheable("tariff-selection")
    public TariffRef selectTariff(UUID equipmentTypeId, int durationMinutes, LocalDate rentalDate) {
        // Get active tariffs for equipment type
        List<Tariff> tariffs = tariffRepository.findActiveByEquipmentType(equipmentTypeId, rentalDate);

        if (tariffs.isEmpty()) {
            throw new NoTariffAvailableException(equipmentTypeId);
        }

        // Find matching period
        TariffPeriod matchingPeriod = TariffPeriod.fromMinutes(durationMinutes);

        // Filter by period
        List<Tariff> matchingTariffs = tariffs.stream()
                .filter(t -> t.getPeriod() == matchingPeriod)
                .toList();

        // If exact match not found, find closest larger period
        if (matchingTariffs.isEmpty()) {
            matchingTariffs = findClosestLargerPeriod(tariffs, durationMinutes);
        }

        // Select lowest cost
        return matchingTariffs.stream()
                .min(Comparator.comparing(Tariff::getBasePrice))
                .map(this::toTariffRef)
                .orElseThrow(() -> new NoTariffAvailableException(equipmentTypeId));
    }
}
```

**TariffPeriod Enhancement:**

```java
public enum TariffPeriod {
    HOUR_1(60),
    HOUR_2(120),
    HOUR_4(240),
    DAY(1440),
    WEEK(10080);

    private final int minutes;

    public static TariffPeriod fromMinutes(int minutes) {
        for (TariffPeriod period : values()) {
            if (period.minutes == minutes) {
                return period;
            }
        }
        // Return closest match
        return Arrays.stream(values())
                .filter(p -> p.minutes >= minutes)
                .findFirst()
                .orElse(WEEK);
    }
}
```

**Integration with Rental:**

```java
public class RentalService {

    private final TariffSelectionService tariffSelectionService;

    public Rental createRental(CreateRentalCommand command) {
        // ...
        TariffRef selectedTariff = tariffSelectionService.selectTariff(
                equipment.getTypeId(),
                command.plannedDurationMinutes(),
                command.startDate()
        );

        rental.setTariff(selectedTariff);
        // ...
    }
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-RN-002/us-rn-002.md](../../../docs/tasks/us/US-RN-002/us-rn-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-TR-001 (Tariff catalog must exist)
- Used by: US-RN-001 (Rental creation workflow)
