# Архитектура системы BikeRent

## 1. Цель сервиса

Создание цифровой системы управления прокатом оборудования (велосипеды, самокаты), автоматизирующей учет аренды,
клиентов,
финансовых операций и технического обслуживания для повышения операционной эффективности и качества обслуживания
клиентов.

---

## 2. Выбор архитектурного подхода

### Решение: Spring Modulith (пакетная модульность)

**Обоснование:**

- NFR-100 требует монолитной архитектуры с модульностью
- NFR-110 требует деплоя одним файлом (fat JAR)
- NFR-112 требует минимальной инфраструктуры
- Spring Modulith уже интегрирован в проект ([`service/build.gradle`](service/build.gradle), строки 34-35, 50)

**Преимущества:**

- Строгий контроль границ модулей через `@ApplicationModule`
- Автоматическая документация модульной структуры
- Event-driven коммуникация между модулями
- Простой рефакторинг в микросервисы при необходимости

---

## 3. Доменная декомпозиция

На основе функциональных требований (FR-*) выделяются 8 модулей:

```mermaid
flowchart TB
    subgraph CoreModules [Core Modules - Независимые]
        Customer[customer<br/>FR-CL]
        Equipment[equipment<br/>FR-EQ]
        Tariff[tariff<br/>FR-TR]
    end

    subgraph BusinessModules [Business Modules]
        Rental[rental<br/>FR-RN]
        Finance[finance<br/>FR-FN]
        Maintenance[maintenance<br/>FR-MT]
    end

    subgraph SupportModules [Support Modules]
        Reporting[reporting<br/>FR-RP]
        Admin[admin<br/>FR-AD]
    end

    Rental --> Customer
    Rental --> Equipment
    Rental --> Tariff
    Finance --> Rental
    Maintenance --> Equipment
    Reporting --> Rental
    Reporting --> Finance
    Reporting --> Customer
    Reporting --> Equipment
    Admin --> Customer
```

### 3.1 Модуль Customer (FR-CL-001 - FR-CL-005)

**Ответственность:** Управление профилями клиентов

**Ключевые сущности:**

- `Customer` - профиль клиента (телефон, имя, email, дата рождения)
- `CustomerStatistics` - агрегированная статистика

**API для других модулей:**

- `CustomerRef` - ссылка на клиента (id + контактные данные)
- `CustomerLookupService` - поиск по телефону

### 3.2 Модуль Equipment (FR-EQ-001 - FR-EQ-005)

**Ответственность:** Каталог оборудования и управление статусами

**Ключевые сущности:**

- `Equipment` - единица оборудования (порядковый номер, QR/NFC UID, тип, статус)
- `EquipmentType` - тип оборудования (велосипед, самокат)
- `EquipmentStatus` - enum: AVAILABLE, RENTED, MAINTENANCE, DECOMMISSIONED

**API для других модулей:**

- `EquipmentRef` - ссылка на оборудование
- `EquipmentAvailabilityService` - проверка доступности
- События: `EquipmentStatusChanged`

### 3.3 Модуль Tariff (FR-TR-001 - FR-TR-005)

**Ответственность:** Справочник тарифов и расчет стоимости

**Ключевые сущности:**

- `Tariff` - тариф (тип оборудования, период, базовая цена, цена доп.времени)
- `TariffPeriod` - enum: HOUR_1, HOUR_2, DAY

**Ключевые сервисы:**

- `TariffSelectionService` - автоматический подбор тарифа
- `RentalCostCalculator` - расчет стоимости с учетом бизнес-правил:
    - Кратность 5 минут
    - Правило "прощения" (до 7 мин)
    - Округление просрочки до 10 мин

### 3.4 Модуль Rental (FR-RN-001 - FR-RN-009) - CORE

**Ответственность:** Основной бизнес-процесс аренды

**Ключевые сущности:**

