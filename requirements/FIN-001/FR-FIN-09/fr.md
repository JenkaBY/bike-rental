# User Story: FR-FIN-09 - Partial Equipment Return & Final Settlement

## 1. Description

**As the** Rental module
**I want to** invoke Finance exactly once when the last rented item is returned, passing the total actual cost computed
by the Tariff module
**So that** Finance only performs financially meaningful work and is not coupled to intermediate Rental state

## 2. Context & Business Rules

* **Trigger:** The Rental module detects that the last item of a rental has been returned, collects the total actual
  cost from the Tariff module, and sends a single settlement request to Finance.
* **Rules Enforced:**
    * Per-item return records and their timestamps are the responsibility of the Rental module; Finance is not notified
      of individual item returns.
    * The full original hold (`CUSTOMER_HOLD`) stays intact and unchanged until this single settlement request arrives.
    * The Tariff module computes each item's actual cost as: `tariff rate × (item return time − rental start time)`. The
      **total actual cost** = sum of all individual item costs. Finance consumes this pre-computed total and must not
      calculate it.
    * Finance receives a settlement request containing: rental ID, held amount, and total actual cost.
    * Settlement applies the total actual cost against the held amount following the rules of FR-FIN-07 (normal, final
      cost ≤ held) or FR-FIN-08 (overtime, final cost > held), as applicable.
    * The same settlement path applies regardless of whether the rental had one item or many.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Final settlement must complete atomically within 1 second.
* **Security/Compliance:** The settlement journal entries are the complete audit record for Finance; the per-item
  timestamps and return events are auditable within the Rental module.
* **Usability/Other:** N/A — this is a system-initiated operation with no direct user interaction.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Two-item rental — last item returned, normal settlement triggered**

* **Given** a rental with `CUSTOMER_HOLD` balance of €90
* **And** item 1 was returned at T1 and item 2 at T2 (recorded by the Rental module)
* **And** the Tariff module has computed a total actual cost of €70
* **When** the Rental module sends a settlement request with total cost €70
* **Then** Finance performs normal settlement per FR-FIN-07:
    * Capture: `CUSTOMER_HOLD` (debit €70) → `REVENUE` (credit €70)
    * Release: `CUSTOMER_HOLD` (debit €20) → `CUSTOMER_WALLET` (credit €20)
* **And** `CUSTOMER_HOLD` becomes €0

**Scenario 2: Two-item rental — last item returned, overtime settlement triggered**

* **Given** a rental with `CUSTOMER_HOLD` balance of €90
* **And** the Tariff module has computed a total actual cost of €110
* **When** the Rental module sends a settlement request with total cost €110
* **Then** Finance performs overtime settlement per FR-FIN-08

**Scenario 3: Single-item rental — item returned triggers immediate settlement**

* **Given** a rental with 1 item and `CUSTOMER_HOLD` balance in place
* **And** the Tariff module has computed the actual cost for that item
* **When** the Rental module sends the settlement request
* **Then** Finance applies normal or overtime settlement — identical behavior to a multi-item rental

## 5. Out of Scope

* Finance receiving or storing per-item return events or timestamps.
* Releasing part of the hold on a per-item basis before all items are returned.
* Re-opening a settled rental.
