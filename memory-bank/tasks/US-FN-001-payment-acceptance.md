# [US-FN-001] - Прием оплаты (Payment Acceptance)

**Status:** Completed  
**Added:** 2026-01-21  
**Updated:** 2026-02-04  
**Priority:** High  
**Module:** finance  
**Dependencies:** None

## Original Request

**Как** Оператор проката  
**Я хочу** фиксировать все платежи от клиентов  
**Чтобы** вести учет финансовых операций

## User Story Details

**Описание:**  
Система должна фиксировать все платежи от клиентов.

**Типы платежей:**

- Предоплата при оформлении аренды
- Доплата при возврате
- Оплата аксессуаров/дополнительных услуг

**Способы оплаты:**

- Наличные
- Банковская карта
- Электронные платежи (QR-код)

**Критерии приемки:**

- Выбор способа оплаты
- Ввод суммы
- Формирование чека
- Сохранение в журнале операций

**Связанные требования:** FR-FN-001

## Thought Process

The finance module is fundamental for tracking all monetary transactions. Key considerations:

1. **Payment Types**: Different contexts (prepayment, additional payment, accessories)
2. **Payment Methods**: Multiple methods must be supported
3. **Receipt Generation**: Need to generate receipts for all payments
4. **Immutability**: Payments should be immutable once created
5. **Event-Driven**: Other modules will trigger payment creation

**Architecture Decisions:**

- Payment is an aggregate root
- Payments are immutable (no updates, only refunds via new payment)
- Use PaymentType and PaymentMethod enums
- Payment events notify other modules
- Link to rental via rentalId (optional, can be null for direct sales)

**Domain Model:**

```
Payment (Aggregate Root)
├── id: UUID
├── rentalId: Long (nullable)
├── amount: Money
├── paymentType: PaymentType (enum)
├── paymentMethod: PaymentMethod (enum)
├── createdAt: LocalDateTime
├── operatorId: UUID
└── receiptNumber: String
```

**Events:**

- `PaymentReceived` - When payment is recorded
- `PaymentRefunded` - When refund is processed

## Implementation Plan

- [x] Create database migration for payments table
- [x] Create Payment domain model with Money value object
- [x] Create PaymentType and PaymentMethod enums
- [x] Create PaymentReceived domain event
- [x] Define PaymentRepository interface
- [x] Create JPA entity and mapper
- [x] Implement PaymentRepositoryAdapter
- [x] Create RecordPaymentUseCase interface and command
- [x] Implement RecordPaymentService with event publishing
- [x] Implement ReceiptNumberGenerationService
- [x] Create payment command/query mappers
- [x] Create REST DTOs (RecordPaymentRequest, PaymentResponse)
- [x] Implement PaymentCommandController (POST)
- [x] Implement PaymentQueryController (GET by id, GET by rental id)
- [x] Write unit tests (Payment, services, mappers) - 15-20 tests
- [x] Write WebMvc tests (validation scenarios) - 10-15 tests
- [x] Create component tests (Cucumber scenarios)

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID   | Description                  | Status   | Updated    | Notes                                                       |
|------|------------------------------|----------|------------|-------------------------------------------------------------|
| 1.1  | Create domain model          | Complete | 2026-02-03 | Payment, enums, events, repository + tests                  |
| 1.2  | Create infrastructure layer  | Complete | 2026-02-04 | JPA entity, mapper, adapter implemented                     |
| 1.3  | Implement use cases          | Complete | 2026-02-04 | RecordPaymentService, GetPayment services created and wired |
| 1.4  | Implement receipt generation | Complete | 2026-02-04 | ReceiptNumberGenerationService implemented                  |
| 1.5  | Create REST endpoints        | Complete | 2026-02-04 | Command and query controllers implemented                   |
| 1.6  | Add database migration       | Complete | 2026-02-04 | Liquibase changelog created                                 |
| 1.7  | Publish domain events        | Complete | 2026-02-04 | PaymentReceived event published on creation                 |
| 1.8  | Write unit tests             | Complete | 2026-02-04 | Unit tests added for domain, service, mappers               |
| 1.9  | Write WebMvc tests           | Complete | 2026-02-04 | WebMvc tests for validation and controllers                 |
| 1.10 | Create component tests       | Complete | 2026-02-04 | Cucumber scenarios and transformers added                   |