- `Rental` - запись аренды (клиент, оборудование, время начала/окончания, статус)
- `RentalStatus` - enum: DRAFT, ACTIVE, COMPLETED, CANCELLED

**Ключевые use cases:**

- `CreateRentalUseCase` - создание аренды (FR-RN-001)
- `StartRentalUseCase` - запуск аренды с предоплатой (FR-RN-005)
- `ReturnEquipmentUseCase` - возврат с расчетом доплаты (FR-RN-006)
- `CancelRentalUseCase` - отмена в течение 10 мин (FR-RN-008)

**События:**

- `RentalStarted` - уведомляет Finance о предоплате
- `RentalCompleted` - уведомляет Finance о доплате
- `RentalCancelled` - уведомляет Finance о возврате

### 3.5 Модуль Finance (FR-FN-001 - FR-FN-004)

**Ответственность:** Финансовые операции и касса

**Ключевые сущности:**

- `Payment` - платеж (тип, сумма, способ оплаты, связь с арендой)
- `PaymentType` - enum: PREPAYMENT, SURCHARGE, REFUND
- `PaymentMethod` - enum: CASH, CARD, QR
- `CashRegister` - касса оператора (смена, баланс)

**Подписка на события:**

- `RentalStarted` -> создание записи предоплаты
- `RentalCompleted` -> фиксация доплаты
- `RentalCancelled` -> создание возврата

### 3.6 Модуль Reporting (FR-RP-001 - FR-RP-005)

**Ответственность:** Отчетность и аналитика

**Ключевые отчеты:**

- `IncomeReport` - доходы за период
- `EquipmentUtilizationReport` - загрузка оборудования
- `CustomerAnalyticsReport` - аналитика по клиентам

**Особенности:**

- Read-only доступ к данным других модулей
- Асинхронная агрегация через события

### 3.7 Модуль Maintenance (FR-MT-001 - FR-MT-004)

**Ответственность:** Техническое обслуживание

**Ключевые сущности:**

- `MaintenanceRecord` - запись ТО (оборудование, тип работ, дата)
- `MaintenanceSchedule` - график планового ТО

**Подписка на события:**

- `RentalCompleted` -> обновление счетчика использования

### 3.8 Модуль Admin (FR-AD-001 - FR-AD-006)

**Ответственность:** Управление пользователями и настройками

**Ключевые сущности:**

- `User` - пользователь системы
- `Role` - enum: OPERATOR, ADMIN (упрощенная модель: оператор = все кроме админа)
- `SystemSettings` - настраиваемые бизнес-параметры
- `AuditLog` - журнал аудита

**Ролевая модель:**

- **OPERATOR** - объединяет функции оператора проката, технического персонала и бухгалтерии
- **ADMIN** - полный доступ ко всем функциям + управление системой

---

## 4. Структура пакетов

