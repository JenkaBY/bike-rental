# User Story: FR-FIN-07 - Normal Rental Settlement

## 1. Description

**As the** Rental module
**I want to** request Finance to settle a completed rental when the final cost is at or below the held amount
**So that** the shop earns its revenue and any unspent held funds are returned to the customer's wallet

## 2. Context & Business Rules

* **Trigger:** The Rental module requests settlement when the last rented item is returned and the Tariff module has
  computed the final cost.
* **Rules Enforced:**
    * The final cost is provided by the Tariff module; Finance must not calculate it.
    * This flow applies only when **final cost ≤ held amount**.
    * Settlement consists of exactly two sequential journal entries, both atomic:
        1. **Capture:** `CUSTOMER_HOLD` (debit, final cost) → `REVENUE` (credit, final cost).
        2. **Release:** `CUSTOMER_HOLD` (debit, excess) → `CUSTOMER_WALLET` (credit, excess), where excess = held
           amount − final cost.
    * If final cost equals the held amount exactly, the release journal entry has a value of zero and must be omitted (
      no zero-amount entries).
    * After settlement, `CUSTOMER_HOLD` balance for this rental must be zero.
    * Transaction types: `CAPTURE` (for the capture entry) and `RELEASE` (for the release entry).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The full settlement (capture + release) must complete atomically within a single transaction under 1
  second.
* **Security/Compliance:** Both journal entries are persisted and auditable. If either entry fails, the entire
  settlement is rolled back.
* **Usability/Other:** N/A — this is a system-initiated operation with no direct user interaction.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Settlement with final cost less than held amount (capture + release)**

* **Given** a rental with `CUSTOMER_HOLD` balance of €80
* **And** the Tariff module has computed a final cost of €60
* **When** the Rental module requests settlement
* **Then** journal entry 1 is created: `CUSTOMER_HOLD` (debit €60) / `REVENUE` (credit €60)
* **And** journal entry 2 is created: `CUSTOMER_HOLD` (debit €20) / `CUSTOMER_WALLET` (credit €20)
* **And** `CUSTOMER_HOLD` balance for this rental becomes €0
* **And** `CUSTOMER_WALLET` increases by €20
* **And** `REVENUE` increases by €60

**Scenario 2: Settlement with final cost equal to held amount (capture only)**

* **Given** a rental with `CUSTOMER_HOLD` balance of €75
* **And** the Tariff module has computed a final cost of €75
* **When** the Rental module requests settlement
* **Then** journal entry 1 is created: `CUSTOMER_HOLD` (debit €75) / `REVENUE` (credit €75)
* **And** no release entry is created
* **And** `CUSTOMER_HOLD` balance for this rental becomes €0

**Scenario 3: Settlement is atomic — partial failure rolls back**

* **Given** a rental with a valid hold in place
* **When** the capture entry succeeds but the release entry fails due to a system error
* **Then** the entire settlement transaction is rolled back
* **And** `CUSTOMER_HOLD` remains at its pre-settlement balance

## 5. Out of Scope

* Settlements where final cost exceeds the held amount (covered by FR-FIN-08).
* Triggering settlement for partial item returns (covered by FR-FIN-09).
* Manual settlement overrides by staff.
