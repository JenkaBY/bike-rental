# [US-RN-010] Поддержка аренды нескольких единиц оборудования

**Status:** Completed - URGENT  
**Added:** 2026-03-12  
**Updated:** 2026-03-17

## Original Request

Изменить текущее поведение аренды, чтобы поддерживать аренду нескольких единиц оборудования в одной аренде.

Требуется:

- Изменить структуру базы данных, чтобы хранить связь аренды с несколькими единицами оборудования.
- Обновить реквесты (`CreateRentalRequest`, `ReturnEquipmentRequest`, `RentalUpdateJsonPatchRequest`) и респонсы (
  `RentalResponse`) чтобы поддерживали массив `equipmentId` / `equipmentUid` где это применимо.
- Обновить логику расчёта тарифов: тарифы будут подбираться автоматически по типам оборудования; предоплата должна
  покрывать стоимость всех единиц оборудования за выбранный период согласно тарифам по типу оборудования.
- Обновить все места в коде, завязанные на единичный equipmentId/equipmentUid (поиск аренды, возврат, старт аренды,
  изменение статусов оборудования, учёт предоплат и доплат).

## Thought Process

Текущая реализация хранила поле `equipment_id` / `equipment_uid` в таблице `rentals` и многие use-case'ы опирались на
единственную единицу оборудования. Выполнен переход на модель "аренда" ↔ "множество equipment" (One Rental — Many
Equipment) через новую таблицу `rental_equipments`.

Ключевые архитектурные решения (принятые):

- `RentalEquipment` — child entity, не отдельный aggregate. Нет отдельного репозитория. Сохранение/загрузка только через
  `RentalRepository` (каскадное).
- `Rental` остаётся Aggregate Root. Все операции над equipment (назначение, активация, возврат) проходят через `Rental`.
- Тариф подбирается per-equipment при создании/обновлении аренды через `TariffFacade.selectTariff()`. Стоимость
  рассчитывается per-equipment и суммируется.
- Предоплата: одна запись на аренду с суммарной суммой (существующий механизм US-RN-004 не менялся).
- Частичный возврат: `ReturnEquipmentRequest` принимает списки `equipmentIds` и `equipmentUids`. Пустые списки означают
  возврат всего оборудования. Аренда завершается только когда `allEquipmentReturned()`.
- `RentalUpdated` event — новый event для синхронизации статусов equipment при изменении draft-аренды.

## Implementation Plan

1. ✅ Анализ и подготовка — все места использования `equipmentId`/`equipmentUid` проверены
2. ✅ Схема БД — создана `rental_equipments`, удалены `equipment_id`/`equipment_uid` из `rentals`
3. ✅ Домен / JPA — `RentalEquipment`, `RentalEquipmentStatus`, `RentalEquipmentJpaEntity`, `RentalJpaEntity` обновлён
4. ✅ DTO / API — `CreateRentalRequest`, `ReturnEquipmentRequest`, `RentalResponse`, `RentalReturnResponse` обновлены
5. ✅ Бизнес-логика — `CreateRentalService`, `UpdateRentalService`, `ReturnEquipmentService` обновлены
6. ✅ Tariff calculation — per-equipment через существующий `TariffFacade`; `CalculateRentalCostUseCase` не менялся
7. ⚠️ Tests — WebMvc тесты обновлены; component tests обновлены; `UpdateRentalServiceTest` удалён (нужно пересоздать)

## Subtasks

| ID  | Description                                                                      | Status      | Updated    | Notes                                                                                                   |
|-----|----------------------------------------------------------------------------------|-------------|------------|---------------------------------------------------------------------------------------------------------|
| 1.1 | Анализ кода и мест использования equipmentId/equipmentUid                        | Complete    | 2026-03-12 |                                                                                                         |
| 2.1 | Liquibase миграция: создать rental_equipments                                    | Complete    | 2026-03-13 | `rental-equipments.create-table.xml`; `equipment_id/uid` удалены из `rentals`                           |
| 3.1 | Добавить domain + JPA entity RentalEquipment                                     | Complete    | 2026-03-13 | `RentalEquipment.java`, `RentalEquipmentStatus`, `RentalEquipmentJpaEntity`, `RentalEquipmentJpaMapper` |
| 4.1 | Обновить DTO: CreateRentalRequest, ReturnEquipmentRequest, RentalResponse и т.д. | Complete    | 2026-03-17 | `List<Long> equipmentIds`, `EquipmentItemResponse`, `PaymentInfoResponse`, `CostBreakdown` как list     |
| 5.1 | Обновить CreateRentalService и ReturnEquipmentService                            | Complete    | 2026-03-17 | Частичный возврат; `RequestedEquipmentValidator`; `UpdateRentalService` с валидацией новых equipment    |
| 6.1 | Обновить TariffFacade / CalculateRentalCostUseCase                               | Complete    | 2026-03-17 | Per-equipment через существующий `TariffFacade`; breakdown в `ReturnEquipmentResult`                    |
| 7.1 | Обновить unit/WebMvc/component тесты                                             | In Progress | 2026-03-17 | WebMvc обновлены; component tests обновлены; `UpdateRentalServiceTest` удалён — нужно пересоздать       |

## Progress Tracking

**Overall Status:** Completed — 100% Complete (marked complete despite outstanding subtasks)

### Subtask Detail

