# User Story: FR-TR-04 - Update Pricing Overrides on DRAFT Rental via PATCH

## 1. Description

**As a** Rental module
**I want to** accept pricing override changes (`specialTariffId`/`specialPrice`, `discountPercent`) on an existing
DRAFT rental and recalculate V2 estimated costs accordingly
**So that** a staff member can correct or remove special pricing and discounts before the rental is activated,
without having to cancel and recreate the rental

## 2. Context & Business Rules

* **Trigger:** A staff member submits `PATCH /api/rentals/{id}` on a DRAFT rental with one or more of
  `specialTariffId`, `specialPrice`, or `discountPercent` present in the request body (including explicit `null`
  to clear a previously set value).
* **Rules Enforced:**
    * All three fields (`specialTariffId`, `specialPrice`, `discountPercent`) are independently patchable on a
      DRAFT rental.
    * If a field is included in the PATCH payload with a non-null value, it replaces the currently stored value on
      the `Rental` aggregate.
    * If a field is included in the PATCH payload as `null`, the stored override is cleared:
        * Clearing `specialTariffId` (and `specialPrice`) reverts the rental to standard per-item V2 pricing at
          the next recalculation.
        * Clearing `discountPercent` removes the discount.
    * When any pricing override field changes, `TariffV2Facade.calculateRentalCost` is re-invoked using the
      updated overrides and the current equipment set/planned duration stored on the `Rental` aggregate.
      `RentalEquipment.estimatedCost` is updated per item from the refreshed `equipmentBreakdowns()`.
    * The same validation rules from rental creation apply:
        * `discountPercent` must be in [0, 100]; violated → `400 Bad Request`.
        * Supplying both `specialTariffId` and `discountPercent` (non-null) in the same PATCH request is
          rejected → `400 Bad Request`.
        * If `specialTariffId` is non-null but references a tariff that does not exist or is not of
          `pricingType = SPECIAL`, the request is rejected → `422 Unprocessable Content`.
        * `specialPrice` is required when `specialTariffId` is set; supplying `specialTariffId` without
          `specialPrice` (or vice-versa) is rejected → `400 Bad Request`.
    * This operation is only valid when the rental is in `DRAFT` status. Attempting to patch pricing overrides
      on a non-DRAFT rental is rejected → `422 Unprocessable Content`.
    * The hold amount is **not** recalculated or released as part of this operation (hold recalculation is a
      separate concern).

## 3. Non-Functional Requirements (NFRs)

* **Performance:** The cost recalculation triggered by this PATCH must complete within the same transaction bound
  as other PATCH operations (under 2 seconds).
* **Security/Compliance:** No pricing logic lives in the Rental module — all computation delegates to the Tariff
  module. The Rental module must not derive prices from raw tariff fields.
* **Usability/Other:** N/A

## 4. Acceptance Criteria (BDD)

**Scenario 1: PATCH sets discountPercent — cost recalculated and stored**

* **Given** a DRAFT rental with no discount and standard per-item pricing
* **When** `PATCH /api/rentals/{id}` is submitted with `discountPercent = 10`
* **Then** `TariffV2Facade.calculateRentalCost` is called with `discountPercent = 10` and current
  equipment/duration
* **And** `RentalEquipment.estimatedCost` is updated per item from the refreshed result
* **And** `Rental.discountPercent = 10` is persisted
* **And** the response is `200 OK`

**Scenario 2: PATCH sets specialTariffId + specialPrice — cost recalculated and stored**

* **Given** a DRAFT rental with no special pricing
* **When** `PATCH /api/rentals/{id}` is submitted with `specialTariffId = 99` and `specialPrice = 15.00`
* **Then** `TariffV2Facade.calculateRentalCost` is called with `specialTariffId = 99` and
  `specialPrice = 15.00`
* **And** `Rental.specialTariffId = 99` and `Rental.specialPrice = 15.00` are persisted
* **And** `RentalEquipment.estimatedCost` is updated per item
* **And** the response is `200 OK`

**Scenario 3: PATCH clears discountPercent — discount removed, cost recalculated**

* **Given** a DRAFT rental with `discountPercent = 10` stored
* **When** `PATCH /api/rentals/{id}` is submitted with `discountPercent = null`
* **Then** `TariffV2Facade.calculateRentalCost` is called without a discount
* **And** `Rental.discountPercent` is cleared (null)
* **And** `RentalEquipment.estimatedCost` is updated per item to reflect the undiscounted cost
* **And** the response is `200 OK`

**Scenario 4: PATCH clears specialTariffId + specialPrice — reverts to standard pricing**

* **Given** a DRAFT rental with `specialTariffId = 99` and `specialPrice = 15.00` stored
* **When** `PATCH /api/rentals/{id}` is submitted with `specialTariffId = null` and `specialPrice = null`
* **Then** `TariffV2Facade.calculateRentalCost` is called in standard per-item mode (no special override)
* **And** `Rental.specialTariffId` and `Rental.specialPrice` are cleared (null)
* **And** `RentalEquipment.estimatedCost` is updated per item from the standard calculation
* **And** the response is `200 OK`

**Scenario 5: Rejected — discountPercent out of range**

* **Given** a DRAFT rental
* **When** `PATCH /api/rentals/{id}` is submitted with `discountPercent = 110`
* **Then** the response is `400 Bad Request`

**Scenario 6: Rejected — specialTariffId and discountPercent supplied together**

* **Given** a DRAFT rental
* **When** `PATCH /api/rentals/{id}` is submitted with both `specialTariffId = 99` and `discountPercent = 10`
* **Then** the response is `400 Bad Request`
* **And** the error body contains a validation message explaining the fields are mutually exclusive

**Scenario 7: Rejected — specialTariffId references non-existent or non-SPECIAL tariff**

* **Given** a DRAFT rental
* **When** `PATCH /api/rentals/{id}` is submitted with `specialTariffId = 999` (not found, or not SPECIAL type)
* **Then** the response is `422 Unprocessable Content`

**Scenario 8: Rejected — specialTariffId supplied without specialPrice**

* **Given** a DRAFT rental
* **When** `PATCH /api/rentals/{id}` is submitted with `specialTariffId = 99` and no `specialPrice`
* **Then** the response is `400 Bad Request`

**Scenario 9: Rejected — pricing override PATCH on non-DRAFT rental**

* **Given** an ACTIVE rental
* **When** `PATCH /api/rentals/{id}` is submitted with `discountPercent = 5`
* **Then** the response is `422 Unprocessable Content`

## 5. Out of Scope

* Recalculating or releasing the hold when pricing overrides change (hold recalculation on update is a separate
  story).
* Patching pricing overrides on non-DRAFT rentals (ACTIVE, RETURNED, SETTLED, etc.).
* Settlement / return flows (covered by FR-TR-02).
* Removal of `TariffFacade` V1 from the tariff module (covered by FR-TR-03).
* Any API changes to the Tariff module itself.
