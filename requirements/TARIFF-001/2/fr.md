# User Story: FR-2 — PUT /api/tariffs/calculations — per-equipment V2 cost calculation endpoint

## 1. Description

**As a** rental module (or external API consumer)  
**I want to** submit a cost calculation request where each equipment item carries its own `startAt` and `returnAt`
timestamps  
**So that** partial returns and overnight flat-fee rentals are all billed accurately per individual item, regardless
of when other items in the same rental were returned

---

## 2. Context & Business Rules

* **Trigger:** A caller needs to calculate the total cost for one or more equipment items that may have been returned
  at different times (partial return scenario), or for a pre-return cost estimate.

### Request structure

| Field                    | Required | Description                                                                                                               |
|--------------------------|----------|---------------------------------------------------------------------------------------------------------------------------|
| `equipments`             | Yes      | Non-empty list of equipment items (see per-item fields below)                                                             |
| `startAt`                | Yes      | Date-time when the rental started; used as the start of each item's billing interval and as the date for tariff selection |
| `plannedDurationMinutes` | Yes      | Global planned duration shared by all items; used for forgiveness logic                                                   |
| `discountPercent`        | No       | Global discount applied to the combined subtotal                                                                          |
| `specialTariffId`        | No       | Activates special-tariff mode (fixed group price)                                                                         |
| `specialPrice`           | No       | Fixed group price used only in special-tariff mode                                                                        |

**Per equipment item:**

| Field           | Required | Description                                                                    |
|-----------------|----------|--------------------------------------------------------------------------------|
| `equipmentId`   | Yes      | Unique identifier of the physical equipment unit                               |
| `equipmentType` | Yes      | Equipment type slug used for tariff lookup                                     |
| `returnAt`      | No       | Date-time when this item was returned; if absent → estimate mode for this item |

### Cost calculation rules

* **Billing duration per item:** computed as the interval from the global `startAt` to the item's `returnAt`.
    * If `returnAt` is absent for an item, the global `plannedDurationMinutes` is used for that item (estimate mode).
* **Forgiveness threshold per item:** the item's actual duration is compared to the global `plannedDurationMinutes`.
    * If the overrun is within the configured threshold, the item is billed at the planned duration.
    * If the overrun exceeds the threshold, the item is billed at its full actual duration.
