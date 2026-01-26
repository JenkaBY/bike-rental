# [US-FN-002] - Возврат средств (Refund Processing)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** finance  
**Dependencies:** US-FN-001

## Original Request

**Как** Оператор проката  
**Я хочу** обрабатывать возвраты денежных средств клиентам  
**Чтобы** корректно учитывать все финансовые операции

## User Story Details

**Описание:**  
Система должна обрабатывать возвраты денежных средств клиентам.

**Случаи возврата:**

- Отмена аренды в течение 10 минут
- Технические проблемы с оборудованием
- Ошибки оператора

**Критерии приемки:**

- Указание причины возврата
- Выбор способа возврата (соответствует способу оплаты)
- Формирование документа возврата
- Связь с исходной операцией оплаты

**Связанные требования:** FR-FN-002

## Thought Process

Refund processing is critical for maintaining accurate financial records. Key considerations:

1. **Financial Integrity**: Refunds must be properly linked to original payments
2. **Immutability**: Like payments, refunds are immutable (create new record, don't update)
3. **Payment Method Matching**: Refund method should match original payment method
4. **Reason Tracking**: Document why refund was issued for audit and analysis
5. **Event-Driven**: Refunds trigger events for other modules to react

**Business Rules:**

- Refund amount ≤ original payment amount
- Refund method typically matches payment method (cash→cash, card→card)
- Full or partial refunds supported
- Each refund links to specific payment

**Technical Approach:**

- Create separate Refund aggregate (not just negative payment)
- Link refund to original payment via paymentId
- Publish RefundIssued domain event
- Generate refund receipt/document
- Support multiple refunds per payment (if partial)

**Architecture Decisions:**

- Refund is its own aggregate, not a Payment with negative amount
- Validates that refund amount doesn't exceed remaining payment balance
- Event listeners in rental module may cancel/update rental on refund

## Implementation Plan

- [ ] Create Refund domain model
- [ ] Create RefundReason enum
- [ ] Implement refund repository
- [ ] Create ProcessRefundUseCase
- [ ] Implement validation (amount, payment exists, remaining balance)
- [ ] Create REST endpoint for refund processing
- [ ] Add database migration for refunds table
- [ ] Publish RefundIssued domain event
- [ ] Generate refund document/receipt
- [ ] Create component tests for refund scenarios
- [ ] Write unit tests for validation logic
- [ ] Write WebMvc tests for endpoint

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 2.1 | Create refund domain model | Not Started | 2026-01-26 |       |
| 2.2 | Implement refund use case  | Not Started | 2026-01-26 |       |
| 2.3 | Create REST endpoint       | Not Started | 2026-01-26 |       |
| 2.4 | Add validation logic       | Not Started | 2026-01-26 |       |
| 2.5 | Generate refund documents  | Not Started | 2026-01-26 |       |
| 2.6 | Publish domain events      | Not Started | 2026-01-26 |       |
| 2.7 | Create tests               | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-FN-001 completion
- Part of Phase 2: Basic Module Functions

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.finance
├── web.command
│   ├── RefundCommandController
│   ├── dto.ProcessRefundRequest
│   └── dto.RefundResponse
├── application
│   ├── usecase.ProcessRefundUseCase
│   └── service.RefundService
├── domain
│   ├── model.Refund
│   ├── model.RefundReason (enum)
│   ├── repository.RefundRepository
│   └── event.RefundIssued
└── infrastructure
    └── persistence
```

**API Endpoint:**

- `POST /api/refunds` - Process refund
- Request body:

```json
{
  "paymentId": "uuid",
  "amount": 500.00,
  "reason": "RENTAL_CANCELLED",
  "refundMethod": "CASH",
  "notes": "Cancelled within 10 minutes"
}
```

- Response: `201 Created` with refund details and receipt number

**Database Schema:**

```sql
CREATE TABLE refunds
(
    id             UUID PRIMARY KEY,
    payment_id     UUID               NOT NULL REFERENCES payments (id),
    amount         DECIMAL(10, 2)     NOT NULL,
    refund_reason  VARCHAR(50)        NOT NULL,
    refund_method  VARCHAR(50)        NOT NULL,
    notes          TEXT,
    created_at     TIMESTAMP          NOT NULL,
    operator_id    UUID,
    receipt_number VARCHAR(50) UNIQUE NOT NULL
);

CREATE INDEX idx_refunds_payment ON refunds (payment_id);
CREATE INDEX idx_refunds_created ON refunds (created_at);
```

**Domain Model:**

```java
public class Refund {
    private UUID id;
    private UUID paymentId;
    private Money amount;
    private RefundReason reason;
    private PaymentMethod refundMethod;
    private String notes;
    private LocalDateTime createdAt;
    private UUID operatorId;
    private String receiptNumber;
}
```

**Enums:**

```java
public enum RefundReason {
    RENTAL_CANCELLED,      // Отмена аренды в течение 10 минут
    EQUIPMENT_ISSUE,       // Технические проблемы с оборудованием
    OPERATOR_ERROR,        // Ошибки оператора
    OVERPAYMENT,          // Переплата
    OTHER
}
```

**Domain Event:**

```java
record RefundIssued(
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        RefundReason reason,
        LocalDateTime issuedAt
) {
}
```

**Validation Rules:**

- Payment must exist and not be already fully refunded
- Refund amount must be positive and ≤ (payment amount - already refunded amount)
- Refund method should typically match payment method (warning if different)

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-FN-002/us-fn-002.md](../../../docs/tasks/us/US-FN-002/us-fn-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-FN-001 (Payment acceptance)
- Used by: US-RN-008 (Early return/equipment replacement)
- Used by: US-TR-005 (Refund on cancellation)
