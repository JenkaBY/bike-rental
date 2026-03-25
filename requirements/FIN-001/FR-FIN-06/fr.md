# Payment Capture on Rental Return

## User Story

**As a** staff member,  
**I want to** settle the exact cost of a rental when the equipment is returned,  
**So that** the customer is charged precisely for the time they used the equipment and any unused held funds are
immediately returned to their wallet.

---

## Acceptance Criteria

### Scenario 1: Actual cost equals the held amount (exact match)

- **Given** a rental with an active hold equal to the actual rental cost
- **When** the equipment is returned and the rental is marked as completed
- **Then** the entire held amount is captured: CUSTOMER_HOLD (debit) → REVENUE (credit)
- **And** the customer's on-hold balance returns to zero for this rental
- **And** the customer's available balance is unchanged

### Scenario 2: Actual cost is less than the held amount (change scenario)

- **Given** a rental where the actual cost is less than the amount that was held
- **When** the equipment is returned and the rental is marked as completed
- **Then** the actual cost is captured: CUSTOMER_HOLD (debit, actual cost) → REVENUE (credit)
- **And** the remainder is released back to the wallet: CUSTOMER_HOLD (debit, remainder) → CUSTOMER_WALLET (credit)
- **And** all movements are recorded as separate, linked double-entry journal entries

### Scenario 3: Actual cost exceeds the held amount — available balance covers the difference

- **Given** a rental that ran over the expected return time, making the actual cost exceed the held amount
- **And** the customer has sufficient available balance to cover the shortfall
- **When** the equipment is returned and the rental is marked as completed
- **Then** the entire hold is captured: CUSTOMER_HOLD (debit) → REVENUE (credit)
- **And** the shortfall is debited from the available wallet: CUSTOMER_WALLET (debit) → REVENUE (credit)
- **And** both journal entries are recorded and linked to the rental

### Scenario 4: Actual cost exceeds the held amount — available balance is insufficient (DEBT)

- **Given** a rental that ran over time and the shortfall exceeds the customer's available balance
- **When** the equipment is returned and the rental is marked as completed
- **Then** the entire hold is captured: CUSTOMER_HOLD (debit) → REVENUE (credit)
- **And** the customer's remaining available balance (if any) is also captured: CUSTOMER_WALLET (debit) → REVENUE (
  credit)
- **And** the rental is flagged with status DEBT, recording the outstanding unpaid amount
- **And** the customer's available balance is set to zero (not negative)
- **And** the debt amount is visible to staff for manual resolution

### Scenario 5: Capture is idempotent

- **Given** a rental that has already been completed and settled
- **When** a duplicate return/complete request is received
- **Then** no additional journal entries are created
- **And** the rental status remains COMPLETED (or DEBT)

---

## Business Rules

| Rule ID | Description                                                                                                          |
|---------|----------------------------------------------------------------------------------------------------------------------|
| BR-01   | Actual and planned cost is calculated by tariff module. Out of scope                                                 |
| BR-02   | The held amount is always fully consumed first before the available balance is touched for overrun amounts.          |
| BR-03   | The customer's available balance must never go below zero — any unpaid remainder becomes a recorded DEBT.            |
| BR-04   | All capture and release movements must be recorded as separate, linked double-entry journal entries.                 |
| BR-05   | A DEBT record must capture: rental ID, customer ID, outstanding amount, and the timestamp when the debt was created. |
| BR-06   | Capture operations are idempotent — repeating a completed rental's return does not create duplicate charges.         |
| BR-07   | Excess hold (change) is returned to the available wallet automatically — no staff action is required.                |

---

## Data / Payload Reference

| Field            | Required        | Notes                                                            |
|------------------|-----------------|------------------------------------------------------------------|
| Rental ID        | Yes             | Identifies the rental being completed                            |
| Return Time      | Yes             | Actual time equipment was returned (used to compute actual cost) |
| Actual Cost      | Yes (computed)  | Calculated by the system from tariff and actual duration         |
| Hold Amount      | Yes (retrieved) | The amount previously reserved at rental creation                |
| Shortfall Amount | Conditional     | Present only when actual cost > hold amount                      |
| Debt Amount      | Conditional     | Present only when available balance is also insufficient         |

---

## Out of Scope

- Waiving debt or writing off outstanding amounts — that is a manual admin adjustment (FR-FIN-03)
- Automated notifications to the customer about debt
- Interest or late fees on overdue debt

