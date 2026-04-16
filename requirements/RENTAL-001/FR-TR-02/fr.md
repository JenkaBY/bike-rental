# User Story: FR-TR-02 - Migrate Rental Settlement to TariffV2

## 1. Description

**As the** Rental module
**I want to** calculate final rental costs at equipment return using TariffV2 instead of the deprecated TariffFacade
**So that** all V2 pricing rules (forgiveness, degressive rates, daily overtime, discounts) are applied at settlement,
and the Rental module no longer depends on the deprecated V1 TariffFacade

## 2. Context & Business Rules

* **Trigger:** Staff records an equipment return via `POST /api/rentals/return`. On partial return, per-item final
  cost is recorded; on final return, the total is settled against the customer's hold via `FinanceFacade.settleRental`.
* **Prerequisite:** FR-TR-01 must be complete — `RentalEquipment` must store `equipmentTypeSlug`, and `Rental`
  must carry `discountPercent`, `specialTariffId`, and `specialPrice`.
* **Rules Enforced:**
    * `ReturnEquipmentService` replaces
      `TariffFacade.calculateRentalCost(tariffId, actualDuration, billableMinutes, plannedDuration)` with a single
      `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)` call covering all returned equipment items in
      one batch.
    * The `RentalCostCalculationCommand` is built from:
        * `equipments` — one `EquipmentCostItem(equipmentTypeSlug)` per returned item (read from
          `RentalEquipment.equipmentTypeSlug` stored at creation)
        * `plannedDuration` — from `Rental.plannedDuration`
        * `actualDuration` — computed from the item's `startedAt` to `returnTime` (rounding is handled by the
          Rental module's `RentalDurationCalculator` before passing to V2)
        * `discount` — from `Rental.discountPercent` (applied only in normal mode)
        * `specialTariffId` + `specialPrice` — from `Rental.specialTariffId` / `Rental.specialPrice`
        * `rentalDate` — the date the rental started
    * Forgiveness rule is applied internally by `TariffV2Facade` — the Rental module does not implement it.
    * `RentalEquipment.finalCost` is set from the corresponding `EquipmentCostBreakdown.cost()` in the V2 result.
    * The total final cost passed to `financeFacade.settleRental()` is `RentalCostCalculationResult.totalCost()`.
    * `RentalEquipment.tariffId` is not used in V2 settlement; existing nullable values are ignored.
    * After this story, `TariffFacade` (V1) has zero callers in the Rental module. Its `@Autowired` injections in
      `CreateRentalService`, `UpdateRentalService`, and `ReturnEquipmentService` are removed.
  * `RentalReturnResponse` does **not** include a per-item cost breakdown. The response carries the updated
    `Rental` object only, which contains sufficient information (e.g., `finalCost`, status) to initiate customer
    charging. Callers requiring a detailed cost breakdown must invoke the idempotent
    `POST /api/tariffs/calculation` endpoint separately.

* **Domain model change — `RentalEquipment.equipmentTypeSlug` (new field):**
    * Added as part of FR-TR-01 (or as a prerequisite task within this story if not already done).
    * Populated from `EquipmentInfo.typeSlug()` at rental creation.
    * Persisted in the `rental_equipment` table via a Liquibase column addition.
    * Without this field, V2 cannot identify which tariff pool to evaluate at return time.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** Settlement calculation (V2 batch call) must complete within the existing transaction time budget
  (under 2 seconds for final return, including `settleRental`).
* **Security/Compliance:** The Rental module must never re-derive pricing from tariff fields — all cost computation
  is delegated to `TariffV2Facade`. After this story, there must be zero import references to `TariffFacade` (V1)
  in the rental module.
* **Usability/Other:** `RentalReturnResponse` must **not** include a per-item cost breakdown. The response
  contains the updated `Rental` object only. Callers needing itemised cost details must use the idempotent
  `POST /api/tariffs/calculation` endpoint.

## 4. Acceptance Criteria (BDD)

**Scenario 1: Single item returned — V2 cost applied**

* **Given** an active rental for one bicycle (planned 2h, started at T, discountPercent = 0)
* **And** the customer returns the bicycle at T + 2h 5min (5 minutes overtime within forgiveness)
* **When** `POST /api/rentals/return` is submitted
* **Then** `TariffV2Facade.calculateRentalCost` is called with `actualDuration = 2h 5min` and `plannedDuration = 2h`
* **And** V2 applies forgiveness internally and charges for 2h (planned)
* **And** `RentalEquipment.finalCost` is set to the V2 result for that item
* **And** `financeFacade.settleRental` is called with `totalCost` = V2 result total

**Scenario 2: Multi-item final return — batch V2 calculation, discount applied**

* **Given** an active rental for one bicycle and one child seat (discountPercent = 10)
* **When** both are returned at the same time
* **Then** `TariffV2Facade.calculateRentalCost` is called once with both items in the batch
* **And** the V2 result applies 10% discount to the non-special subtotal
* **And** `settleRental` receives the discounted total

**Scenario 3: Partial return — no settlement yet**

* **Given** an active rental for two bicycles
* **When** only the first bicycle is returned
* **Then** V2 calculates partial cost for that item only
* **And** `RentalEquipment.finalCost` is recorded for the returned item
* **And** `settleRental` is NOT called
* **And** rental remains `ACTIVE`

**Scenario 4: Final return with SPECIAL pricing**

* **Given** an active rental with `specialTariffId = 99` and `specialPrice = 15.00`
* **When** all equipment is returned
* **Then** `TariffV2Facade.calculateRentalCost` is called with `specialTariffId = 99` and `specialPrice = 15.00`
* **And** V2 bypasses per-item calculations and returns total = €15.00
* **And** `settleRental` is called with €15.00

**Scenario 5: No TariffFacade (V1) import in Rental module after migration**

* **Given** FR-TR-02 changes are applied
* **When** the codebase is searched for `import com.github.jenkaby.bikerental.tariff.TariffFacade` in the rental
  module packages
* **Then** zero matches are found

## 5. Out of Scope

* Removal of `TariffFacade` and V1 classes from the tariff module itself (covered by FR-TR-03).
* Implementing the `POST /api/tariffs/calculation` endpoint (assumed to exist; covered by a separate story).
* Debt auto-recovery on subsequent deposit (covered by FR-FIN-12).
