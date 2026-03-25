# Balance Inquiry

## User Story

**As a** staff member,  
**I want to** look up a customer's current financial position at any time,  
**So that** I can confirm they have enough funds before advising them, resolve disputes, and provide accurate
information.

---

## Acceptance Criteria

### Scenario 1: Successful balance inquiry

- **Given** a customer with an existing financial account
- **When** a staff member requests the customer's balance
- **Then** the system returns three values:
    - **Available balance** — funds the customer can spend right now
    - **On-hold balance** — funds reserved for active rentals
    - **Total balance** — available + on-hold
- **And** the values are consistent and current at the time of the request

### Scenario 2: Balance inquiry for a customer with no transactions

- **Given** a customer whose account was just created (zero balance)
- **When** a staff member requests the balance
- **Then** all three values are returned as zero

### Scenario 3: Balance inquiry for a non-existent customer

- **Given** no customer exists for the provided identifier
- **When** a staff member requests the balance
- **Then** the system returns a clear "customer not found" error

### Scenario 4: Consistency during concurrent operations

- **Given** a customer balance is being modified by a concurrent transaction (e.g., rental hold being placed)
- **When** a staff member requests the balance at the same moment
- **Then** the returned values reflect either the state before or fully after the concurrent operation — never a
  partial/torn read

---

## Business Rules

| Rule ID | Description                                                                                               |
|---------|-----------------------------------------------------------------------------------------------------------|
| BR-01   | Total balance is always the sum of available balance and on-hold balance.                                 |
| BR-02   | Available balance can never be negative.                                                                  |
| BR-03   | On-hold balance can never be negative.                                                                    |
| BR-04   | Balance reads must be consistent — concurrent writes must not result in a torn or incorrect balance view. |

---

## Data / Payload Reference

| Field             | Required       | Notes                                       |
|-------------------|----------------|---------------------------------------------|
| Customer ID       | Yes            | Identifier of the customer being queried    |
| Available Balance | Yes (response) | Funds spendable immediately                 |
| On-Hold Balance   | Yes (response) | Funds reserved for active rentals           |
| Total Balance     | Yes (response) | Available + On-Hold                         |
| As-Of Timestamp   | Yes (response) | The moment in time the balance was computed |

---

## Out of Scope

- Historical balance snapshots (point-in-time balance for a past date) — covered by FR-FIN-08 (Transaction History)
- Aggregate balance across multiple customers
- Balance alerts or low-balance notifications

