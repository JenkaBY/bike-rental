# Partial Equipment Return

## User Story

**As a** staff member,  
**I want to** record the return of individual items within a multi-item rental without closing the rental,  
**So that** customers can give back equipment at different times and are charged accurately per item based on actual
usage.

---

## Acceptance Criteria

### Scenario 1: First item returned from a two-item rental

- **Given** an ACTIVE rental containing two pieces of equipment and an associated hold
- **When** a staff member records the return of one item
- **Then** that item is marked as returned with the return timestamp
- **And** the rental status remains ACTIVE
- **And** the full financial hold is preserved — no funds are moved
- **And** the remaining unreturned item is still tracked as active within the rental

### Scenario 2: Final item returned — full settlement triggered

- **Given** an ACTIVE rental with one remaining unreturned item (at least one item was previously partially returned)
- **When** a staff member records the return of the last item
- **Then** the rental is settled in full using per-item actual durations:
    - Each item's actual cost is calculated by tariff module
    - Total actual cost = sum of all per-item actual costs
- **And** the settlement follows the standard capture model from FR-FIN-05:
    - If total actual cost ≤ hold: capture total cost, release remainder to customer wallet
    - If total actual cost > hold but wallet covers shortfall: capture full hold, debit shortfall from wallet
    - If total actual cost > hold and wallet is insufficient: capture full hold, debit available wallet, flag rental as
      DEBT
- **And** the rental status changes to COMPLETED (or DEBT if applicable)

### Scenario 3: Single-item rental return — not a partial return

- **Given** an ACTIVE rental containing exactly one piece of equipment
- **When** a staff member records the return of that item
- **Then** the system treats this as a standard (non-partial) return
- **And** settlement proceeds immediately using the existing FR-FIN-05 flow

### Scenario 4: Returning an item already marked as returned — rejected

- **Given** an ACTIVE rental where a specific item has already been partially returned
- **When** a staff member attempts to record a return for the same item again
- **Then** the operation is rejected with a clear error indicating the item is already returned
- **And** no changes are made to the rental or the financial hold

### Scenario 5: Returning an item not included in the rental — rejected

- **Given** an ACTIVE rental
- **When** a staff member records a return for an equipment item that is not part of that rental
- **Then** the operation is rejected with a validation error
- **And** no changes are made to the rental or balances

### Scenario 6: Partial return on a non-ACTIVE rental — rejected

- **Given** a rental in COMPLETED, CANCELLED, or DEBT status
- **When** a staff member attempts to record a partial return
- **Then** the operation is rejected with a clear error indicating the rental is already finalised
- **And** no changes are made

### Scenario 7: Idempotent final return

- **Given** a rental that is already COMPLETED or DEBT after the last item was returned and settled
- **When** a duplicate final-return request is received
- **Then** no additional journal entries are created
- **And** the rental status remains unchanged

---

## Business Rules

| Rule ID | Description                                                                                                                                  |
|---------|----------------------------------------------------------------------------------------------------------------------------------------------|
| BR-01   | Each item in a multi-item rental tracks its own return timestamp independently.                                                              |
| BR-02   | The financial hold placed at rental creation is not adjusted during partial returns — it stays fully intact until the last item is returned. |
| BR-03   | The rental status remains ACTIVE throughout all partial returns.                                                                             |
| BR-04   | Full financial settlement (capture and/or release) is triggered only when the last item in the rental is returned.                           |
| BR-05   | At final settlement, each item's actual cost is computed by tariff module.                                                                   |
| BR-06   | Total actual cost = sum of all per-item actual costs across the rental. Calculated by tariff module. The response contains breakdown costs   |
| BR-07   | The settlement logic at final return follows FR-FIN-05 in full (under/over/DEBT scenarios), using the total actual cost as input.            |
| BR-08   | An item that has already been returned within the rental cannot be returned again.                                                           |
| BR-09   | Each partial return event is recorded with its actor (staff member) and timestamp for auditability.                                          |

---

## Data / Payload Reference

### Partial Return Request

| Field        | Required | Notes                                                  |
|--------------|----------|--------------------------------------------------------|
| Rental ID    | Yes      | Identifies the rental to which the item belongs        |
| Equipment ID | Yes      | Identifies the specific item being returned            |
| Return Time  | Yes      | Actual time the item was handed back (defaults to now) |
| Returned By  | Yes      | Staff member recording the return                      |

### Final Return (auto-triggered on last item)

| Field             | Required        | Notes                                         |
|-------------------|-----------------|-----------------------------------------------|
| Rental ID         | Yes             | The rental being completed                    |
| Total Actual Cost | Yes (computed)  | Sum of all per-item costs                     |
| Hold Amount       | Yes (retrieved) | Amount originally reserved at rental creation |
| Shortfall Amount  | Conditional     | Present only when total actual cost > hold    |
| Debt Amount       | Conditional     | Present only when wallet is also insufficient |

---

## Out of Scope

- Proportional hold adjustment at time of partial return — hold is always deferred to final return
- Changing the tariff for a specific item mid-rental after it has been partially returned
- Splitting a rental into sub-rentals per item
- Partial returns for single-item rentals (those follow the standard FR-FIN-05 flow directly)
