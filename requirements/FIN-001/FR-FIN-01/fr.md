# Automatic Financial Account Provisioning

## User Story

**As a** system,  
**I want to** automatically create a financial account for every new customer at the moment of registration,  
**So that** the customer is immediately ready to make deposits and pay for rentals without any additional staff action.

---

## Acceptance Criteria

### Scenario 1: Account created on customer registration

- **Given** a new customer is successfully registered in the system
- **When** the customer registration is confirmed
- **Then** a financial account is automatically created for that customer
- **And** the account has an available balance of zero and an on-hold amount of zero

### Scenario 2: Duplicate account creation is prevented

- **Given** a customer already has a financial account
- **When** the system attempts to create another account for the same customer
- **Then** the operation is rejected silently (no duplicate account is created)
- **And** the existing account is left unchanged

### Scenario 3: Account creation failure does not silently succeed

- **Given** a new customer is being registered
- **When** the financial account cannot be created (e.g., system error)
- **Then** the overall customer registration is treated as failed
- **And** no partial customer record is persisted

---

## Business Rules

| Rule ID | Description                                                                                      |
|---------|--------------------------------------------------------------------------------------------------|
| BR-01   | Every customer must have exactly one financial account — no more, no fewer.                      |
| BR-02   | A financial account is always initialised with zero available balance and zero on-hold balance.  |
| BR-03   | Account provisioning is an internal, automated step — it is never triggered manually by staff.   |
| BR-04   | Account creation is atomic with customer registration — either both succeed or neither persists. |

---

## Data / Payload Reference

| Field             | Required | Notes                                                |
|-------------------|----------|------------------------------------------------------|
| Customer ID       | Yes      | Links the financial account to the customer identity |
| Available Balance | Yes      | Initialised to 0                                     |
| On-Hold Balance   | Yes      | Initialised to 0                                     |
| Created At        | Yes      | Timestamp of account creation (UTC)                  |

---

## Out of Scope

- Staff-facing UI for account creation — this is fully automated
- Assigning an opening balance at registration
- Account types for non-customer entities (e.g., shop accounts — those are system-managed ledger accounts)

