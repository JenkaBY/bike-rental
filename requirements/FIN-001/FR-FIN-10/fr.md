# Customer Balance Withdrawal

## User Story

**As a** staff member,  
**I want to** record a customer's request to withdraw funds from their available wallet balance in cash,  
**So that** customers can retrieve funds they no longer wish to keep on account without waiting for a rental to
complete.

---

## Acceptance Criteria

### Scenario 1: Successful cash withdrawal

- **Given** a customer whose available balance is greater than or equal to the requested withdrawal amount
- **When** a staff member records a withdrawal with a valid positive amount and selects CASH as the payout method
- **Then** the customer's available balance decreases by the withdrawn amount
- **And** a double-entry journal record is created: CUSTOMER_WALLET (debit) → CASH (credit)
- **And** the transaction appears in the customer's history with type WITHDRAWAL, the amount, payout method CASH, and
  the current timestamp

### Scenario 2: Partial withdrawal — less than full available balance

- **Given** a customer with an available balance of 100
- **When** a staff member records a withdrawal of 40
- **Then** the customer's available balance decreases to 60
- **And** the remaining 60 stays on the account

### Scenario 3: Withdrawal amount exceeds available balance — rejected

- **Given** a customer whose available balance is less than the requested withdrawal amount
- **When** a staff member attempts to record the withdrawal
- **Then** the operation is rejected with an "insufficient funds" error
- **And** the customer's balance is unchanged
- **And** no journal entry is created

### Scenario 4: Zero or negative amount is rejected

- **Given** any customer with a financial account
- **When** a staff member submits a withdrawal with an amount of zero or less
- **Then** the operation is rejected with a validation error

### Scenario 5: Withdrawal of on-hold funds is rejected

- **Given** a customer whose on-hold balance is non-zero
- **When** a staff member attempts to record a withdrawal amount that would require consuming on-hold funds
- **Then** the operation is rejected — only available (non-hold) balance can be withdrawn
- **And** the customer's balances are unchanged

### Scenario 6: Withdrawal for a non-existent customer is rejected

- **Given** no customer exists for the provided identifier
- **When** a staff member attempts to record a withdrawal
- **Then** the operation is rejected with a clear "customer not found" error

---

## Business Rules

| Rule ID | Description                                                                                                                   |
|---------|-------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | Withdrawal amount must be a positive value greater than zero.                                                                 |
| BR-02   | Only the available (non-held) balance may be withdrawn — on-hold funds are excluded.                                          |
| BR-03   | Withdrawal amount may not exceed the customer's available balance; no overdraft is allowed.                                   |
| BR-04   | Partial withdrawals are permitted: the customer may withdraw any valid amount up to their available balance.                  |
| BR-05   | The only accepted payout method is CASH.                                                                                      |
| BR-06   | Every withdrawal creates exactly one double-entry journal record: CUSTOMER_WALLET (debit) → selected payout account (credit). |
| BR-07   | The payout method is stored on the transaction record for audit purposes.                                                     |
| BR-08   | The transaction record must capture the staff member's identity for accountability.                                           |

---

## Data / Payload Reference

| Field         | Required | Notes                                                                  |
|---------------|----------|------------------------------------------------------------------------|
| Customer ID   | Yes      | Identifies the customer making the withdrawal                          |
| Amount        | Yes      | Positive decimal value; must not exceed available balance              |
| Payout Method | Yes      | Always CASH in this version                                            |
| Withdrawn At  | Yes      | Date and time of the withdrawal (defaults to now, adjustable by staff) |
| Note          | No       | Optional free-text note from staff                                     |

---

## Out of Scope

- Cashless withdrawals via card terminal or bank transfer — cash-only in this version
- Customer-initiated self-service withdrawal (no customer-facing interface in this version)
- Withdrawing on-hold funds before a rental is completed or cancelled
- Automatic payouts or scheduled transfers
- Splitting a single withdrawal into multiple payout methods
