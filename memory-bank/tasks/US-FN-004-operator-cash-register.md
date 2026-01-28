# [US-FN-004] - Касса оператора (Operator Cash Register)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** Medium  
**Module:** finance  
**Dependencies:** US-FN-001, US-FN-002

## Original Request

**Как** Оператор проката / Администратор  
**Я хочу** вести кассу для каждого оператора  
**Чтобы** контролировать наличные средства и сверять смены

## User Story Details

**Описание:**  
Система должна вести кассу для каждого оператора (наличные средства).

**Операции:**

- Открытие смены (начальная сумма)
- Прием наличных
- Выдача наличных (возвраты)
- Изъятие наличных (инкассация)
- Закрытие смены

**Критерии приемки:**

- Учет всех наличных операций
- Автоматический расчет остатка кассы
- Сверка фактических и учетных данных
- Акт приема-передачи смены

**Связанные требования:** FR-FN-004

## Thought Process

Cash register management tracks physical cash flow per operator. Essential for cash reconciliation and shift handover.

**Key Features:**

1. **Shift Management**: Open/close shifts with operator assignment
2. **Cash Operations**: Track all cash in/out during shift
3. **Balance Tracking**: Running balance throughout shift
4. **Reconciliation**: Compare expected vs actual at shift end
5. **Audit Trail**: Complete history of cash movements

**Domain Model:**

```
CashRegister (Aggregate Root)
├── id: UUID
├── operatorId: UUID
├── shiftStartedAt: LocalDateTime
├── shiftClosedAt: LocalDateTime
├── openingBalance: Money
├── closingBalance: Money
├── expectedBalance: Money (calculated)
├── discrepancy: Money (actual - expected)
└── transactions: List<CashTransaction>
```

**Operations:**

- Cash received from payments (CASH_IN)
- Cash paid for refunds (CASH_OUT)
- Cash removal for collection (COLLECTION)

## Implementation Plan

- [ ] Create CashRegister domain model
- [ ] Implement shift management (open/close)
- [ ] Track cash transactions automatically
- [ ] Calculate running balance
- [ ] Implement reconciliation logic
- [ ] Create REST endpoints
- [ ] Add database migration
- [ ] Listen to payment/refund events
- [ ] Generate shift report
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 4.1 | Create domain model        | Not Started | 2026-01-26 |       |
| 4.2 | Implement shift management | Not Started | 2026-01-26 |       |
| 4.3 | Track cash transactions    | Not Started | 2026-01-26 |       |
| 4.4 | Implement reconciliation   | Not Started | 2026-01-26 |       |
| 4.5 | Create REST endpoints      | Not Started | 2026-01-26 |       |
| 4.6 | Create tests               | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoints:**

- `POST /api/cash-register/open` - Open shift
- `POST /api/cash-register/close` - Close shift
- `GET /api/cash-register/current` - Get current shift
- `POST /api/cash-register/collect` - Record cash collection
- `GET /api/cash-register/shifts` - Get shift history

**Domain Model:**

```java
public class CashRegister {
    private UUID id;
    private UUID operatorId;
    private LocalDateTime shiftStartedAt;
    private LocalDateTime shiftClosedAt;
    private Money openingBalance;
    private Money closingBalance;
    private List<CashTransaction> transactions;

    public void openShift(UUID operatorId, Money openingBalance) {
        this.operatorId = operatorId;
        this.shiftStartedAt = LocalDateTime.now();
        this.openingBalance = openingBalance;
        this.transactions = new ArrayList<>();
    }

    public void addCashTransaction(CashTransactionType type, Money amount, String reference) {
        transactions.add(new CashTransaction(type, amount, reference, LocalDateTime.now()));
    }

    public Money calculateExpectedBalance() {
        Money balance = openingBalance;
        for (CashTransaction tx : transactions) {
            balance = tx.type().adjustBalance(balance, tx.amount());
        }
        return balance;
    }

    public void closeShift(Money actualClosingBalance) {
        this.shiftClosedAt = LocalDateTime.now();
        this.closingBalance = actualClosingBalance;

        Money expected = calculateExpectedBalance();
        Money discrepancy = actualClosingBalance.subtract(expected);

        registerEvent(new ShiftClosed(id, operatorId, expected, actualClosingBalance, discrepancy));
    }
}
```

**Event Handlers:**

```java

@EventListener
public void onPaymentReceived(PaymentReceived event) {
    if (event.paymentMethod() == PaymentMethod.CASH) {
        CashRegister register = getCurrentShift(event.operatorId());
        register.addCashTransaction(
                CashTransactionType.CASH_IN,
                event.amount(),
                "Payment: " + event.paymentId()
        );
    }
}

@EventListener
public void onRefundIssued(RefundIssued event) {
    if (event.refundMethod() == PaymentMethod.CASH) {
        CashRegister register = getCurrentShift(event.operatorId());
        register.addCashTransaction(
                CashTransactionType.CASH_OUT,
                event.amount(),
                "Refund: " + event.refundId()
        );
    }
}
```

**Database Schema:**

```sql
CREATE TABLE cash_registers
(
    id               UUID PRIMARY KEY,
    operator_id      UUID           NOT NULL REFERENCES app_users (id),
    shift_started_at TIMESTAMP      NOT NULL,
    shift_closed_at  TIMESTAMP,
    opening_balance  DECIMAL(10, 2) NOT NULL,
    closing_balance  DECIMAL(10, 2),
    created_at       TIMESTAMP      NOT NULL
);

CREATE TABLE cash_transactions
(
    id               UUID PRIMARY KEY,
    cash_register_id UUID           NOT NULL REFERENCES cash_registers (id),
    transaction_type VARCHAR(20)    NOT NULL,
    amount           DECIMAL(10, 2) NOT NULL,
    reference        VARCHAR(255),
    created_at       TIMESTAMP      NOT NULL
);

CREATE INDEX idx_cash_registers_operator ON cash_registers (operator_id);
CREATE INDEX idx_cash_registers_shift ON cash_registers (shift_started_at, shift_closed_at);
```

## References

- User Story File: [docs/tasks/us/US-FN-004/us-fn-004.md](../../../docs/tasks/us/US-FN-004/us-fn-004.md)
- Dependencies: US-FN-001, US-FN-002
- Used by: US-RP-003 (financial reconciliation)