| Module          | Change                                                                                                                | Status |
|-----------------|-----------------------------------------------------------------------------------------------------------------------|--------|
| DB              | `rental-equipments` таблица создана, `equipment_id`/`equipment_uid` удалены из `rentals`                              | ✅      |
| Domain          | `RentalEquipment`, `RentalEquipmentStatus` добавлены; `Rental` обновлён (equipments list + методы)                    | ✅      |
| JPA             | `RentalEquipmentJpaEntity` + `@OneToMany` в `RentalJpaEntity`; каскадное сохранение                                   | ✅      |
| Mappers         | `RentalEquipmentJpaMapper`, `RentalEquipmentMapper`, `RentalEquipmentStatusMapper`, `RentalEquipmentWebMapper`        | ✅      |
| Events          | `RentalCreated`/`RentalStarted`/`RentalCompleted` — списки IDs; новый `RentalUpdated` для draft                       | ✅      |
| Equipment       | `RentalEventListener` обновлён; `GetEquipmentByIdsUseCase`; `EquipmentFacade.findByIds()`                             | ✅      |
| Finance         | `FinanceFacade.getPayments()`; `PaymentType.ADDITIONAL_PAYMENT`                                                       | ✅      |
| Web DTO         | `CreateRentalRequest.equipmentIds: List<Long>`; `ReturnEquipmentRequest` списки; `RentalResponse.equipmentItems`      | ✅      |
| Services        | `CreateRentalService`, `UpdateRentalService`, `ReturnEquipmentService` — полностью переработаны                       | ✅      |
| WebMvc tests    | `RentalCommandControllerTest` обновлён (219 изменений)                                                                | ✅      |
| Component tests | `rental.feature`, `rental-return.feature`, `rental-query.feature`, `rental-validation.feature` обновлены              | ✅      |
| Unit tests      | `RecordPrepaymentServiceTest`, `FindRentalsServiceTest`, `RentalTest` обновлены; `UpdateRentalServiceTest` **удалён** | ⚠️     |

## Known Issues & Open Concerns

1. **`UpdateRentalServiceTest` удалён**: 493 строк тестового покрытия удалено (коммит
   `c3da983 - refactor: partially fix the concerns of unit tests`). Необходимо пересоздать этот класс с тестами для
   нового multi-equipment API.
2. **TECH-015 создан**: Формула `toPay` в `ReturnEquipmentService` имеет проблему расчёта при частичном возврате —
   учитывает `remainingEstimatedCost` вместо `remainingFinalCost`.
3. **`isPrepaymentSufficient` / `canBeActivated`** используют приватное поле `this.estimatedCost`, которое
   устанавливается через `setEstimatedCost()`. Нужно проверить, что `RecordPrepaymentService` правильно вызывает эту
   проверку — фактическая сумма должна сравниваться с `getEstimatedCost()` (сумма по оборудованию).

## Progress Log

### 2026-03-17

**Анализ изменений ветки `feature/support-rental-of-equipment-group`**

- 101 файл изменён, 2811 строк добавлено, 1397 удалено в 20 коммитах
- Все ключевые компоненты реализации завершены: DB, Domain, JPA, Services, DTO, Events, Equipment module, Finance module
- **DB:** `rental_equipments` таблица создана; `equipment_id` и `equipment_uid` удалены из таблицы `rentals`; FK +
  индексы добавлены
- **Domain:** `RentalEquipment` (child entity с методами `activateForRental()`, `markReturned()`, `assigned()`),
  `RentalEquipmentStatus` (ASSIGNED/ACTIVE/RETURNED), `Rental` полностью переработан
- **JPA:** `RentalEquipmentJpaEntity` + `@OneToMany(cascade=ALL, orphanRemoval=true)` + `@Fetch(FetchMode.SUBSELECT)` в
  `RentalJpaEntity`; computed `getEstimatedCost()`/`getFinalCost()` методы
- **Services:** `CreateRentalService` — per-equipment tariff + cost; `UpdateRentalService` — списки equipment с
  валидацией только новых; `ReturnEquipmentService` — частичный возврат, per-equipment cost, завершение только при
  `allEquipmentReturned()`
- **Events:** `RentalCreated`, `RentalStarted`, `RentalCompleted` расширены списками IDs; новый `RentalUpdated` для
  синхронизации статусов equipment при изменении draft
- **Equipment module:** `RentalEventListener` поддерживает списки + новый `onRentalUpdated()`;
  `EquipmentFacade.findByIds()`
- **Finance module:** `FinanceFacade.getPayments()` добавлен для расчёта доплаты
- **Web:** все DTO обновлены для работы с массивами; `EquipmentItemResponse`, `PaymentInfoResponse`, `CostBreakdown`
  list в `RentalReturnResponse`
- **Tests:** `RentalCommandControllerTest` и component tests обновлены; `UpdateRentalServiceTest` **удалён** (регрессия
  в покрытии)

### 2026-03-17 (Marked Completed)

- Task marked as **Completed** on 2026-03-17 by request. Note: subtask 7.1 (`UpdateRentalServiceTest`) remains In
  Progress and must be recreated to restore unit test coverage. The known issues section retains details about this
  outstanding work and TECH-015; please address them in a follow-up task.

### 2026-03-13

- Создан task файл
- Выполнен анализ кода (subtask 1.1)
- Созданы Liquibase миграции (subtask 2.1)
- Добавлены domain + JPA entities (subtask 3.1)

### 2026-03-12

- Task создан с первоначальным implementation plan

## Next Steps (short-term)

1. **КРИТИЧНО**: Пересоздать `UpdateRentalServiceTest.java` для нового multi-equipment API — тесты для: смены
   customerId, duration, equipmentIds (список), статуса ACTIVE/DRAFT, частичного патча
2. Проверить корректность `isPrepaymentSufficient()` — должна использовать `getEstimatedCost()` (сумму по equipment), а
   не приватное поле `estimatedCost`
3. TECH-015: Исправить формулу `toPay` в `ReturnEquipmentService` (неправильный учёт `remainingEstimatedCost`)
4. Прогнать component tests (`./gradlew :component-test:test`) после запуска БД
