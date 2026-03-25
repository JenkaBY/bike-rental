# Customer Balance Deposit

## User Story

**As a** staff member,  
**I want to** record a payment received from a customer (cash, card terminal, or bank transfer) and credit it to their
wallet,  
**So that** the customer has funds available to pay for rentals regardless of how they prefer to pay.

---

## Acceptance Criteria

### Scenario 1: Successful cash deposit

- **Given** a customer with an existing financial account
- **When** a staff member records a deposit with a valid positive amount and selects CASH as the payment method
- **Then** the customer's available wallet balance increases by the deposited amount
- **And** a double-entry journal record is created: CASH (debit) → CUSTOMER_WALLET (credit)
- **And** the transaction appears in the customer's transaction history with type DEPOSIT, the amount, the payment
  method CASH, and the current timestamp

### Scenario 2: Successful card terminal deposit

- **Given** a customer with an existing financial account
- **When** a staff member records a deposit with a valid positive amount and selects CARD_TERMINAL as the payment method
- **Then** the customer's available wallet balance increases by the deposited amount
- **And** a double-entry journal record is created: CARD_TERMINAL (debit) → CUSTOMER_WALLET (credit)
- **And** the transaction appears in the customer's history with type DEPOSIT, the amount, the payment method
  CARD_TERMINAL, and the current timestamp

### Scenario 3: Successful bank transfer deposit

- **Given** a customer with an existing financial account
- **When** a staff member records a deposit with a valid positive amount and selects BANK_TRANSFER as the payment method
- **Then** the customer's available wallet balance increases by the deposited amount
- **And** a double-entry journal record is created: BANK_TRANSFER (debit) → CUSTOMER_WALLET (credit)
- **And** the transaction appears in the customer's history with type DEPOSIT, the amount, the payment method
  BANK_TRANSFER, and the current timestamp

### Scenario 4: Zero or negative deposit is rejected

- **Given** a customer with an existing financial account
- **When** a staff member attempts to record a deposit with an amount of zero or less
- **Then** the operation is rejected
- **And** the customer's balance is unchanged
- **And** no journal entry is created

### Scenario 5: Deposit for a non-existent customer is rejected

- **Given** no customer exists for the provided identifier
- **When** a staff member attempts to record a deposit
- **Then** the operation is rejected with a clear "customer not found" error

### Scenario 6: Deposit with an unrecognised payment method is rejected

- **Given** a valid customer and a positive amount
- **When** a staff member submits a deposit with a payment method that is not CASH, CARD_TERMINAL, or BANK_TRANSFER
- **Then** the operation is rejected with a validation error
- **And** no journal entry is created

---

## Business Rules

| Rule ID | Description                                                                                                                            |
|---------|----------------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | Deposit amount must be a positive value greater than zero.                                                                             |
| BR-02   | Accepted payment methods are: CASH, CARD_TERMINAL, and BANK_TRANSFER.                                                                  |
| BR-03   | Each payment method uses its own dedicated ledger account as the debit side of the journal entry.                                      |
| BR-04   | Every deposit creates exactly one double-entry journal record: the selected payment method account (debit) → CUSTOMER_WALLET (credit). |
| BR-05   | The payment method is stored on the transaction record for audit and reporting purposes.                                               |
| BR-06   | Deposit and rental creation are always separate operations — a deposit cannot simultaneously start a rental.                           |
| BR-07   | The transaction record must capture the staff member's identity for audit purposes.                                                    |

---

## Data / Payload Reference

| Field          | Required | Notes                                                                         |
|----------------|----------|-------------------------------------------------------------------------------|
| Customer ID    | Yes      | Identifies the customer whose wallet is credited                              |
| Amount         | Yes      | Positive decimal value; the system default currency                           |
| Payment Method | Yes      | One of: CASH, CARD_TERMINAL, BANK_TRANSFER                                    |
| Received At    | Yes      | Date and time the payment was received (defaults to now, adjustable by staff) |
| Note           | No       | Optional free-text note from staff (e.g., "advance for tomorrow's booking")   |

---

## Out of Scope

- Automatically initiating or verifying a real card or bank transaction with external payment processors — this is
  staff-recorded only
- A single deposit spanning multiple payment methods (one deposit = one method)
- Splitting a single payment across multiple customers
- Partial deposit validation against rental cost — deposit is independent of any future rental

