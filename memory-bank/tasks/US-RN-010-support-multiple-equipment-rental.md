# [US-RN-010] Поддержка аренды нескольких единиц оборудования

**Status:** In Progress - URGENT  
**Added:** 2026-03-12  
**Updated:** 2026-03-13

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

Текущая реализация хранит поле `equipment_id` / `equipment_uid` в таблице `rentals` (TECH-007) и многие use-case'ы
опираются на единственную единицу оборудования для поиска и завершения аренды. Нужно перейти на модель "аренда" ↔ "
множество equipment" (One Rental — Many Equipment). Это повлечёт изменения в базе данных, доменной модели, JPA
сущностях, DTO, use case'ах, миграциях Liquibase, а также в компонентах, которые рассчитывают стоимость и проверяют
состояния оборудования.

Ключевые решения и предположения:

- Использовать связующую таблицу `rental_equipments` (rental_id, equipment_id, equipment_uid, expected_return_at?,
  started_at?, status?) либо простую таблицу связей вместе с основным `rentals` и доп. колонками в самой аренде (keep
  minimal). Предпочтение: отдельная сущность `rental_equipment` чтобы сохранять per-equipment metadata (например
  startedAt/actualReturnAt/overdue для каждой единицы).
- Тарифы: воспользоваться существующим `TariffFacade` / `TariffPriceSelector` (US-TR-002, US-RN-002, US-TR-002 уже
  выполнены) — расширить его чтобы принимать список equipmentType -> агрегировать суммарную стоимость за период (
  например суммировать стоимости для каждого equipment по его типу и выбранному тарифу).
- Предоплата: до записи аренды вычислять полную стоимость для периода и формировать запись предоплаты (US-RN-004 уже
  реализован) с суммой, равной общей сумме по всем equipment.

## Implementation Plan

План работ (по шагам):

1. Анализ и подготовка
    - Просмотреть все места в репозитории где используется `equipmentId`, `equipmentUid` и поле `equipment_uid` в
      таблице `rentals`.
    - Составить список классов/эндпоинтов/use-case'ов/SQL миграций для изменения.

2. Схема БД (Liquibase миграции)
    - Создать новую таблицу `rental_equipments`:
        - id (PK), rental_id (FK -> rentals.id), equipment_id, equipment_uid, status, started_at, expected_return_at,
          actual_return_at, created_at
    - Удалить колонку `equipment_uid` и `equipment_id` из таблицы `rentals`.

3. Домен / JPA
    - Добавить доменную child-entity `RentalEquipment` внутри `Rental` aggregate (см. DDD): `Rental` остаётся Aggregate
      Root,
      все операции над equipment (назначение, старт, возврат) проходят через `Rental`.
    - Не создавать отдельный доменный репозиторий для `RentalEquipment`. Сохранение/загрузка — через `RentalRepository`.
    - Обновить `Rental` доменную модель: убрать; добавить коллекцию `List<RentalEquipment>`.
    - В infrastructure добавить JPA entity `RentalEquipmentJpaEntity` и связать `RentalJpaEntity` с
      `@OneToMany(cascade = ALL, orphanRemoval = true)`.
    - Обновить мапперы (MapStruct) и JPA mapping (one-to-many). Перенести `tariffId` на уровень `RentalEquipment` (
      per-equipment tariff) и агрегировать суммы на уровне `Rental`.

4. DTO / API
    - Обновить `CreateRentalRequest`, `ReturnEquipmentRequest`, `RentalUpdateJsonPatchRequest`, `RentalResponse` и
      связанные мапперы, чтобы поддерживали массив `equipmentIds` / `equipmentUids`.
    - Добавить валидацию: пустой массив недопустим

5. Бизнес-логика
    - Update CreateRentalService: при создании аренды создавать записи в `rental_equipments` для каждой единицы.
    - Update ReturnEquipmentService: поддерживать возврат одной единицы (partial return) и возврат всех сразу; если
      аренда содержит несколько единиц — завершать аренду только когда все equipment помечены как возвращённые или
      отдельная логика завершения по аренде/по equipment.
    - Обновить статус-транзиции оборудования (EquipmentStatusTransitionPolicy) для массовой операции.

6. Tariff calculation
    - Расширить TariffFacade / CalculateRentalCostUseCase чтобы принимать список equipment (или их типы) и возвращать
      breakdown по equipment.
    - Убедиться, что предоплата (CreateRental + US-RN-004) учитывает суммарную стоимость.

7. Tests
    - WebMvc tests для API DTO (create rental request with multiple equipment, return single equipment, return all).
    - Component tests (BDD) — обновить feature файлы и добавить сценарии для множественной аренды.

8. Migration & Rollout
    - Применить миграции в staging, прогнать интеграционные тесты.
    - Документировать изменения API в OpenAPI/Swagger и memory-bank.

## Subtasks