## Progress Log

### 2026-02-04

- Marked US-FN-001 - Payment Acceptance as Completed (100%).
- Finalized infrastructure: `PaymentJpaEntity`, `PaymentJpaRepository`, `PaymentJpaMapper`, and
  `PaymentRepositoryAdapter` implemented and verified.
- Implemented application layer use cases and services: `RecordPaymentService` (now using `UuidGenerator` port),
  `GetPaymentByIdService`, `GetPaymentsByRentalIdService`.
- Implemented `ReceiptNumberGenerationService` and integrated it into `RecordPaymentService`.
- Implemented web layer (CQRS): `PaymentCommandController` (POST /api/payments) and `PaymentQueryController` (GET
  /api/payments/{id}, GET /api/payments/by-rental/{rentalId}).
- Added WebMvc tests for command and query controllers; added unit tests for service and mappers. Added component (
  Cucumber) feature for payment flows.
- Implemented domain event publishing: `PaymentReceived` published after successful persistence; created listener stubs
  for downstream modules.
- Updated tests and fixed architecture issues: replaced direct UUID library use in application service with
  `UuidGenerator` port and adapter.
- Updated Liquibase changelog with `payments` table migration and indexes on `receipt_number`.

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.finance
├── web.command
│   ├── PaymentCommandController
│   ├── dto.RecordPaymentRequest
│   └── dto.PaymentResponse
├── application
│   ├── usecase.RecordPaymentUseCase
│   ├── service.PaymentService
│   └── service.ReceiptGenerationService
├── domain
│   ├── model.Payment
│   ├── model.vo.Money
│   ├── model.PaymentType (enum)
│   ├── model.PaymentMethod (enum)
│   ├── repository.PaymentRepository
│   └── event.PaymentReceived
└── infrastructure
    └── persistence
```

**API Endpoints:**

- `POST /api/payments` - Record payment
- Request body: `{ "rentalId": "int64", "amount": number, "paymentType": "string", "paymentMethod": "string" }`
- Response: `201 Created` with payment details and receipt number

**Database Schema:**

```sql
CREATE TABLE payments
(
    id             UUID PRIMARY KEY,
    rental_id biginteger,
    amount         DECIMAL(10, 2)     NOT NULL,
    payment_type   VARCHAR(50)        NOT NULL,
    payment_method VARCHAR(50)        NOT NULL,
    created_at     TIMESTAMP          NOT NULL,
    operator_id    UUID,
    receipt_number VARCHAR(50) UNIQUE NOT NULL
);
```

**Enums:**

```java
public enum PaymentType {
    PREPAYMENT,         // Предоплата
    ADDITIONAL_PAYMENT, // Доплата
    ACCESSORY,          // Аксессуары
    OTHER
}

