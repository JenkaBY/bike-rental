# [US-RN-004] - Внесение предоплаты (Record Prepayment)

**Status:** Completed  
**Added:** 2026-02-06  
**Completed:** 2026-02-10  
**Priority:** URGENT  
**Module:** rental  
**Dependencies:** US-RN-001, US-FN-001

## Original Request

**Как** Оператор проката  
**Я хочу** внести предоплату за аренду  
**Чтобы** зафиксировать оплату и запустить аренду

## User Story Details

**Описание:**  
Система должна позволять оператору внести предоплату за аренду. Предоплата должна быть не меньше рассчитанной стоимости
аренды.

**Последовательность действий:**

1. Оператор выбирает аренду в статусе DRAFT
2. Оператор указывает сумму предоплаты и способ оплаты
3. Система проверяет, что сумма не меньше рассчитанной стоимости
4. Система записывает платеж через модуль finance
5. Система публикует событие PaymentReceived

**Критерии приемки:**

- Предоплата может быть внесена только для аренды в статусе DRAFT
- Сумма предоплаты должна быть не меньше рассчитанной стоимости
- Если рассчитанная стоимость не установлена, предоплата не может быть внесена
- Оператор обязателен для всех предоплат
- После успешного внесения предоплаты публикуется событие PaymentReceived

## Implementation Summary

### Domain Layer

- **`Rental.isPrepaymentSufficient(Money amount)`** - метод для проверки достаточности суммы предоплаты
- **`InsufficientPrepaymentException`** - доменное исключение с фабричными методами:
    - `estimatedCostNotSet(Long rentalId)` - когда рассчитанная стоимость не установлена
    - `amountBelowEstimatedCost(Long rentalId)` - когда сумма меньше рассчитанной стоимости
    - `forInsufficientPrepayment(Rental rental)` - статический метод для выбора правильного исключения

### Application Layer

- **`RecordPrepaymentUseCase`** - интерфейс use case для записи предоплаты
- **`RecordPrepaymentService`** - реализация use case:
    - Валидация статуса аренды (должен быть DRAFT)
    - Валидация достаточности суммы предоплаты через `Rental.isPrepaymentSufficient()`
    - Вызов `FinanceFacade.recordPrepayment()` для записи платежа
    - Возврат `PaymentInfo` с информацией о платеже

### Web Layer

- **`RecordPrepaymentRequest`** - DTO запроса с валидацией:
    - `amount` - обязательное, минимум 0.01
    - `paymentMethod` - обязательное
    - `operatorId` - обязательное, не может быть пустым
- **`PrepaymentResponse`** - DTO ответа с информацией о платеже:
    - `paymentId`, `amount`, `paymentMethod`, `receiptNumber`, `createdAt`
- **`RentalCommandMapper`** - маппер с использованием `MoneyMapper` для конвертации:
    - `toRecordPrepaymentCommand()` - конвертация запроса в команду
    - `toPrepaymentResponse()` - конвертация `PaymentInfo` в ответ
- **`RentalCommandController`** - REST контроллер:
    - `POST /api/rentals/{id}/prepayments` - эндпоинт для записи предоплаты
- **`RentalRestControllerAdvice`** - обработчик исключений:
    - `InsufficientPrepaymentException` → HTTP 422 (Unprocessable Content)

### Integration

- **`FinanceFacade.recordPrepayment()`** - метод для записи предоплаты через finance модуль
- **`PaymentInfo`** - DTO для передачи информации о платеже между модулями
- **`PaymentMethod`** - enum перемещен в публичный пакет `finance` для использования в rental модуле

### Tests

#### Unit Tests

- **`RecordPrepaymentServiceTest`** - тесты сервиса:
    - Успешная запись предоплаты
    - Ошибка при неверном статусе аренды
    - Ошибка когда рассчитанная стоимость не установлена
    - Ошибка когда сумма меньше рассчитанной стоимости
- **`RentalTest`** - тесты доменной модели:
    - `isPrepaymentSufficient()` - различные сценарии валидации

#### WebMvc Tests

