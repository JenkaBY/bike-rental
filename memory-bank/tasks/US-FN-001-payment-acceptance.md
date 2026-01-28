# [US-FN-001] - Прием оплаты (Payment Acceptance)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
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
├── rentalId: UUID (nullable)
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

- [ ] Create Payment domain model with value objects
- [ ] Create PaymentType and PaymentMethod enums
- [ ] Implement payment repository
- [ ] Create RecordPaymentUseCase
- [ ] Implement receipt generation service
- [ ] Create REST endpoint for payment recording
- [ ] Add database migration for payments table
- [ ] Publish PaymentReceived domain event
- [ ] Create component tests for payment scenarios
- [ ] Write unit tests for payment logic
- [ ] Write WebMvc tests for endpoints
- [ ] Implement receipt number generation strategy

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                  | Status      | Updated    | Notes |
|-----|------------------------------|-------------|------------|-------|
| 1.1 | Create domain model          | Not Started | 2026-01-26 |       |
| 1.2 | Implement payment use case   | Not Started | 2026-01-26 |       |
| 1.3 | Create REST endpoint         | Not Started | 2026-01-26 |       |
| 1.4 | Add database migration       | Not Started | 2026-01-26 |       |
| 1.5 | Implement receipt generation | Not Started | 2026-01-26 |       |
| 1.6 | Publish domain events        | Not Started | 2026-01-26 |       |
| 1.7 | Create component tests       | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, independent task that can be started after US-CL-002

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
- Request body: `{ "rentalId": "uuid", "amount": number, "paymentType": "string", "paymentMethod": "string" }`
- Response: `201 Created` with payment details and receipt number

**Database Schema:**

```sql
CREATE TABLE payments
(
    id             UUID PRIMARY KEY,
    rental_id      UUID,
    amount         DECIMAL(10, 2)     NOT NULL,
    payment_type   VARCHAR(50)        NOT NULL,
    payment_method VARCHAR(50)        NOT NULL,
    created_at     TIMESTAMP          NOT NULL,
    operator_id    UUID,
    receipt_number VARCHAR(50) UNIQUE NOT NULL
);

CREATE INDEX idx_payments_rental ON payments (rental_id);
CREATE INDEX idx_payments_created ON payments (created_at);
CREATE INDEX idx_payments_receipt ON payments (receipt_number);
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
        UUID rentalId,
        BigDecimal amount,
        PaymentType paymentType,
        LocalDateTime receivedAt
) {
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-FN-001/us-fn-001.md](../../../docs/tasks/us/US-FN-001/us-fn-001.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Critical dependency for: US-RN-004, US-FN-002, US-FN-003