| ID  | Description                                                                      | Status      | Updated    | Notes                             |
|-----|----------------------------------------------------------------------------------|-------------|------------|-----------------------------------|
| 1.1 | Анализ кода и мест использования equipmentId/equipmentUid                        | In Progress | 2026-03-12 | Найти все usages через grep/IDE   |
| 2.1 | Liquibase миграция: создать rental_equipments                                    | Not Started |            | Изменить существующую миграцию    |
| 3.1 | Добавить domain + JPA entity RentalEquipment                                     | Not Started |            | MapStruct mapper                  |
| 4.1 | Обновить DTO: CreateRentalRequest, ReturnEquipmentRequest, RentalResponse и т.д. | Not Started |            | Обратная совместимость не нужна   |
| 5.1 | Обновить CreateRentalService и ReturnEquipmentService                            | Not Started |            | Включая prepayment расчёт         |
| 6.1 | Обновить TariffFacade / CalculateRentalCostUseCase                               | Not Started |            | Возвращать breakdown по equipment |
| 7.1 | Обновить unit/WebMvc/component тесты                                             | Not Started |            | Обновить feature файлы BDD        |

## Steps

1. Обновить доменную модель Rental в Rental.java: заменить поля equipmentId/equipmentUid/tariffId на
   List<RentalEquipment> equipments, добавить методы addEquipment(), returnEquipment(equipmentId),
   allEquipmentReturned(), complete() с проверкой всех возвратов.
2. Создать domain child entity RentalEquipment в rental.domain.model: поля id, equipmentId, equipmentUid, tariffId,
   equipmentType, status (enum: ASSIGNED/ACTIVE/RETURNED), startedAt, expectedReturnAt, actualReturnAt, estimatedCost,
   finalCost — чистый POJO без JPA аннотаций.
3. Создать JPA entity RentalEquipmentJpaEntity в rental.infrastructure.persistence.entity с @ManyToOne на
   RentalJpaEntity; в RentalJpaEntity добавить @OneToMany(cascade = ALL, orphanRemoval = true) — без отдельного
   репозитория, каскадное сохранение через RentalJpaRepository.save().
4. Обновить RentalJpaMapper в RentalJpaMapper.java: добавить маппинг List<RentalEquipmentJpaEntity> ↔
   List<RentalEquipment>, удалить маппинг старых полей equipmentId/equipmentUid.
5. Обновить use case'ы — CreateRentalService, UpdateRentalService, ReturnEquipmentService, RecordPrepaymentService — все
   операции с equipment проходят через Rental aggregate root (rental.addEquipment(...), rental.returnEquipment(...),
   rentalRepository.save(rental)).
6. Обновить task file US-RN-010: зафиксировать архитектурное решение — RentalEquipment это child entity, не отдельный
   aggregate; убрать пункт про отдельный репозиторий.

## Impact Analysis

- DB: Добавление новой таблицы; plan for backfill of existing rentals (migrate data from rentals.equipment_uid to
  rental_equipments).
- Finance: Prepayment calculation flow должен учитывать несколько единиц — проверить события
  PaymentReceived/PaymentCaptured и их consumers.
- Tariff module: расширение TariffFacade должно быть идемпотентным и не ломать существующие расчёты для единичной
  аренды.

## Acceptance Criteria

1. Можно создать аренду с массивом equipmentIds; записи в `rental_equipments` созданы корректно.
2. Предоплата рассчитывается как сумма стоимостей по тарифам для каждого equipmentType за выбранный период.
3. Возврат одной единицы оборудования корректно рассчитывает доплату для этой единицы и помечает её как возвращённую;
   аренда завершается только когда все единицы возвращены (или если оператор принудительно завершает аренду —
   валидация/флаг).
4. Существующие API, которые передают одиночный `equipmentId` продолжают работать (обработчик принимает и конвертирует в
   массив из одного элемента).
5. Обновления покрыты unit, WebMvc и component тестами; component tests (BDD) обновлены и все проходят.

## Notes / Open Questions

- Нужен ли per-equipment expected_return_at/start_at или достаточно хранить его только на уровне аренды? Предложение —
  добавить per-equipment поля, чтобы поддержать частичный возврат и частичную оплату. Окончательное решение - да, нужен.
- Как отмечать предоплату — одна запись платежа на аренду (аванс покрывает всю аренду) или отдельные платежи per
  equipment? Предпочтение: одна запись на аренду с суммарной суммой. Да, одна запись на аренду.
- Нужно ли поддерживать частичный возврат с автоматическим перерасчётом Overtime/forgiveness? Да — покрыть в расчёте
  стоимости при возврате одной единицы. Да, поддерживать частичный возврат с перерасчётом.

## Next Steps (short-term)

1. Провести grep по проекту и собрать список всех мест, где используется `equipmentId` / `equipmentUid`.
2. Обновить существую Liquibase миграцию `rentals.create-table.xml` в module `service/src/main/resources/db/changelog/`.
   Создать новую таблицу `rental_equipments` отдельным changelog файлом и удалить колонки `equipment_id` и
   `equipment_uid` из `rentals`.
3. Начать реализацию domain + JPA entity для `RentalEquipment`.

