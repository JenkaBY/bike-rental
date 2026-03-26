# User Story: FR-FIN-08 - Overtime Settlement

## 1. Description

**As the** Rental module
**I want to** request Finance to settle a completed rental when the final cost exceeds the held amount
**So that** the shop recovers as much earned revenue as possible and any unrecoverable shortfall is flagged for manual
staff resolution

## 2. Context & Business Rules

* **Trigger:** The Rental module requests settlement when the last item is returned and the Tariff module has computed a
  final cost greater than the held amount.
* **Rules Enforced:**
    * The final cost is provided by the Tariff module; Finance must not calculate it.
    * This flow applies only when **final cost > held amount**.
    * The gap amount = final cost − held amount.
    * Settlement proceeds in at most three steps, evaluated in order, all within one atomic transaction:
        1. **Capture full hold:** `CUSTOMER_HOLD` (debit, held amount) → `REVENUE` (credit, held amount). Always
           executed.
        2. **Debit gap from wallet:** `CUSTOMER_WALLET` (debit, gap) → `REVENUE` (credit, gap). Executed only if
           `CUSTOMER_WALLET` balance ≥ gap.
        3. **Partial capture + DEBT flag:** If `CUSTOMER_WALLET` < gap, debit whatever is available from
           `CUSTOMER_WALLET` → `REVENUE` and flag the rental with status `DEBT`. The remaining shortfall is not
           automatically recovered.
    * After settlement, `CUSTOMER_HOLD` balance must be zero.
    * Transaction types: `CAPTURE` (step 1), `OVERTIME_DEBIT` (step 2 or 3 wallet debit).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** All three settlement steps must execute atomically in a single transaction within 1 second.
* **Security/Compliance:** All journal entries (including partial captures) are persisted and auditable. The `DEBT` flag
  and the shortfall amount are recorded against the rental for staff follow-up.
* **Usability/Other:** Staff must be able to identify all rentals flagged with `DEBT` status via the rental management
  interface (out of scope here but the flag must be set correctly).

## 4. Acceptance Criteria (BDD)

**Scenario 1: Overtime — wallet covers the gap fully**

* **Given** a rental with `CUSTOMER_HOLD` balance of €80
* **And** the Tariff module computes a final cost of €100 (gap = €20)
* **And** `CUSTOMER_WALLET` balance is €50
* **When** the Rental module requests settlement
* **Then** journal entry 1: `CUSTOMER_HOLD` (debit €80) / `REVENUE` (credit €80)
* **And** journal entry 2: `CUSTOMER_WALLET` (debit €20) / `REVENUE` (credit €20)
* **And** `CUSTOMER_HOLD` becomes €0
* **And** `CUSTOMER_WALLET` decreases by €20
* **And** `REVENUE` increases by €100
* **And** rental is NOT flagged as `DEBT`

**Scenario 2: Overtime — wallet partially covers the gap (DEBT flagged)**

* **Given** a rental with `CUSTOMER_HOLD` balance of €80
* **And** the Tariff module computes a final cost of €110 (gap = €30)
* **And** `CUSTOMER_WALLET` balance is €10
* **When** the Rental module requests settlement
* **Then** journal entry 1: `CUSTOMER_HOLD` (debit €80) / `REVENUE` (credit €80)
* **And** journal entry 2: `CUSTOMER_WALLET` (debit €10) / `REVENUE` (credit €10)
* **And** `CUSTOMER_HOLD` becomes €0
* **And** `CUSTOMER_WALLET` becomes €0
* **And** `REVENUE` increases by €90
* **And** the rental is flagged with status `DEBT`

**Scenario 3: Overtime — wallet is empty (DEBT flagged, only hold captured)**

* **Given** a rental with `CUSTOMER_HOLD` balance of €80
* **And** the Tariff module computes a final cost of €100
* **And** `CUSTOMER_WALLET` balance is €0
* **When** the Rental module requests settlement
* **Then** journal entry 1: `CUSTOMER_HOLD` (debit €80) / `REVENUE` (credit €80)
* **And** no further debit entry is created
* **And** `REVENUE` increases by €80
* **And** the rental is flagged with status `DEBT`

**Scenario 4: Settlement is atomic — any failure rolls back entirely**

* **Given** a rental with an active hold and wallet balance
* **When** settlement is initiated but a system error occurs mid-way
* **Then** all journal entries for this settlement are rolled back
* **And** no DEBT flag is set
* **And** the rental remains in its pre-settlement state

## 5. Out of Scope

* Settlements where final cost ≤ held amount (covered by FR-FIN-07).
* Automated DEBT recovery or debt collection workflows.
* Manual adjustment of the shortfall amount by staff (covered by FR-FIN-04 if needed later).
