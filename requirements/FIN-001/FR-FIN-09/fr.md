# ~~Cashless Deposit (Card Terminal or Bank Transfer)~~ — Superseded by FR-FIN-02

> This story has been merged into **FR-FIN-02 (Customer Balance Deposit)**, which now covers all payment methods: CASH,
> CARD_TERMINAL, and BANK_TRANSFER.

## User Story

**As a** staff member,  
**I want to** record a cashless payment received from a customer (via card terminal or bank transfer) and credit it to
their wallet,  
**So that** customers who do not pay with cash can still top up their balance and use the rental service.

---

## Acceptance Criteria

### Scenario 1: Successful card terminal deposit

- **Given** a customer with an existing financial account
- **When** a staff member records a deposit with a valid positive amount and selects CARD_TERMINAL as the payment method
- **Then** the customer's available wallet balance increases by the deposited amount
- **And** a double-entry journal record is created: CARD_TERMINAL (debit) → CUSTOMER_WALLET (credit)
- **And** the transaction appears in the customer's history with type DEPOSIT, the amount, the payment method
  CARD_TERMINAL, and the current timestamp

### Scenario 2: Successful bank transfer deposit

- **Given** a customer with an existing financial account
- **When** a staff member records a deposit with a valid positive amount and selects BANK_TRANSFER as the payment method
- **Then** the customer's available wallet balance increases by the deposited amount
- **And** a double-entry journal record is created: BANK_TRANSFER (debit) → CUSTOMER_WALLET (credit)
- **And** the transaction appears in the customer's history with type DEPOSIT, the amount, the payment method
  BANK_TRANSFER, and the current timestamp

### Scenario 3: Zero or negative amount is rejected

- **Given** a customer with an existing financial account
- **When** a staff member attempts to record a cashless deposit with an amount of zero or less
- **Then** the operation is rejected
- **And** the customer's balance is unchanged
- **And** no journal entry is created

### Scenario 4: Deposit for a non-existent customer is rejected

- **Given** no customer exists for the provided identifier
- **When** a staff member attempts to record a cashless deposit
- **Then** the operation is rejected with a clear "customer not found" error

### Scenario 5: Deposit with an unrecognised payment method is rejected

- **Given** a valid customer and a positive amount
- **When** a staff member submits a deposit with a payment method that is not CARD_TERMINAL or BANK_TRANSFER
- **Then** the operation is rejected with a validation error
- **And** no journal entry is created

---

## Business Rules

| Rule ID | Description                                                                                                                                      |
|---------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | Deposit amount must be a positive value greater than zero.                                                                                       |
| BR-02   | Accepted cashless payment methods are: CARD_TERMINAL and BANK_TRANSFER.                                                                          |
| BR-03   | Each method uses its own dedicated ledger account: CARD_TERMINAL or BANK_TRANSFER (debit side).                                                  |
| BR-04   | Every cashless deposit creates exactly one double-entry journal record: the corresponding cashless account (debit) and CUSTOMER_WALLET (credit). |
| BR-05   | The payment method is stored on the transaction record for audit and reporting purposes.                                                         |
| BR-06   | Deposit and rental creation are always separate operations — a deposit cannot simultaneously start a rental.                                     |
| BR-07   | The transaction record must capture the staff member's identity for audit purposes.                                                              |

---

## Data / Payload Reference

| Field          | Required | Notes                                                                          |
|----------------|----------|--------------------------------------------------------------------------------|
| Customer ID    | Yes      | Identifies the customer whose wallet is credited                               |
| Amount         | Yes      | Positive decimal value; the system default currency                            |
| Payment Method | Yes      | One of: CARD_TERMINAL, BANK_TRANSFER                                           |
| Received At    | Yes      | Date and time the payment was confirmed (defaults to now, adjustable by staff) |
| Note           | No       | Optional free-text note from staff                                             |

---

## Out of Scope

- Automatically initiating or verifying a real card or bank transaction with external payment processors — this is
  staff-recorded only (like cash)
- A single deposit spanning both card and bank transfer (one deposit = one method)
- Splitting a cashless payment across multiple customers
