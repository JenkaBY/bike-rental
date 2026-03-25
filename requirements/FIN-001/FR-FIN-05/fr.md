# Rental Payment Hold (Funds Reservation)

## User Story

**As a** staff member,  
**I want to** create a rental only when the customer has enough available funds to cover the estimated cost,  
**So that** the customer's funds are reserved for the duration of the rental and they cannot be inadvertently spent
elsewhere.

---

## Acceptance Criteria

### Scenario 1: Rental created successfully — sufficient funds

- **Given** a customer with an available balance greater than or equal to the estimated rental cost
- **And** the rental request includes the expected return time
- **When** the staff member submits the rental creation request
- **Then** the estimated cost is calculated as: tariff rate × (expected return time − start time)
- **And** the estimated cost is moved from the customer's available balance to their on-hold balance
- **And** the rental is created in PENDING status (transitioning to ACTIVE)
- **And** a double-entry journal record is created: CUSTOMER_WALLET (debit) → CUSTOMER_HOLD (credit)
- **And** the hold is linked to the rental's unique identifier for traceability

### Scenario 2: Rental rejected — insufficient funds

- **Given** a customer whose available balance is less than the estimated rental cost
- **When** the staff member submits the rental creation request
- **Then** the rental is rejected with an INSUFFICIENT_FUNDS error
- **And** no rental record is created
- **And** the customer's balances are unchanged
- **And** no journal entry is created

### Scenario 3: Concurrent rental requests do not allow double-spending

- **Given** a customer with an available balance equal to the cost of exactly one rental
- **When** two rental creation requests for that customer are submitted simultaneously
- **Then** exactly one rental is created and the funds are held
- **And** the second request is rejected with an INSUFFICIENT_FUNDS error

### Scenario 4: Rental with multiple equipment items

- **Given** a rental request containing more than one piece of equipment
- **When** the total estimated cost across all items is calculated
- **Then** the single combined estimated amount is held — not a separate hold per item
- **And** the rental is rejected if the total exceeds the available balance

---

## Business Rules

| Rule ID | Description                                                                                                                       |
|---------|-----------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | Estimated cost = sum of (tariff rate × expected duration) for all equipment items in the rental.                                  |
| BR-02   | Available balance must be greater than or equal to the estimated cost for the rental to proceed.                                  |
| BR-03   | The hold amount is exactly equal to the estimated cost — no buffer or markup is applied automatically.                            |
| BR-04   | The hold must be atomically linked to the rental ID so it can be released or captured later.                                      |
| BR-05   | The system must prevent concurrent rental requests from the same customer from jointly consuming more than the available balance. |
| BR-06   | If the hold cannot be placed (e.g., system error), the rental creation fails entirely — no partial state is persisted.            |

---

## Data / Payload Reference

| Field                | Required        | Notes                                                |
|----------------------|-----------------|------------------------------------------------------|
| Customer ID          | Yes             | Customer whose funds are being held                  |
| Equipment Item(s)    | Yes             | One or more items with their tariff reference        |
| Expected Return Time | Yes             | Used to calculate estimated duration                 |
| Estimated Cost       | Yes (computed)  | Calculated by the system; not supplied by the caller |
| Rental ID            | Yes (generated) | Linked to the hold for future capture or release     |

---

## Out of Scope

- Partial holds across multiple customers (one rental always belongs to one customer)
- Allowing rental creation on credit or with a negative balance (no overdraft)
- Recalculating the hold if the expected return time is later modified — that is a future enhancement

