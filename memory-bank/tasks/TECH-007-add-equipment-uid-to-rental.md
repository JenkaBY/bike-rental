# [TECH-007] - Добавление equipmentUid в таблицу rental

**Status:** Completed  
**Added:** 2026-02-25  
**Updated:** 2026-02-25  
**Completed:** 2026-02-25  
**Priority:** URGENT  
**Module:** rental  
**Dependencies:** US-RN-001

## Original Request

**Как** Разработчик  
**Я хочу** хранить equipmentUid в таблице rental  
**Чтобы** упростить поиск активных аренд по UID оборудования при возврате

## Description

Добавить поле `equipment_uid` в таблицу `rentals` для хранения UID оборудования. Это позволит искать активные аренды
напрямую по UID без необходимости сначала находить equipmentId через таблицу equipments.

## Business Value

- Упрощает процесс возврата оборудования: можно найти активную аренду напрямую по UID метки
- Улучшает производительность: не требуется JOIN с таблицей equipments для поиска аренд по UID
- Упрощает интеграцию с фронтендом: клиент может передать UID и сразу получить аренду

## Implementation Plan

- [ ] Создать миграцию для добавления колонки `equipment_uid` в таблицу `rentals`
- [ ] Обновить `RentalJpaEntity` - добавить поле `equipmentUid`
- [ ] Обновить `Rental` domain model - добавить поле `equipmentUid`
- [ ] Обновить `RentalJpaMapper` - маппинг equipmentUid
- [ ] Обновить `CreateRentalService` - сохранять equipmentUid при создании аренды
- [ ] Обновить `UpdateRentalService` - обновлять equipmentUid при изменении equipmentId
- [ ] Добавить метод `findByStatusAndEquipmentUid` в `RentalRepository`
- [ ] Добавить метод `findByStatusAndEquipmentUid` в `RentalJpaRepository`
- [ ] Реализовать метод в `RentalRepositoryAdapter`
- [ ] Обновить `FindRentalsService` - поддержка фильтрации по `equipmentUid`
- [ ] Обновить `RentalQueryController` - добавить query параметр `equipmentUid` в GET /api/rentals
- [ ] Обновить существующие тесты
- [ ] Написать новые тесты для фильтрации по equipmentUid

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                                 | Status    | Updated    | Notes                          |
|-----|---------------------------------------------|-----------|------------|--------------------------------|
| 7.1 | Создать миграцию для equipment_uid          | Completed | 2026-02-25 |                                |
| 7.2 | Обновить JPA entity и domain model          | Completed | 2026-02-25 |                                |
| 7.3 | Обновить мапперы                            | Completed | 2026-02-25 | MapStruct автоматически маппит |
| 7.4 | Обновить сервисы создания/обновления аренды | Completed | 2026-02-25 |                                |
| 7.5 | Добавить метод поиска по equipmentUid       | Completed | 2026-02-25 |                                |
| 7.6 | Добавить query параметр equipmentUid в GET  | Completed | 2026-02-25 |                                |
| 7.7 | Обновить тесты                              | Completed | 2026-02-25 | Unit, WebMvc, Component тесты  |

## Progress Log

### 2026-02-25

- ✅ Создана миграция `rentals.add-equipment-uid.xml` с добавлением колонки, заполнением данных и индексом
- ✅ Обновлен Rental domain model - добавлено поле `equipmentUid` и метод `setEquipmentUid()`
- ✅ Обновлен RentalJpaEntity - добавлено поле `equipmentUid` с аннотацией `@Column`
- ✅ MapStruct автоматически маппит `equipmentUid` (имена полей совпадают)
- ✅ Добавлен метод `findByStatusAndEquipmentUid` в RentalRepository, RentalJpaRepository и RentalRepositoryAdapter
- ✅ Обновлен CreateRentalService - сохраняет `equipmentUid` при создании аренды из EquipmentInfo
- ✅ Обновлен UpdateRentalService - обновляет `equipmentUid` при изменении `equipmentId`
- ✅ Обновлен FindRentalsService - добавлена поддержка фильтрации по `equipmentUid` с приоритетом над customerId
- ✅ Обновлен FindRentalsQuery - добавлено поле `equipmentUid`
- ✅ Обновлен RentalQueryController - добавлен query параметр `equipmentUid` в GET /api/rentals
- ✅ Создан FindRentalsServiceTest с тестами для всех сценариев фильтрации
- ✅ Обновлен UpdateRentalServiceTest - проверка обновления `equipmentUid`
- ✅ Обновлен RentalQueryControllerTest - добавлены тесты с `equipmentUid` параметром
- ✅ Обновлен RentalJpaEntityTransformer - добавлена поддержка `equipmentUid` в тестовых данных
- ✅ Добавлены component тесты в rental-query.feature для фильтрации по `equipmentUid`
- ✅ Обновлены все существующие сценарии в rental-query.feature - добавлен `equipmentUid` во все тестовые данные (
  обязательное поле)

**Реализовано:**

- Database migration с заполнением существующих записей
- Domain model и JPA entity обновлены
- Repository методы для поиска по equipmentUid
- Сервисы обновлены для сохранения equipmentUid
- REST endpoint поддерживает фильтрацию по equipmentUid
- Полное покрытие тестами (unit, WebMvc, component)

## Technical Details

**Database Migration:**

```xml

<changeSet id="rentals.add-equipment-uid" author="bikerental">
    <addColumn tableName="rentals">
        <column name="equipment_uid" type="VARCHAR(100)"/>
    </addColumn>

    <!-- Заполнить существующие записи equipment_uid из таблицы equipments -->
    <sql>
        UPDATE rentals r
        SET equipment_uid = (
        SELECT e.uid
        FROM equipments e
        WHERE e.id = r.equipment_id
        )
        WHERE r.equipment_id IS NOT NULL;
    </sql>

    <!-- Добавить индекс для производительности -->
    <createIndex tableName="rentals" indexName="idx_rentals_equipment_uid">
        <column name="equipment_uid"/>
    </createIndex>
</changeSet>
```

**RentalJpaEntity:**

```java

@Column(name = "equipment_uid", length = 100)
private String equipmentUid;
```

**Rental Domain Model:**

```java
private String equipmentUid; // UID оборудования (для быстрого поиска)
```

**RentalRepository:**

```java
Page<Rental> findByStatusAndEquipmentUid(RentalStatus status, String equipmentUid, PageRequest pageRequest);
```

**API Endpoint:**

- `GET /api/rentals?status=ACTIVE&equipmentUid=ABC123XYZ` - Поиск активных аренд по UID оборудования

## Migration Strategy

1. Добавить колонку как nullable
2. Заполнить существующие записи данными из таблицы equipments
3. После проверки можно сделать колонку NOT NULL (опционально)

## Notes

- Поле `equipment_uid` является денормализацией данных для улучшения производительности
- При изменении `equipmentId` в аренде необходимо также обновлять `equipmentUid`
- UID оборудования можно получить через `EquipmentFacade.findById(equipmentId)`
