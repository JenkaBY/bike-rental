# [US-TR-004] - Эндпоинт расчёта стоимости аренды (Rental Cost Estimation Endpoint)

**Status:** Pending  
**Added:** 2026-02-27  
**Updated:** 2026-02-27  
**Priority:** High  
**Module:** tariff  
**Dependencies:** US-TR-002, US-RN-002

## Original Request

Создать эндпоинт, который рассчитывает стоимость аренды по переданным параметрам:

- Дата аренды (может быть пустая — тогда используется сегодняшняя дата)
- Длительность аренды
- Тип оборудования **или** UID оборудования **или** ID тарифа

## User Story Details

**Как** оператор проката  
**Я хочу** узнать стоимость аренды до её создания  
**Чтобы** сообщить клиенту точную цену и принять предоплату

**Критерии приемки:**

- При передаче `equipmentType` — автоматически выбирается подходящий тариф (через `SelectTariffForRentalUseCase`)
- При передаче `equipmentUid` — определяется тип оборудования, затем выбирается тариф
- При передаче `tariffId` — тариф используется напрямую без подбора
- Если `rentalDate` не передана — используется сегодняшняя дата
- Ответ содержит: итоговая сумма (BigDecimal), ID тарифа, продолжительность в минутах
- Если подходящий тариф не найден — 404 с понятным сообщением
- Если оборудование не найдено — 422

## Thought Process

Оператору нужно рассчитать стоимость **до** создания аренды — например, при консультации клиента.
Сейчас `GET /api/tariffs/selection` позволяет найти тариф, но не возвращает итоговую стоимость.
Новый эндпоинт должен объединить подбор тарифа и расчёт стоимости в один запрос.

**Логика разрешения тарифа (приоритет):**

1. `tariffId` передан → использовать напрямую через `GetTariffByIdUseCase`
2. `equipmentUid` передан → найти оборудование через `EquipmentFacade`, взять `typeSlug`, выбрать тариф через
   `SelectTariffForRentalUseCase`
3. `equipmentType` передан → выбрать тариф через `SelectTariffForRentalUseCase`

Ровно один из трёх параметров (`tariffId`, `equipmentUid`, `equipmentType`) должен быть передан — иначе 400.

**Расчёт стоимости:**

Для оценки (estimation) `actualDuration == plannedDuration`, `billableMinutes = (int) duration.toMinutes()`.
Вызов: `CalculateRentalCostUseCase.execute(tariffId, duration, billableMinutes, duration).totalCost()`

**Расположение эндпоинта:**

`GET /api/tariffs/cost-estimate` — в модуле tariff, контроллер `TariffQueryController`

## Implementation Plan

- [ ] Создать `EstimateRentalCostUseCase` (interface + command + result)
- [ ] Реализовать `EstimateRentalCostService` (логика разрешения тарифа + расчёт)
- [ ] Добавить `EquipmentFacade` в зависимости tariff-модуля (если не подключён)
- [ ] Создать `RentalCostEstimateResponse` DTO
- [ ] Добавить `GET /api/tariffs/cost-estimate` в `TariffQueryController`
- [ ] Написать unit-тесты для `EstimateRentalCostService`
- [ ] Написать WebMvc тесты для контроллера
- [ ] Написать компонентные тесты (feature file)

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                                   | Status      | Updated    | Notes                                               |
|-----|-----------------------------------------------|-------------|------------|-----------------------------------------------------|
| 4.1 | EstimateRentalCostUseCase interface + command | Not Started | 2026-02-27 |                                                     |
| 4.2 | EstimateRentalCostService implementation      | Not Started | 2026-02-27 | Resolve tariff → calculate cost                     |
| 4.3 | EquipmentFacade доступен в tariff-модуле      | Not Started | 2026-02-27 | Проверить зависимости, добавить если нужно          |
| 4.4 | RentalCostEstimateResponse DTO                | Not Started | 2026-02-27 | totalCost (BigDecimal), tariffId, durationMinutes   |
| 4.5 | GET /api/tariffs/cost-estimate в контроллере  | Not Started | 2026-02-27 |                                                     |
| 4.6 | Unit тесты EstimateRentalCostService          | Not Started | 2026-02-27 | Все 3 пути: tariffId / equipmentUid / equipmentType |
| 4.7 | WebMvc тесты контроллера                      | Not Started | 2026-02-27 | Valid / invalid / 404 / 422 сценарии                |
| 4.8 | Компонентные тесты (feature file)             | Not Started | 2026-02-27 |                                                     |

## Technical Details

**API Endpoint:**

```
GET /api/tariffs/cost-estimate
  ?durationMinutes={int}          — обязательный
  &rentalDate={YYYY-MM-DD}        — опциональный, default: today
  &equipmentType={slug}           — один из трёх
  &equipmentUid={uid}             — один из трёх
  &tariffId={id}                  — один из трёх
```

**Validation:**

- `durationMinutes` — обязательный, > 0
- Ровно один из (`tariffId`, `equipmentUid`, `equipmentType`) должен быть передан — иначе 400

**Request example:**

```
GET /api/tariffs/cost-estimate?durationMinutes=120&equipmentType=bicycle&rentalDate=2026-03-01
GET /api/tariffs/cost-estimate?durationMinutes=60&equipmentUid=BIKE-001
GET /api/tariffs/cost-estimate?durationMinutes=90&tariffId=1
```

**Response DTO:**

```java
record RentalCostEstimateResponse(
        Long tariffId,
        BigDecimal totalCost,
        int durationMinutes
) {
}
```

**Use Case:**

```java
interface EstimateRentalCostUseCase {
    Result execute(EstimateRentalCostCommand command);

    record EstimateRentalCostCommand(
            Duration duration,
            LocalDate rentalDate,      // nullable — default: today
            String equipmentType,      // nullable
            String equipmentUid,       // nullable
            Long tariffId              // nullable
    ) {
    }

    record Result(RentalCost rentalCost, TariffInfo tariff) {
    }
}
```

**Service flow:**

```
1. Resolve tariff:
   - if tariffId != null → GetTariffByIdUseCase.get(tariffId)
   - else if equipmentUid != null → EquipmentFacade.findByUid(equipmentUid) → typeSlug → SelectTariffForRentalUseCase
   - else → SelectTariffForRentalUseCase(equipmentType, durationMinutes, rentalDate)
2. CalculateRentalCostUseCase.execute(tariffId, duration, billableMinutes=durationMinutes, plannedDuration=duration)
3. Return Result(rentalCost, tariffInfo)
```

**Архитектурные замечания:**

- `EquipmentFacade` может потребоваться добавить в tariff-модуль — необходимо проверить текущий `settings.gradle` /
  зависимости на циклы
- Если EquipmentFacade вызывает cyclic dependency — альтернатива: принимать `equipmentUid` в контроллере, но разрешение
  делать через отдельный query-параметр в equipment-модуле (lookup endpoint) или добавить метод `findByUid` в
  `EquipmentFacade`

## References

- `TariffQueryController` — место размещения нового endpoint
- `SelectTariffForRentalUseCase` — существующий use case для подбора тарифа
- `CalculateRentalCostUseCase` — существующий use case для расчёта стоимости
- `GET /api/tariffs/selection` — существующий endpoint подбора тарифа (без расчёта стоимости)
- `EquipmentFacade` — для lookup оборудования по UID