public enum PaymentMethod {
    CASH,          // Наличные
    CARD,          // Банковская карта
    ELECTRONIC     // Электронные платежи (QR)
}
```

**Domain Event:**

```java
record PaymentReceived(
        UUID paymentId,
        Long rentalId,
        BigDecimal amount,
        PaymentType paymentType,
        LocalDateTime receivedAt
) {
}
```

## Steps

1. Database Layer (Liquibase migrations) - Create table structure for payments table with UUID primary key, rental_id
   reference, amount (DECIMAL 10,2), payment_type/method enums, created_at timestamp, operator_id, and unique
   receipt_number. Include indexes on receipt_number. Follow pattern from customers.create-table.xml.
2. Domain Layer (Aggregates & Value Objects) - Create Payment aggregate root with builder pattern, PaymentType enum (
   PREPAYMENT, ADDITIONAL_PAYMENT, ACCESSORY, OTHER), PaymentMethod enum (CASH, CARD, ELECTRONIC). Use existing Money
   value object from shared.domain.model.vo.Money. Create PaymentReceived domain event record. Define PaymentRepository
   interface with methods: save(Payment), findById(UUID), findByRentalId(Long), existsByReceiptNumber(String).
3. Infrastructure Layer (JPA & Adapters) - Create PaymentJpaEntity with JPA annotations and enum converters,
   PaymentJpaRepository extending JpaRepository<PaymentJpaEntity, UUID>, PaymentJpaMapper using MapStruct for domain↔JPA
   conversion with MoneyMapper integration. Implement PaymentRepositoryAdapter delegating to JPA repository. Pattern
   similar to equipment module implementation.
4. Application Layer (Use Cases & Services) - Create use case interfaces: RecordPaymentUseCase (with
   RecordPaymentCommand), GetPaymentByIdUseCase, GetPaymentsByRentalIdUseCase. Implement services:
   RecordPaymentService (validates receipt uniqueness, generates receipt number, saves payment, publishes
   PaymentReceived event), GetPaymentByIdService, GetPaymentsByRentalIdService. Create PaymentCommandToDomainMapper for
   command→domain mapping.
5. Web Layer (REST API with CQRS) - Create RecordPaymentRequest DTO with Jakarta validation (@NotNull, @Positive,
   @DecimalMin, @Size for receipt), PaymentResponse DTO. Implement PaymentCommandMapper (request→command) and
   PaymentQueryMapper (domain→response) using MapStruct. Create PaymentCommandController with POST /api/payments
   endpoint (returns 201), PaymentQueryController with GET /api/payments/{id} and GET /api/payments/by-rental/{rentalId}
   endpoints.
6. Receipt Generation Service - Implement ReceiptNumberGenerationService with strategy pattern (format: "
   RCP-{timestamp}-{random}", e.g., "RCP-20260203123045-A1B2"). Inject into RecordPaymentService. Add unit tests for
   uniqueness validation and format compliance.
7. Unit Tests (TDD First) - Write 15-20 unit tests: PaymentTest (domain validation, builder), MoneyTest (already
   exists), RecordPaymentServiceTest (successful recording, duplicate receipt validation, event publishing verification
   with Mockito), PaymentJpaMapperTest, enum tests. Follow CreateCustomerServiceTest structure with @ExtendWith(
   MockitoExtension.class) and BDD naming.
8. WebMvc Tests (Controller Validation) - Create PaymentCommandControllerTest and PaymentQueryControllerTest with nested
   classes (ShouldReturn201, ShouldReturn400, ShouldReturn404, ShouldReturn500). Test validation scenarios:
   null/negative amounts, invalid payment types/methods, missing required fields, unsupported media type. Pattern from
   TariffCommandControllerTest. 10-15 tests total.
9. Component Tests (Cucumber BDD) - Create payment.feature with scenarios: record payment with cash/card/electronic
   methods, retrieve payment by id, retrieve payments by rental id, fail on duplicate receipt number. Create
   PaymentWebSteps (REST interactions), PaymentDbSteps (database verification), PaymentRequestTransformer and
   PaymentJpaEntityTransformer for DataTable mapping. Follow tariff.feature pattern with transposed tables and Scenario
   Outlines.
10. Domain Event Publishing - Configure Spring ApplicationEventPublisher injection in RecordPaymentService. Publish
    PaymentReceived event after successful payment persistence. Create PaymentEventListener stub for future rental
    module integration (mark with TODO). Add unit test verifying event publication with @Mock ApplicationEventPublisher.

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-FN-001/us-fn-001.md](../../../docs/tasks/us/US-FN-001/us-fn-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Critical dependency for: US-RN-004, US-FN-002, US-FN-003