```
com.github.jenkaby.bikerental
├── BikeRentalApplication.java
├── shared/                          # Shared Kernel
│   ├── domain/
│   │   ├── Money.java              # Value Object для денежных сумм
│   │   └── TimeRange.java          # Value Object для временных интервалов
│   └── event/
│       └── DomainEvent.java        # Базовый интерфейс событий
│
├── customer/
│   ├── package-info.java           # @ApplicationModule
│   ├── api/                        # Публичный API модуля
│   │   ├── CustomerRef.java
│   │   └── CustomerLookupService.java
│   ├── domain/
│   │   ├── Customer.java
│   │   └── CustomerRepository.java
│   ├── application/
│   │   └── CustomerService.java
│   └── web/
│       └── OperatorCustomerController.java    # /api/operator/customers
│
├── equipment/
│   ├── package-info.java
│   ├── api/
│   │   ├── EquipmentRef.java
│   │   ├── EquipmentAvailabilityService.java
│   │   └── event/
│   │       └── EquipmentStatusChanged.java
│   ├── domain/
│   │   ├── Equipment.java
│   │   ├── EquipmentType.java
│   │   ├── EquipmentStatus.java
│   │   └── EquipmentRepository.java
│   ├── application/
│   │   └── EquipmentService.java
│   └── web/
│       ├── OperatorEquipmentController.java   # /api/operator/equipment
│       └── AdminEquipmentController.java      # /api/admin/equipment
│
├── tariff/
│   ├── package-info.java
│   ├── api/
│   │   ├── TariffInfo.java
│   │   ├── TariffSelectionService.java
│   │   └── RentalCostCalculator.java
│   ├── domain/
│   │   ├── Tariff.java
│   │   ├── TariffPeriod.java
│   │   └── TariffRepository.java
│   ├── application/
│   │   └── TariffService.java
│   └── web/
│       ├── OperatorTariffController.java      # /api/operator/tariffs (read-only)
│       └── AdminTariffController.java         # /api/admin/tariffs (full CRUD)
│
├── rental/
│   ├── package-info.java
│   ├── api/
│   │   ├── RentalInfo.java
│   │   └── event/
│   │       ├── RentalStarted.java
│   │       ├── RentalCompleted.java
│   │       └── RentalCancelled.java
│   ├── domain/
│   │   ├── Rental.java
│   │   ├── RentalStatus.java
│   │   └── RentalRepository.java
│   ├── application/
│   │   ├── CreateRentalUseCase.java
│   │   ├── StartRentalUseCase.java
│   │   ├── ReturnEquipmentUseCase.java
│   │   └── CancelRentalUseCase.java
│   └── web/
│       └── OperatorRentalController.java      # /api/operator/rentals
│
├── finance/
│   ├── package-info.java
│   ├── domain/
│   │   ├── Payment.java
│   │   ├── PaymentType.java
│   │   ├── PaymentMethod.java
│   │   ├── CashRegister.java
│   │   └── PaymentRepository.java
│   ├── application/
│   │   ├── PaymentService.java
│   │   ├── CashRegisterService.java
│   │   └── RentalEventHandler.java  # @EventListener
│   └── web/
│       └── OperatorFinanceController.java     # /api/operator/payments, /cash-register
│
├── reporting/
│   ├── package-info.java
│   ├── application/
│   │   ├── DashboardService.java
│   │   ├── ShiftReportService.java
│   │   ├── IncomeReportService.java
│   │   ├── EquipmentUtilizationReportService.java
│   │   └── CustomerAnalyticsService.java
│   └── web/
│       ├── OperatorReportController.java      # /api/operator/dashboard, /reports/shift
│       └── AdminReportController.java         # /api/admin/reports (full analytics)
│
├── maintenance/
│   ├── package-info.java
│   ├── domain/
│   │   ├── MaintenanceRecord.java
│   │   ├── MaintenanceSchedule.java
│   │   └── MaintenanceRepository.java
│   ├── application/
│   │   ├── MaintenanceService.java
│   │   └── EquipmentUsageTracker.java  # @EventListener
│   └── web/
│       └── OperatorMaintenanceController.java # /api/operator/maintenance
│
└── admin/
    ├── package-info.java
    ├── domain/
    │   ├── User.java
    │   ├── Role.java                          # enum: OPERATOR, ADMIN
    │   ├── SystemSettings.java
    │   └── AuditLog.java
    ├── application/
    │   ├── UserService.java
    │   ├── SettingsService.java
    │   ├── AuditService.java
    │   └── BackupService.java
    └── web/
        ├── AdminUserController.java           # /api/admin/users
        ├── AdminSettingsController.java       # /api/admin/settings
        ├── AdminAuditController.java          # /api/admin/audit-log
        └── AdminBackupController.java         # /api/admin/backup
```

---

## 5. Коммуникация между модулями

### 5.1 Синхронная (через API интерфейсы)

