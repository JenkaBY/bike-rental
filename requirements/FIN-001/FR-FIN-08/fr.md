# Customer Transaction History

## User Story

**As a** staff member,  
**I want to** view a complete, chronological record of all financial movements for a customer,  
**So that** I can resolve disputes, verify payments, and provide full financial transparency during customer
interactions.

---

## Acceptance Criteria

### Scenario 1: Viewing full transaction history for a customer

- **Given** a customer with one or more financial transactions on record
- **When** a staff member requests the customer's transaction history
- **Then** the system returns a list of all transactions in descending chronological order (newest first)
- **And** each entry includes: transaction type, amount, timestamp, description or reference, and the running balance
  after that entry

### Scenario 2: Viewing history for a customer with no transactions

- **Given** a customer whose account has just been created and has no transactions
- **When** a staff member requests the transaction history
- **Then** an empty list is returned with no error

### Scenario 3: Filtering by date range

- **Given** a customer with transactions spanning multiple weeks
- **When** a staff member filters the history by a specific start date and end date
- **Then** only transactions within that date range (inclusive) are returned
- **And** the running balance shown per entry reflects the state at that point in time

### Scenario 4: Filtering by transaction type

- **Given** a customer with mixed transaction types (deposits, holds, captures, releases, adjustments)
- **When** a staff member filters by a specific type (e.g., DEPOSIT)
- **Then** only transactions of that type are returned

### Scenario 5: History for a non-existent customer

- **Given** no customer exists for the provided identifier
- **When** a staff member requests the history
- **Then** the system returns a clear "customer not found" error

---

## Business Rules

| Rule ID | Description                                                                                                                                   |
|---------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | Every financial operation (deposit, hold, capture, release, adjustment) must appear in the transaction history — no operation is ever silent. |
| BR-02   | Entries are immutable — once recorded, a transaction entry cannot be edited or deleted.                                                       |
| BR-03   | The running balance shown at each entry is derived from the ordered transaction history up to and including that entry.                       |
| BR-04   | The transaction history is the authoritative audit log — it must be consistent with the customer's current available and on-hold balances.    |
| BR-05   | Each entry must carry a reference to the originating business event (e.g., rental ID, deposit ID, adjustment ID) for traceability.            |

---

## Data / Payload Reference

| Field             | Required        | Notes                                                               |
|-------------------|-----------------|---------------------------------------------------------------------|
| Customer ID       | Yes             | The customer whose history is being retrieved                       |
| Transaction Type  | Yes (per entry) | One of: DEPOSIT, HOLD, CAPTURE, HOLD_RELEASE, ADJUSTMENT            |
| Amount            | Yes (per entry) | Positive for credits, negative for debits                           |
| Timestamp         | Yes (per entry) | UTC datetime of the transaction                                     |
| Description       | Yes (per entry) | Human-readable summary (e.g., "Hold placed for rental #ABC")        |
| Reference ID      | Yes (per entry) | ID of the originating business entity (rental ID, deposit ID, etc.) |
| Running Balance   | Yes (per entry) | Available balance after this transaction was applied                |
| Filter: Date From | No              | Start of date range filter (inclusive)                              |
| Filter: Date To   | No              | End of date range filter (inclusive)                                |
| Filter: Type      | No              | Filter by one transaction type                                      |

---

## Out of Scope

- Exporting transaction history to CSV or PDF — future enhancement
- Customer-facing self-service access to their own history — admin/staff only in this version
- Aggregate financial reports across all customers (e.g., daily revenue) — separate reporting feature
- Editing or reversing existing transaction entries — corrections go through FR-FIN-03 (Manual Adjustment)

