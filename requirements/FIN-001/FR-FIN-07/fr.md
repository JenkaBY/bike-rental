# Hold Release on Rental Cancellation

## User Story

**As a** staff member,  
**I want to** cancel a rental and have the held funds automatically returned to the customer's available wallet,  
**So that** the customer is not left with funds locked against a rental that will never happen.

---

## Acceptance Criteria

### Scenario 1: Successful hold release on cancellation

- **Given** a rental in PENDING or ACTIVE status with an associated hold
- **When** the rental is cancelled
- **Then** the full held amount is released back to the customer's available balance
- **And** a double-entry journal record is created: CUSTOMER_HOLD (debit) → CUSTOMER_WALLET (credit)
- **And** the rental status changes to CANCELLED
- **And** no revenue is captured

### Scenario 2: Cancellation of a rental with no hold (edge case)

- **Given** a rental in PENDING status where the hold was never successfully placed
- **When** the rental is cancelled
- **Then** the cancellation succeeds
- **And** no journal entry is created (there are no funds to release)
- **And** the customer's balances remain unchanged

### Scenario 3: Cancellation rejected for rentals not in a cancellable state

- **Given** a rental that is already in CANCELLED, COMPLETED, or DEBT status
- **When** a cancellation request is submitted
- **Then** the request is rejected with a 422 Unprocessable Entity error indicating the rental is not in a cancellable
  state
- **And** no funds movement occurs
- **And** no journal entry is created

---

## Business Rules

| Rule ID | Description                                                                                                                                            |
|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | The full hold amount (as originally placed at rental creation) is released — no partial release on cancellation.                                       |
| BR-02   | No revenue is recorded for a cancelled rental.                                                                                                         |
| BR-03   | Only rentals in PENDING or ACTIVE status can be cancelled — CANCELLED, COMPLETED, and DEBT rentals are rejected with a 422 Unprocessable Entity error. |
| BR-04   | The release journal entry must reference the rental ID for full traceability.                                                                          |

---

## Data / Payload Reference

| Field           | Required       | Notes                                         |
|-----------------|----------------|-----------------------------------------------|
| Rental ID       | Yes            | Identifies the rental being cancelled         |
| Released Amount | Yes (computed) | The hold amount originally placed at creation |
| Cancelled At    | Yes            | Timestamp of cancellation                     |
| Cancelled By    | Yes            | Staff member identity                         |

---

## Out of Scope

- Partial cancellations (cancelling only one item within a multi-item rental) — treated as a future enhancement
- Automatic cancellation due to timeout — that is a separate operational process
- Cancellation fees or penalties