```mermaid
sequenceDiagram
    participant Operator
    participant RentalController
    participant CreateRentalUseCase
    participant CustomerLookupService
    participant EquipmentAvailabilityService
    participant TariffSelectionService
    Operator ->> RentalController: POST /api/rentals
    RentalController ->> CreateRentalUseCase: execute(command)
    CreateRentalUseCase ->> CustomerLookupService: findByPhone(phone)
    CustomerLookupService -->> CreateRentalUseCase: CustomerRef
    CreateRentalUseCase ->> EquipmentAvailabilityService: checkAvailable(equipmentNumber)
    EquipmentAvailabilityService -->> CreateRentalUseCase: EquipmentRef
    CreateRentalUseCase ->> TariffSelectionService: selectTariff(equipmentType, period)
    TariffSelectionService -->> CreateRentalUseCase: TariffInfo
    CreateRentalUseCase -->> RentalController: RentalInfo
```

### 5.2 Асинхронная (через Spring Application Events)

```mermaid
sequenceDiagram
    participant ReturnEquipmentUseCase
    participant ApplicationEventPublisher
    participant PaymentEventHandler
    participant EquipmentEventHandler
    participant MaintenanceEventHandler
    ReturnEquipmentUseCase ->> ApplicationEventPublisher: publish(RentalCompleted)
    ApplicationEventPublisher -->> PaymentEventHandler: handle(RentalCompleted)
    PaymentEventHandler ->> PaymentEventHandler: recordSurcharge()
    ApplicationEventPublisher -->> EquipmentEventHandler: handle(RentalCompleted)
    EquipmentEventHandler ->> EquipmentEventHandler: updateStatus(AVAILABLE)
    ApplicationEventPublisher -->> MaintenanceEventHandler: handle(RentalCompleted)
    MaintenanceEventHandler ->> MaintenanceEventHandler: updateUsageHours()
```

---

## 6. Модель данных (ER-диаграмма)

```mermaid
erDiagram
    CUSTOMER {
        uuid id PK
        string phone UK
        string first_name
        string last_name
        string email
        date birth_date
        timestamp created_at
    }

    EQUIPMENT {
        uuid id PK
        string serial_number UK
        string nfc_uid UK
        uuid equipment_type_id FK
        string status
        timestamp commissioned_at
        int total_usage_hours
    }

    EQUIPMENT_TYPE {
        uuid id PK
        string name
        string description
    }

    TARIFF {
        uuid id PK
        uuid equipment_type_id FK
        string period
        decimal base_price
        decimal extra_time_price_per_5min
        date valid_from
        date valid_to
        boolean is_active
    }

    RENTAL {
        uuid id PK
        uuid customer_id FK
        uuid equipment_id FK
        uuid tariff_id FK
        string status
        timestamp started_at
        timestamp expected_return_at
        timestamp actual_return_at
        int planned_minutes
        int actual_minutes
    }

    PAYMENT {
        uuid id PK
        uuid rental_id FK
        string payment_type
        string payment_method
        decimal amount
        timestamp created_at
    }

    MAINTENANCE_RECORD {
        uuid id PK
        uuid equipment_id FK
        string maintenance_type
        text description
        timestamp started_at
        timestamp completed_at
    }

    APP_USER {
        uuid id PK
        string username UK
        string password_hash
        string role
        boolean is_active
    }

    SYSTEM_SETTINGS {
        string key PK
        string value
        timestamp updated_at
    }

    AUDIT_LOG {
        uuid id PK
        uuid user_id FK
        string event_type
        string entity_type
        uuid entity_id
        jsonb changes
        timestamp created_at
    }

    CUSTOMER ||--o{ RENTAL: has
    EQUIPMENT ||--o{ RENTAL: used_in
    EQUIPMENT }o--|| EQUIPMENT_TYPE: belongs_to
    TARIFF }o--|| EQUIPMENT_TYPE: applies_to
    RENTAL ||--|| TARIFF: uses
    RENTAL ||--o{ PAYMENT: has
    EQUIPMENT ||--o{ MAINTENANCE_RECORD: has
    APP_USER ||--o{ AUDIT_LOG: performs
```