* **Tariff selection:** A single tariff is selected per **equipment type** using `startAt.toLocalDate()` (evaluated
  in the server's configured timezone). Items of the same type share the same selected tariff.
* **Flat fee tariff:** When the selected tariff is `FLAT_FEE`, the number of billable days is the count of distinct
  calendar dates in `[startAt.toLocalDate(timezone), returnAt.toLocalDate(timezone)]` (per FR-1).
* **Daily tariff:** `DAILY` uses a 24-hour-block model with an `overtimeHourlyPrice` for remainder hours; the
  calendar-day fix from FR-1 does **not** apply. An overnight rental shorter than 24 h is correctly charged as
  1 full day with no overtime.
* **Global discount:** Applied as a percentage reduction to the combined subtotal of all items.
* **Special tariff mode:** Activated when `specialTariffId` is provided. The `specialPrice` is applied as a fixed
  total; individual item costs are reported as zero and the group pricing flag is set to `true`.
* **Validation:** `specialTariffId` and `specialPrice` must both be provided together (existing
  `@SpecialTariffConsistency` constraint applies).

### Facade integration

* The controller must call `TariffV2Facade` to perform the calculation, so the rental module can invoke the same
  logic in-process without going through HTTP.
* A new method `calculateRentalCostV2(RentalCostCalculationV2Command)` must be exposed on `TariffV2Facade`.

---

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Response time should be comparable to the existing `/calculate` endpoint (no additional database
  queries per item beyond one tariff lookup per distinct equipment type).
* **Security/Compliance:** Request body validated with JSR-380 constraints; invalid requests return HTTP 400 with
  `ProblemDetail` and `CONSTRAINT_VIOLATION` error code.
* **API Contract:** Endpoint documented with SpringDoc OpenAPI annotations (summary, response codes 200 / 400 / 404).

---

## 4. Acceptance Criteria (BDD)

**Scenario 1: Two items of the same type returned at different times (partial return)**

* **Given** a rental with global `startAt` = 09:00, and two helmets (IDs 10 and 11)
* **And** helmet 10 has `returnAt` = 11:00 (2 h actual), helmet 11 has `returnAt` = 14:00 (5 h actual)
* **And** the planned duration is 3 hours
* **When** `PUT /api/tariffs/calculations` is called with both items
* **Then** each item's cost is calculated independently using its own billing duration
* **And** the response breakdown contains two entries: one for equipmentId=10 and one for equipmentId=11

**Scenario 2: Overnight rental with FLAT_FEE tariff — correct day count per item**

* **Given** global `startAt` = 20:00 June 1
* **And** a bike (FLAT_FEE tariff, issuance fee 15.00) with `returnAt` = 08:00 June 2
* **And** a helmet (FLAT_FEE tariff, issuance fee 5.00) with `returnAt` = 08:00 June 2
* **When** `PUT /api/tariffs/calculations` is called
* **Then** the bike cost is 30.00 (2 days × 15.00)
* **And** the helmet cost is 10.00 (2 days × 5.00)

**Scenario 3: Item without returnAt uses planned duration (estimate mode)**

* **Given** a request where one item has `returnAt` = null and `plannedDurationMinutes` = 180
* **When** `PUT /api/tariffs/calculations` is called
* **Then** that item is billed using 180 minutes from `startAt`
* **And** the response marks the overall calculation as an estimate

**Scenario 4: Forgiveness threshold per item**

* **Given** global `startAt` = T0, `plannedDurationMinutes` = 60, forgiveness threshold = 15 minutes
* **And** item A has `returnAt` = T0 + 70 min (overrun 10 min ≤ threshold)
* **And** item B has `returnAt` = T0 + 80 min (overrun 20 min > threshold)
* **When** `PUT /api/tariffs/calculations` is called
* **Then** item A is billed for 60 minutes (overrun forgiven)
* **And** item B is billed for 80 minutes (full actual duration)

**Scenario 5: Global discount applied to combined subtotal**

* **Given** a request with two items, each costing 100.00, and a discount of 10%
* **When** `PUT /api/tariffs/calculations` is called
* **Then** subtotal is 200.00, discount is 20.00, total is 180.00

**Scenario 6: Special tariff mode**

* **Given** a request with `specialTariffId` and `specialPrice` = 50.00
* **When** `PUT /api/tariffs/calculations` is called
* **Then** individual item costs are reported as 0.00
* **And** the total cost is 50.00
* **And** `specialPricingApplied` is `true` in the response

**Scenario 7: Missing required fields return HTTP 400**

* **Given** a request with an empty `equipments` list
* **When** `PUT /api/tariffs/calculations` is called
* **Then** HTTP 400 is returned with a `ProblemDetail` body containing `errorCode = CONSTRAINT_VIOLATION`

**Scenario 8: No suitable tariff found returns HTTP 404**

* **Given** a request with an equipment type that has no active tariff on the given `rentalDate`
* **When** `PUT /api/tariffs/calculations` is called
* **Then** HTTP 404 is returned with a `ProblemDetail` body containing `errorCode = RESOURCE_NOT_FOUND`

---

## 5. Out of Scope

* Persisting the calculated cost to the database (this endpoint is a pure calculation utility).
* Changes to the existing `POST /api/tariffs/calculate` endpoint or `RentalCostCalculationCommand`.
* Per-item discount or per-item special tariff override.
* Timezone selection per request; server timezone is always used for date boundaries.
* Changes to `DAILY` tariff calculation logic — it correctly uses a 24-hour-block model and is not affected by the
  calendar-day fix.