- **`RentalCommandControllerTest`** - параметризованные тесты:
    - Успешная запись предоплаты (201)
    - Негативные сценарии валидации (400):
        - Нулевая или отрицательная сумма
        - Отсутствующий способ оплаты
        - Отсутствующий или пустой оператор
    - Бизнес-валидация (422):
        - Сумма меньше рассчитанной стоимости

#### Component Tests

- **`rental.feature`** - Cucumber сценарии:
    - Успешная запись предоплаты с валидацией ответа
    - Отклонение предоплаты при недостаточной сумме
    - Валидация события `PaymentReceived` после успешной записи
    - Интеграция с активацией аренды

### Key Implementation Details

1. **Валидация суммы предоплаты:**
    - Логика валидации инкапсулирована в доменной модели `Rental.isPrepaymentSufficient()`
    - Исключения создаются через статический фабричный метод для правильного выбора сообщения

2. **Использование Money value object:**
    - Внутри модуля используется `Money` для работы с денежными суммами
    - В DTO ответов используется `BigDecimal` для совместимости с API
    - Конвертация выполняется через `MoneyMapper` в MapStruct мапперах

3. **Модульные границы:**
    - `PaymentMethod` перемещен в публичный пакет `finance` для использования в rental модуле
    - Коммуникация между модулями через `FinanceFacade` и `PaymentInfo` DTO

4. **События:**
    - После успешной записи предоплаты публикуется событие `PaymentReceived`
    - Событие содержит `paymentId`, `rentalId`, `amount`, `paymentType` (PREPAYMENT), `receivedAt`

## Files Changed

### Domain

- `rental/domain/model/Rental.java` - добавлен метод `isPrepaymentSufficient()`
- `rental/domain/exception/InsufficientPrepaymentException.java` - создано новое исключение

### Application

- `rental/application/usecase/RecordPrepaymentUseCase.java` - интерфейс use case
- `rental/application/service/RecordPrepaymentService.java` - реализация use case

### Web

- `rental/web/command/dto/RecordPrepaymentRequest.java` - DTO запроса
- `rental/web/command/dto/PrepaymentResponse.java` - DTO ответа
- `rental/web/command/mapper/RentalCommandMapper.java` - маппер с MoneyMapper
- `rental/web/command/RentalCommandController.java` - REST эндпоинт
- `rental/web/error/RentalRestControllerAdvice.java` - обработчик исключений

### Finance Module (Integration)

- `finance/PaymentMethod.java` - перемещен в публичный пакет
- `finance/FinanceFacade.java` - метод `recordPrepayment()`
- `finance/FinanceFacadeImpl.java` - реализация метода

### Tests

- `rental/application/service/RecordPrepaymentServiceTest.java` - unit тесты
- `rental/domain/model/RentalTest.java` - тесты доменной модели
- `rental/web/command/RentalCommandControllerTest.java` - WebMvc тесты
- `component-test/features/rental/rental.feature` - компонентные тесты
- `component-test/steps/rental/RentalWebSteps.java` - step definitions
- `component-test/transformer/PrepaymentResponseTransformer.java` - transformer для Cucumber

## API Endpoint

```
POST /api/rentals/{id}/prepayments
Content-Type: application/json

Request Body:
{
  "amount": 100.00,
  "paymentMethod": "CASH",
  "operatorId": "operator-1"
}

Response 201:
{
  "paymentId": "uuid",
  "amount": 100.00,
  "paymentMethod": "CASH",
  "receiptNumber": "REC-20260210-001",
  "createdAt": "2026-02-10T10:30:00Z"
}

Response 422 (Insufficient prepayment):
{
  "title": "Insufficient prepayment",
  "detail": "Prepayment amount must be at least the estimated cost of the rental"
}
```

## Related Tasks

- [US-FN-001] Прием оплаты - Completed (используется для записи платежа)
- [US-RN-005] Запуск аренды - Depends on this task (требует предоплату)
- [TECH-006] Event-driven prepayment architecture - Technical improvement proposal

## Notes

- Предоплата записывается через finance модуль для обеспечения единой точки учета всех платежей
- Валидация суммы выполняется на уровне доменной модели для обеспечения бизнес-правил
- Событие `PaymentReceived` публикуется для возможной интеграции с другими модулями
- Оператор обязателен для всех предоплат для аудита операций