---

## 7. REST API структура

API разделен на два контекста по ролям: **Operator API** (роль OPERATOR) и **Admin API** (роль ADMIN).

### 7.1 RESTful принципы

- Используются **существительные во множественном числе** для ресурсов
- **HTTP методы** определяют действие: GET (чтение), POST (создание), PUT (полное обновление), PATCH (частичное
  обновление), DELETE (удаление)
- **Фильтрация** через query parameters: `?status=active&from=2026-01-01`
- **Изменение состояния** через PATCH с телом запроса: `{"status": "ACTIVE"}`
- **Вложенные ресурсы** для связанных данных: `/customers/{id}/rentals`
- **Версионирование** через Content-Type (media type negotiation)

---

### 7.2 Версионирование API (Content-Type Negotiation)

Версия API указывается через заголовки `Accept` и `Content-Type` с использованием vendor-specific media type:

```
application/vnd.bikerent.v1+json
```

**Формат:** `application/vnd.{vendor}.{version}+json`

**Примеры запросов:**

```http
# Запрос с указанием версии
GET /api/customers HTTP/1.1
Accept: application/vnd.bikerent.v1+json

# Ответ
HTTP/1.1 200 OK
Content-Type: application/vnd.bikerent.v1+json

{"id": "uuid", "phone": "+79001234567", ...}
```

```http
# POST запрос
POST /api/rentals HTTP/1.1
Content-Type: application/vnd.bikerent.v1+json
Accept: application/vnd.bikerent.v1+json

{"customer_id": "uuid", "equipment_id": "uuid", ...}
```

**Конфигурация Spring Boot:**

```java

@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    public static final String V1_MEDIA_TYPE = "application/vnd.bikerent.v1+json";

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.parseMediaType(V1_MEDIA_TYPE))
                .mediaType("v1", MediaType.parseMediaType(V1_MEDIA_TYPE));
    }
}

// Использование в контроллере
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @GetMapping(produces = "application/vnd.bikerent.v1+json")
    public List<CustomerDto> getCustomers(@RequestParam(required = false) String phone) {
        // ...
    }
}
```

**Преимущества подхода:**

- Чистые URL без версии
- Гибкость: можно поддерживать несколько версий одновременно
- Соответствует HTTP спецификации (content negotiation)
- Легко добавить v2 без изменения URL

**Fallback:**

- Если `Accept` header не указан, используется последняя стабильная версия (v1)

---

### 7.3 Operator API (`/api/...`)

Доступен для роли **OPERATOR** (и ADMIN). Основные рабочие операции.

