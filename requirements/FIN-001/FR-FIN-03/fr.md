# Admin Manual Balance Adjustment

## User Story

**As an** admin,  
**I want to** manually credit or debit a customer's wallet balance with a written reason,  
**So that** I can correct errors, apply goodwill credits, or handle exceptional situations without workarounds.

---

## Acceptance Criteria

### Scenario 1: Successful positive (credit) adjustment

- **Given** a customer with an existing financial account
- **When** an admin applies a positive adjustment with a mandatory reason
- **Then** the customer's available wallet balance increases by the adjustment amount
- **And** a double-entry journal record is created: ADJUSTMENT account is debited and CUSTOMER_WALLET account is
  credited
- **And** the transaction appears in the customer's history with type ADJUSTMENT, the amount (positive), the reason, and
  the admin's identity

### Scenario 2: Successful negative (debit) adjustment with sufficient balance

- **Given** a customer whose available balance is greater than or equal to the adjustment amount
- **When** an admin applies a negative adjustment with a mandatory reason
- **Then** the customer's available wallet balance decreases by the adjustment amount
- **And** a double-entry journal record is created: CUSTOMER_WALLET is debited and ADJUSTMENT account is credited

### Scenario 3: Negative adjustment rejected when it would cause an overdraft

- **Given** a customer whose available balance is less than the requested debit amount
- **When** an admin attempts to apply a negative adjustment
- **Then** the operation is rejected with an "insufficient funds" error
- **And** the customer's balance is unchanged
- **And** no journal entry is created

### Scenario 4: Adjustment without a reason is rejected

- **Given** any customer
- **When** an admin submits an adjustment request without providing a reason
- **Then** the operation is rejected with a validation error

---

## Business Rules

| Rule ID | Description                                                                                           |
|---------|-------------------------------------------------------------------------------------------------------|
| BR-01   | A reason/note is mandatory for every manual adjustment — the field cannot be blank.                   |
| BR-02   | Negative adjustments may not reduce the available balance below zero (no overdraft).                  |
| BR-03   | On-hold funds are not affected by manual adjustments — only the available wallet balance is modified. |
| BR-04   | Every adjustment is recorded with the admin's identity for full accountability.                       |
| BR-05   | Adjustments are logged with double-entry: ADJUSTMENT account ↔ CUSTOMER_WALLET.                       |

---

## Data / Payload Reference

| Field       | Required | Notes                                                                     |
|-------------|----------|---------------------------------------------------------------------------|
| Customer ID | Yes      | The customer whose balance is being adjusted                              |
| Amount      | Yes      | Positive = credit, Negative = debit                                       |
| Reason      | Yes      | Mandatory free-text explanation (e.g., "Goodwill credit — bicycle fault") |
| Adjusted At | Yes      | Timestamp of the adjustment (defaults to now)                             |
| Admin ID    | Yes      | Identity of the admin performing the adjustment                           |

---

## Out of Scope

- Adjustments to the on-hold balance — holds are only created and released by the rental payment flow
- Bulk adjustments across multiple customers
- Approval workflows for large adjustments (future enhancement)

