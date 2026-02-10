# [US-RN-002] - Автоматический подбор тарифа (Automatic Tariff Selection)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-09, 2026-02-10, 2026-02-09 (Error Handling)  
**Priority:** High  
**Module:** tariff  
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

- [x] Create TariffSelectionService in tariff module
- [x] Implement selection algorithm (select lowest price for period)
- [x] Implement API endpoint GET /api/tariffs/selection for tariff selection
- [x] Add manual tariff override capability (already exists via PATCH /api/rentals/{id})
- [x] Create component tests for selection scenarios
- [x] Write unit tests for selection algorithm
- [x] Write tests for edge cases (no matching tariff)
- [x] Document selection logic

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                   | Status      | Updated    | Notes                                           |
|-----|-------------------------------|-------------|------------|-------------------------------------------------|
| 2.1 | Create selection service      | Completed   | 2026-02-09 | TariffSelectionService created                  |
| 2.2 | Implement selection algorithm | Completed   | 2026-02-09 | TariffPriceSelector + improved TariffFacadeImpl |
| 2.3 | Add caching                   | Not Started | 2026-01-26 | Moved to TECH-005                               |
| 2.4 | Create selection endpoint     | Completed   | 2026-02-09 | GET /api/tariffs/selection                      |
| 2.5 | Implement manual override     | Completed   | 2026-02-09 | Already exists via PATCH /api/rentals/{id}      |
| 2.6 | Create tests                  | Completed   | 2026-02-09 | Unit, WebMvc, and Component tests               |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-TR-001
- Part of Phase 3: Main Rental Process

### 2026-02-09

- **Implementation Completed:**
  - Created `TariffPriceSelector` utility for selecting correct price by period
  - Enhanced `TariffPeriodSelector` with `selectPeriod()` method returning `TariffPeriod` enum
  - Improved `TariffFacadeImpl.selectTariff()` to use period-specific prices instead of basePrice
  - Created `TariffSelectionService` wrapping TariffFacade
  - Added `halfHourPrice` field to `TariffInfo` public API
  - Created RESTful endpoint `GET /api/tariffs/selection` (sub-resource pattern)
  - Created `TariffSelectionResponse` DTO and `TariffSelectionMapper`
  - Updated `TariffToInfoMapper` to include halfHourPrice
  - Created unit tests for `TariffPriceSelector` and `TariffSelectionService`
  - Created WebMvc tests for `/selection` endpoint
  - Created component tests (Cucumber) for tariff selection scenarios
  - `CreateRentalService` and `UpdateRentalService` automatically use improved algorithm via TariffFacade
- **Status:** Completed
- **Note:** Caching moved to separate task TECH-005

### 2026-02-10

- **Period Selection Logic Updated:**
  - Updated `TariffPeriodSelector` to use new period boundaries:
    - HALF_HOUR: up to 30 minutes (inclusive)
    - HOUR: from 31 minutes to 3 hours 59 minutes (up to 239 minutes)
    - DAY: 4 hours or more (240 minutes and above)
  - Created comprehensive unit tests for `TariffPeriodSelector` covering all boundary cases
  - Updated documentation to reflect new period selection rules

### 2026-02-09 (Error Handling Update)

- **Error Handling Improvements:**
  - Changed HTTP status code for `SuitableTariffNotFoundException` from 422 (Unprocessable Content) to 404 (Not Found)
  - Updated `TariffRestControllerAdvice` and `RentalRestControllerAdvice` to return 404 status
  - Added comprehensive error response assertions in component tests for all error scenarios:
    - 400 Bad Request: missing required parameters (equipmentType, durationMinutes)
    - 404 Not Found: no suitable tariff found for equipment type
  - Updated rental feature files with test scenarios for tariff selection errors:
    - Create rental with auto-selected tariff when no suitable tariff found
    - Update rental equipment when no suitable tariff found for new equipment type
    - Update rental duration when no suitable tariff found for equipment type
  - All error responses now include proper `title` and `detail` fields in ProblemDetail format

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.tariff
├── application
│   ├── service.TariffSelectionService
│   └── dto.TariffSelectionCriteria
├── web.query
│   ├── TariffQueryController
│   └── dto.TariffSelectionResponse
└── domain
    └── model.TariffSelectionStrategy
```

**API Endpoint:**

- `GET /api/tariffs/selection?equipmentType={slug}&durationMinutes={minutes}&rentalDate={date}` - Get selected tariff (
  RESTful подресурс)
- Параметры:
  - `equipmentType` (String, required) - slug типа оборудования
  - `durationMinutes` (int, required) - длительность аренды в минутах (must be positive)
  - `rentalDate` (LocalDate, optional) - дата аренды (по умолчанию: сегодня)
- Response (200 OK):

```json
{
  "id": 1,
  "name": "Hourly Rate",
  "equipmentType": "bicycle",
  "price": 300.00,
  "period": "HOUR"
}
```

**Error Responses:**

- **400 Bad Request** - Missing required parameters:

```json
{
  "title": "Bad Request",
  "detail": "Required request parameter 'equipmentType' for method parameter type String is not present"
}
```

- **404 Not Found** - No suitable tariff found:

```json
{
  "title": "Suitable tariff not found",
  "detail": "No suitable tariff found for equipment type 'scooter' on date 2026-02-09 for duration: 120 minutes"
}
```

**RESTful обоснование:**

- Использует подресурс `/selection` вместо действия `/suggest` (глагол)
- Соответствует паттернам проекта (`/equipments/by-uid`, `/equipments/by-serial`)
- Явно выражает семантику: выбор тарифа из коллекции тарифов

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

None - implementation completed successfully

## Implementation Summary

**Completed on:** 2026-02-09

**Key Changes:**

1. Created `TariffPriceSelector` utility for selecting correct price by period (HALF_HOUR, HOUR, DAY)
2. Enhanced `TariffPeriodSelector` with `selectPeriod()` method returning `TariffPeriod` enum
3. Improved `TariffFacadeImpl.selectTariff()` to compare tariffs by period-specific prices instead of basePrice
4. Created `TariffSelectionService` wrapping TariffFacade for internal module use
5. Added `halfHourPrice` field to `TariffInfo` public API
6. Created RESTful endpoint `GET /api/tariffs/selection` following sub-resource pattern
7. Created `TariffSelectionResponse` DTO and `TariffSelectionMapper`
8. Updated `TariffToInfoMapper` to include halfHourPrice mapping
9. Comprehensive test coverage: unit tests, WebMvc tests, and component tests
10. Updated period selection logic with new boundaries (2026-02-10):
  - HALF_HOUR: up to 30 minutes (inclusive)
  - HOUR: from 31 minutes to 3 hours 59 minutes (up to 239 minutes)
  - DAY: 4 hours or more (240 minutes and above)
11. Created unit tests for `TariffPeriodSelector` covering all boundary cases

**Algorithm:**

- Determines period (HALF_HOUR/HOUR/DAY) based on rental duration:
  - **HALF_HOUR**: Duration up to 30 minutes (inclusive)
  - **HOUR**: Duration from 31 minutes to 3 hours 59 minutes (up to 239 minutes)
  - **DAY**: Duration of 4 hours or more (240 minutes and above)
- Selects tariff with lowest price for the determined period
- Filters by equipment type, active status, and validity date

## References

- User Story File: [docs/tasks/us/US-RN-002/us-rn-002.md](../../../docs/tasks/us/US-RN-002/us-rn-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-TR-001 (Tariff catalog must exist)
- Used by: US-RN-001 (Rental creation workflow)