**Клиенты (customer module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/customers` | Список клиентов с фильтрацией `?phone={digits}` |

| POST | `/customers` | Создание клиента |

| GET | `/customers/{id}` | Профиль клиента |

| PUT | `/customers/{id}` | Полное обновление профиля |

| PATCH | `/customers/{id}` | Частичное обновление профиля |

| GET | `/customers/{id}/rentals` | История аренд клиента |

**Оборудование (equipment module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/equipments` | Список оборудования с фильтрацией `?number={num}&uid={nfc_uid}&status={status}` |

| GET | `/equipments/{id}` | Детали оборудования |

| PATCH | `/equipments/{id}` | Изменение статуса `{"status": "MAINTENANCE"}` |

**Тарифы (tariff module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/tariffs` | Справочник активных тарифов |

| GET | `/tariffs/{id}` | Детали тарифа |

| GET | `/tariffs/cost` | Расчет стоимости `?equipment_type_id={id}&duration_minutes={min}` |

**Аренда (rental module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/rentals` | Список аренд с фильтрацией `?status=ACTIVE&overdue=true` |

| POST | `/rentals` | Создание аренды (статус DRAFT) |

| GET | `/rentals/{id}` | Детали аренды |

| PATCH | `/rentals/{id}` | Изменение статуса аренды |

| GET | `/rentals/{id}/payments` | Платежи по аренде |

**Статусы аренды (через PATCH):**

```json
// Запуск аренды (после предоплаты)
PATCH /rentals/{
  id
}
{
  "status": "ACTIVE",
  "started_at": "2026-01-21T10:00:00Z"
}

// Возврат оборудования
PATCH /rentals/{
  id
}
{
  "status": "COMPLETED",
  "returned_at": "2026-01-21T11:30:00Z"
}

// Отмена аренды
PATCH /rentals/{
  id
}
{
  "status": "CANCELLED",
  "cancellation_reason": "customer_request"
}
```

**Платежи (finance module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/payments` | Список платежей с фильтрацией `?rental_id={id}&date_from={date}` |

| POST | `/payments` | Регистрация платежа |

| GET | `/payments/{id}` | Детали платежа |

**Смены/Касса (finance module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/shifts` | Список смен `?status=OPEN&operator_id={id}` |

| POST | `/shifts` | Открытие новой смены |

| GET | `/shifts/current` | Текущая открытая смена |

| GET | `/shifts/{id}` | Детали смены |

| PATCH | `/shifts/{id}` | Закрытие смены `{"status": "CLOSED", "actual_cash": 15000}` |

**Техническое обслуживание (maintenance module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/maintenance-records` | Записи ТО `?equipment_id={id}&status=PENDING` |

| POST | `/maintenance-records` | Создание записи ТО |

| GET | `/maintenance-records/{id}` | Детали записи ТО |

| PATCH | `/maintenance-records/{id}` | Обновление записи `{"status": "COMPLETED"}` |

**Дашборд и отчеты (reporting module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/dashboard` | Сводка текущего дня |

| GET | `/reports/shift` | Отчет по смене `?shift_id={id}` |

---

### 7.4 Admin API (`/api/admin/...`)

Доступен только для роли **ADMIN**. Настройки и управление системой.

**Пользователи (admin module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/users` | Список пользователей |

| POST | `/admin/users` | Создание пользователя |

| GET | `/admin/users/{id}` | Данные пользователя |

| PUT | `/admin/users/{id}` | Полное обновление |

| PATCH | `/admin/users/{id}` | Частичное обновление `{"is_active": false}` |

| PUT | `/admin/users/{id}/password` | Установка нового пароля |

**Настройки системы (admin module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/settings` | Все настройки |

| GET | `/admin/settings/{key}` | Конкретная настройка |

| PUT | `/admin/settings/{key}` | Обновление настройки |

| PATCH | `/admin/settings` | Массовое обновление настроек |

**Тарифы - полный CRUD (tariff module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/tariffs` | Все тарифы (включая неактивные) |

| POST | `/admin/tariffs` | Создание тарифа |

| GET | `/admin/tariffs/{id}` | Детали тарифа |

| PUT | `/admin/tariffs/{id}` | Полное обновление тарифа |

| PATCH | `/admin/tariffs/{id}` | Частичное обновление `{"is_active": false}` |

| DELETE | `/admin/tariffs/{id}` | Удаление (если не используется) |

**Оборудование - полный CRUD (equipment module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/equipments` | Все оборудование (включая списанное) |

| POST | `/admin/equipments` | Добавление оборудования |

| GET | `/admin/equipments/{id}` | Детали оборудования |

| PUT | `/admin/equipments/{id}` | Полное обновление |

| PATCH | `/admin/equipments/{id}` | Частичное обновление / списание `{"status": "DECOMMISSIONED"}` |

| DELETE | `/admin/equipments/{id}` | Удаление (только если никогда не использовалось) |

**Типы оборудования (equipment module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/equipment-types` | Типы оборудования |

| POST | `/admin/equipment-types` | Создание типа |

| GET | `/admin/equipment-types/{id}` | Детали типа |

| PUT | `/admin/equipment-types/{id}` | Обновление типа |

**Отчетность и аналитика (reporting module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/reports/income` | Доходы `?from={date}&to={date}&group_by=day` |

| GET | `/admin/reports/equipment-utilization` | Загрузка `?from={date}&to={date}` |

| GET | `/admin/reports/customer-analytics` | Аналитика клиентов |

| GET | `/admin/reports/financial-reconciliation` | Финансовая сверка |

**Экспорт (reporting module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/exports/income` | Экспорт доходов `?format=xlsx&from={date}&to={date}` |

| GET | `/admin/exports/audit-log` | Экспорт аудита `?format=pdf` |

**Журнал аудита (admin module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/audit-logs` | Журнал аудита `?user_id={id}&event_type={type}&from={date}` |

| GET | `/admin/audit-logs/{id}` | Детали записи |

**Резервные копии (admin module):**

| Метод | Endpoint | Описание |

|-------|----------|----------|

| GET | `/admin/backups` | Список резервных копий |

| POST | `/admin/backups` | Создание резервной копии |

| GET | `/admin/backups/{id}` | Метаданные копии |

| POST | `/admin/backups/{id}/restorations` | Запуск восстановления (создает ресурс restoration) |

| GET | `/admin/restorations/{id}` | Статус восстановления |

---

### 7.5 Диаграмма API по ролям

```mermaid
flowchart LR
    subgraph Versioning [Версионирование]
        V1["Accept: application/vnd.bikerent.v1+json"]
    end

    subgraph Roles [Роли]
        OPERATOR[OPERATOR]
        ADMIN[ADMIN]
    end

    subgraph OperatorAPI ["/api/ - Operator API"]
        op_customers["/customers"]
        op_equipments["/equipments"]
        op_tariffs["/tariffs - GET only"]
        op_rentals["/rentals"]
        op_payments["/payments"]
        op_shifts["/shifts"]
        op_maintenance["/maintenance-records"]
        op_dashboard["/dashboard"]
    end

    subgraph AdminAPI ["/api/admin/ - Admin API"]
        admin_users["/users"]
        admin_settings["/settings"]
        admin_tariffs["/tariffs - full CRUD"]
        admin_equipments["/equipments - full CRUD"]
        admin_reports["/reports"]
        admin_exports["/exports"]
        admin_audit["/audit-logs"]
        admin_backups["/backups"]
    end

    V1 --> OperatorAPI
    V1 --> AdminAPI
    OPERATOR --> OperatorAPI
    ADMIN --> OperatorAPI
    ADMIN --> AdminAPI
```

### 7.6 Примеры запросов

**Создание и запуск аренды:**

```http
# 1. Поиск клиента по телефону
GET /api/customers?phone=1234 HTTP/1.1
Accept: application/vnd.bikerent.v1+json

# 2. Поиск оборудования по номеру
GET /api/equipments?number=15 HTTP/1.1
Accept: application/vnd.bikerent.v1+json

# 3. Расчет стоимости
GET /api/tariffs/cost?equipment_type_id=uuid&duration_minutes=60 HTTP/1.1
Accept: application/vnd.bikerent.v1+json

# 4. Создание аренды
POST /api/rentals HTTP/1.1
Content-Type: application/vnd.bikerent.v1+json
Accept: application/vnd.bikerent.v1+json

{
  "customer_id": "uuid",
  "equipment_id": "uuid",
  "tariff_id": "uuid",
  "planned_duration_minutes": 60
}

# 5. Регистрация предоплаты
POST /api/payments HTTP/1.1
Content-Type: application/vnd.bikerent.v1+json
Accept: application/vnd.bikerent.v1+json

{
  "rental_id": "uuid",
  "type": "PREPAYMENT",
  "method": "CASH",
  "amount": 500.00
}

# 6. Запуск аренды
PATCH /api/rentals/{id} HTTP/1.1
Content-Type: application/vnd.bikerent.v1+json
Accept: application/vnd.bikerent.v1+json

{
  "status": "ACTIVE"
}
```

**Возврат оборудования:**

```http
# 1. Поиск аренды по NFC-метке оборудования
GET /api/rentals?status=ACTIVE&equipment_uid=nfc123 HTTP/1.1
Accept: application/vnd.bikerent.v1+json

# 2. Завершение аренды (система рассчитает доплату)
PATCH /api/rentals/{id} HTTP/1.1
Content-Type: application/vnd.bikerent.v1+json
Accept: application/vnd.bikerent.v1+json

{
  "status": "COMPLETED"
}

# Response:
HTTP/1.1 200 OK
Content-Type: application/vnd.bikerent.v1+json

{
  "id": "uuid",
  "status": "COMPLETED",
  "surcharge_amount": 100.00,
  "surcharge_reason": "overtime_15_min"
}

# 3. Регистрация доплаты (если есть)
POST /api/payments HTTP/1.1
Content-Type: application/vnd.bikerent.v1+json
Accept: application/vnd.bikerent.v1+json

{
  "rental_id": "uuid",
  "type": "SURCHARGE",
  "method": "CARD",
  "amount": 100.00
}
```

---

## 8. Технические решения

### 8.1 Аутентификация и авторизация (NFR-020)

**Аутентификация:**

- Spring Security OAuth2 с Google Sign-In
- JWT токены для API авторизации

**Авторизация по ролям:**

```java

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests(auth -> auth
                        // Admin API - только ADMIN
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        // Operator API - доступен OPERATOR и ADMIN
                        .requestMatchers("/api/**")
                        .hasAnyRole("OPERATOR", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(...)
            .build();
    }
}
```

**Упрощенная ролевая модель:**

- `OPERATOR` - выполняет все операционные задачи (аренда, клиенты, оплата, ТО, базовые отчеты)
- `ADMIN` - полный доступ + управление пользователями, настройками, тарифами, полная аналитика

### 8.2 NFC/QR сканирование (FR-EQ-003)

- Поиск оборудования: `GET /api/equipments?uid={nfc_uid}`
- Поиск активной аренды: `GET /api/rentals?status=ACTIVE&equipment_uid={nfc_uid}`
- Frontend использует Web NFC API (Android Chrome) или камеру для QR

### 8.3 Экспорт отчетов (NFR-014)

- Apache POI для Excel
- iText/OpenPDF для PDF

### 8.4 Деплой (NFR-110, NFR-111)

- GitHub Actions CI/CD (уже настроен в `.github/workflows/build.yml`)
- Single fat JAR через `./gradlew bootJar`
- Docker-compose для локальной разработки (уже есть в `docker/`)

---

## 9. Приоритеты реализации

**Phase 1 - MVP (Высокий приоритет):**

1. customer module - базовый CRUD + поиск по телефону
2. equipment module - каталог + статусы
3. tariff module - справочник + расчет стоимости
4. rental module - полный цикл аренды

**Phase 2 - Финансы и отчетность:**

5. finance module - платежи и касса
6. reporting module - базовые отчеты
7. admin module - пользователи и настройки

**Phase 3 - Расширение:**

8. maintenance module - ТО и обслуживание
9. Интеграция NFC/QR сканирования
10. Google OAuth

---

## 10. Валидация архитектуры

Spring Modulith предоставляет встроенные тесты для проверки модульной структуры:

```java

@Test
void verifyModularStructure() {
    ApplicationModules.of(BikeRentalApplication.class).verify();
}

@Test
void generateModuleDocumentation() {
    new Documenter(ApplicationModules.of(BikeRentalApplication.class))
            .writeDocumentation();
}
```

Эти тесты гарантируют:

- Отсутствие циклических зависимостей между модулями
- Доступ только через публичный API модуля
- Корректную изоляцию internal-классов