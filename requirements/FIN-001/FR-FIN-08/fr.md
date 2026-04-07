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
  * Settlement only proceeds when total available funds (`CUSTOMER_HOLD` + `CUSTOMER_WALLET`) ≥ final cost.
    If not, no journal entries are created; the settlement raises an exception and the rental is flagged with
    status `DEBT`. Automatic recovery is handled separately (see FR-FIN-12).
  * When funds are sufficient, settlement executes two steps atomically:
      1. **Capture full hold:** `CUSTOMER_HOLD` (debit, held amount) → `REVENUE` (credit, held amount).
      2. **Debit gap from wallet:** `CUSTOMER_WALLET` (debit, gap) → `REVENUE` (credit, gap).
    * After settlement, `CUSTOMER_HOLD` balance must be zero.
  * Transaction type for both journal entries: `CAPTURE`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** All three settlement steps must execute atomically in a single transaction within 1 second.
* **Security/Compliance:** All journal entries are persisted and auditable. When settlement is rejected due to
  insufficient funds, the `DEBT` flag is recorded against the rental for staff follow-up. The Finance module must
  not mark the shared database transaction as rollback-only on a settlement rejection, so that the Rental module
  can still commit the `DEBT` status within the same transaction.
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

**Scenario 2: Overtime — insufficient total funds (DEBT flagged, no journal entries)**

* **Given** a rental with `CUSTOMER_HOLD` balance of €80
* **And** the Tariff module computes a final cost of €110 (gap = €30)
* **And** `CUSTOMER_WALLET` balance is €10 (total available: €90, which is < €110)
* **When** the Rental module requests settlement
* **Then** no journal entries are created
* **And** `CUSTOMER_HOLD` remains €80
* **And** `CUSTOMER_WALLET` remains €10
* **And** `REVENUE` is unchanged
* **And** the rental is flagged with status `DEBT`

**Scenario 3: Overtime — wallet is empty (DEBT flagged, no journal entries)**

* **Given** a rental with `CUSTOMER_HOLD` balance of €80
* **And** the Tariff module computes a final cost of €100
* **And** `CUSTOMER_WALLET` balance is €0 (total available: €80, which is < €100)
* **When** the Rental module requests settlement
* **Then** no journal entries are created
* **And** `CUSTOMER_HOLD` remains €80
* **And** `CUSTOMER_WALLET` remains €0
* **And** `REVENUE` is unchanged
* **And** the rental is flagged with status `DEBT`

**Scenario 4a: Settlement is atomic — system error on sufficient-funds path rolls back entirely**

* **Given** a rental with sufficient combined `CUSTOMER_HOLD` and `CUSTOMER_WALLET` to cover the final cost
* **When** settlement is initiated but an unexpected system error occurs mid-way
* **Then** all Finance journal entries are rolled back
* **And** the rental status is not changed
* **And** no DEBT flag is set

**Scenario 4b: Intentional split commit — insufficient funds path**

* **Given** a rental where `CUSTOMER_HOLD` + `CUSTOMER_WALLET` < final cost
* **When** the Rental module requests settlement
* **Then** the Finance module creates no journal entries and raises a settlement exception
* **And** the Rental module commits the rental with status `DEBT` in the Rental transaction
* **And** account balances remain unchanged

## 5. Out of Scope

* Settlements where final cost ≤ held amount (covered by FR-FIN-07).
* Automated DEBT recovery or debt collection workflows.
* Manual adjustment of the shortfall amount by staff (covered by FR-FIN-04 if needed later).
