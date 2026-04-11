# User Story: FR-TR-01 - Migrate Rental Creation & Draft Updates to TariffV2

## 1. Description

**As the** Rental module
**I want to** calculate planned rental costs using TariffV2 instead of the deprecated TariffFacade
**So that** the full V2 pricing model (degressive hourly, flat hourly, daily, flat fee, special, discounts) is
applied from the moment a rental is created, and the correct hold amount is reserved for the customer

## 2. Context & Business Rules

* **Trigger:** A staff member submits `POST /api/rentals` (rental creation) or `PATCH /api/rentals/{id}` to update
  equipment, duration, or pricing overrides on a DRAFT rental.
* **Rules Enforced:**
    * `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)` replaces all calls to the deprecated
      `TariffFacade.selectTariff()` and `TariffFacade.calculateRentalCost()` in `CreateRentalService` and
      `UpdateRentalService`.
    * The `RentalCostCalculationCommand` is built from: the equipment list (type slugs from `EquipmentInfo`), the
      planned duration, the rental date, and the optional `specialTariffId`, `specialPrice`, and `discountPercent`
      from the request.
    * The `RentalCostCalculationResult.totalCost()` (after discount) is the amount passed to `holdFunds` (FR-FIN-14).
    * `CreateRentalRequest` gains three optional fields:
        * `specialTariffId` (Long) — id of a SPECIAL-type V2 tariff; mutually exclusive with per-item calculation
        * `specialPrice` (Money) — operator-provided fixed total; required when `specialTariffId` is set
        * `discountPercent` (Integer, 0–100) — discount applied to the non-special subtotal; ignored when
          `specialTariffId` is set
    * `specialTariffId`, `specialPrice`, and `discountPercent` are stored on the `Rental` aggregate so they can be
      re-applied at settlement time without operator re-entry.
    * `RentalEquipment.estimatedCost` is populated per item from `RentalCostCalculationResult.equipmentBreakdowns()`.
    * `RentalEquipment.tariffId` is not populated by V2 (V2 auto-selects at calculation time); the field becomes
      nullable. No validation requires it to be set after this story.
    * If `specialTariffId` is provided but the referenced tariff does not exist or is not of `pricingType = SPECIAL`,
      the request is rejected with `422 Unprocessible content`.
    * If `discountPercent` is outside [0, 100], the request is rejected with `400 Bad Request`.
    * Supplying both `specialTariffId` and `discountPercent` in the same request is rejected with `400 Bad Request`.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Cost calculation + hold must complete within the same transaction bound as today (under 2 seconds).
* **Security/Compliance:** No pricing logic lives in the Rental module — all computation delegates to the Tariff
  module. The Rental module must not re-derive prices from tariff fields.
* **Usability/Other:** The insufficient-funds error (from FR-FIN-14) must still include `availableBalance` and
  `requiredAmount`; the `requiredAmount` is now the V2 total cost after discount.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Rental created with degressive hourly tariff — standard path**

* **Given** a customer with sufficient wallet balance
* **And** a rental request for one bicycle with planned duration 2 hours (no discount, no special pricing)
* **When** `POST /api/rentals` is submitted
* **Then** `TariffV2Facade.calculateRentalCost` is called with the bicycle equipment type and 2-hour duration
* **And** `RentalEquipment.estimatedCost` is set to the V2 per-item result
* **And** `holdFunds` is called with the V2 `totalCost()`
* **And** the rental is persisted in `DRAFT` status

**Scenario 2: Rental created with discount — hold reflects discounted total**

* **Given** a customer with €100 wallet balance
* **And** a rental request for one bicycle (V2 planned cost = €16), `discountPercent = 10`
* **When** `POST /api/rentals` is submitted
* **Then** V2 total cost = €14.40
* **And** `holdFunds` is called with €14.40
* **And** `Rental.discountPercent = 10` is persisted

**Scenario 3: Rental created with SPECIAL pricing**

* **Given** a valid SPECIAL-type tariff with id=99
* **And** a rental request with `specialTariffId = 99`, `specialPrice = 15.00`
* **When** `POST /api/rentals` is submitted
* **Then** `TariffV2Facade.calculateRentalCost` is called with `specialTariffId = 99` and `specialPrice = 15.00`
* **And** `holdFunds` is called with €15.00
* **And** `Rental.specialTariffId = 99` and `Rental.specialPrice = 15.00` are persisted

**Scenario 4: Rejected — specialTariffId and discountPercent supplied together**

* **Given** a rental request with both `specialTariffId` and `discountPercent` set
* **When** `POST /api/rentals` is submitted
* **Then** the response is `400 Bad Request`
* **And** the error body contains a validation message explaining the fields are mutually exclusive

**Scenario 5: Rejected — discountPercent out of range**

* **Given** a rental request with `discountPercent = 110`
* **When** `POST /api/rentals` is submitted
* **Then** the response is `400 Bad Request`

**Scenario 6: PATCH updates equipment — cost recalculated via V2**

* **Given** a DRAFT rental with equipment set and a hold already in place
* **When** `PATCH /api/rentals/{id}` updates the equipment list
* **Then** `TariffV2Facade.calculateRentalCost` is called with the new equipment types
* **And** `RentalEquipment.estimatedCost` is updated accordingly

## 5. Out of Scope

* Updating or releasing the hold when equipment is changed on a DRAFT rental (hold recalculation on update is a
  separate story).
* Settlement / return flows (covered by FR-TR-02).
* Removal of `TariffFacade` (V1) from the tariff module (covered by FR-TR-03).
* Any API changes to the Tariff module itself.
