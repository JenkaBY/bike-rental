# System Design: FR-TR-01 - Migrate Rental Creation & Draft Updates to TariffV2

## 1. Architectural Overview

This story evolves the cost-calculation path inside the Rental module at rental creation and draft-update time. The
deprecated per-item, per-call pattern against V1 `TariffFacade` (`selectTariff` + `calculateRentalCost`) is replaced by
a single batch call to `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)`. The inbound command contract
(`CreateRentalRequest`) gains three optional pricing-override fields that are stored on the `Rental` aggregate for
later settlement re-use. A new `RentalEquipment.equipmentTypeSlug` field is introduced to carry the equipment type
identifier that V2 requires at calculation time (and will be critical for FR-TR-02 settlement).

The hold-funds call to `FinanceFacade.holdFunds` now receives `RentalCostCalculationResult.totalCost()` — the
post-discount V2 amount — instead of the V1-derived estimated cost. All pricing logic remains entirely inside the
Tariff module boundary; the Rental module never re-derives prices from raw tariff fields.

---

## 2. Impacted Components

* **`RentalCommandController`:** Must accept three new optional request fields (`specialTariffId`, `specialPrice`,
  `discountPercent`) and enforce the mutual-exclusivity constraint between `specialTariffId` and `discountPercent`
  at the API boundary before forwarding to the use-case. `discountPercent` range validation ([0, 100]) is also applied
  here.

* **`CreateRentalService`:** Must replace all calls to `TariffFacade.selectTariff` and
  `TariffFacade.calculateRentalCost`
  with a single `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)` call. Must build the command from
  equipment type slugs (read from `EquipmentInfo`), planned duration, rental date, and optional pricing overrides.
  Must populate `RentalEquipment.estimatedCost` per item from `RentalCostCalculationResult.equipmentBreakdowns()`.
  Must persist `specialTariffId`, `specialPrice`, and `discountPercent` on the `Rental` aggregate. Must forward
  `totalCost()` to `FinanceFacade.holdFunds`.

* **`UpdateRentalService`:** On any PATCH that changes the equipment list or planned duration, must rebuild the
  `RentalCostCalculationCommand` using the equipment type slugs from the new equipment set and the stored pricing
  overrides from the `Rental` aggregate, then call `TariffV2Facade.calculateRentalCost`; update
  `RentalEquipment.estimatedCost` per item from the refreshed result. The V1 `TariffFacade` injection is removed
  entirely from this service.

* **`TariffV2Facade`** *(tariff module boundary, consumed — no internal change in this story)*: The new cross-module
  contract entry point consumed by `CreateRentalService` and `UpdateRentalService`. Accepts
  `RentalCostCalculationCommand` and returns `RentalCostCalculationResult` with `totalCost()` and
  per-item `equipmentBreakdowns()`.

* **`FinanceFacade`** *(finance module boundary)*: Must expose `holdFunds(rentalId, amount)` to reserve the V2 total
  cost against the customer wallet (per FR-FIN-14). The error response on insufficient funds must include
  `availableBalance` and `requiredAmount`; `requiredAmount` is the V2 post-discount total.

* **`Rental`** *(domain aggregate)*: Gains three new nullable fields — `specialTariffId` (Long), `specialPrice`
  (Money), and `discountPercent` (Integer). These are stored at creation and read back at settlement time without
  operator re-entry.

* **`RentalEquipment`** *(domain child entity)*: Gains new non-nullable field `equipmentTypeSlug` (String), populated
  from `EquipmentInfo.typeSlug()` at rental creation. `tariffId` becomes nullable; it is no longer required to be
  set by V2.

---

## 3. Abstract Data Schema Changes

* **Entity: `rental`**
    * **Attributes Added:**
        * `special_tariff_id` (BIGINT, nullable) — stores the SPECIAL-type tariff reference when operator-fixed
          pricing is used
        * `special_price` (NUMERIC(19,4), nullable) — operator-provided fixed total; present only when
          `special_tariff_id` is set
        * `discount_percent` (SMALLINT, nullable, check constraint 0–100) — percentage discount applied to the
          non-special subtotal; mutually exclusive with `special_tariff_id`

* **Entity: `rental_equipment`**
    * **Attributes Added:**
        * `equipment_type_slug` (VARCHAR, not null) — equipment type identifier used by V2 tariff engine at
          calculation and settlement time
    * **Attributes Modified:**
        * `tariff_id` — becomes nullable; no longer required after V2 migration

* **Relations:** No changes to entity associations.

---

## 4. Component Contracts & Payloads

* **Interaction: External Client → `RentalCommandController` (POST /api/rentals)**
    * **Protocol:** REST / HTTP
    * **Payload Changes:**
        * Request body gains three optional fields: `specialTariffId` (Long), `specialPrice` (Money),
          `discountPercent` (Integer)
        * Return `400 Bad Request` when `discountPercent` is outside [0, 100]
        * Return `400 Bad Request` when both `specialTariffId` and `discountPercent` are supplied in the same
          request
        * Return `422 Unprocessable Content` when `specialTariffId` references a tariff that does not exist or
          is not of `pricingType = SPECIAL`

* **Interaction: `CreateRentalService` → `TariffV2Facade`**
    * **Protocol:** In-process synchronous call (Spring Modulith Facade)
    * **Payload Changes:**
        * Command: `RentalCostCalculationCommand` containing `equipments` (list of `EquipmentCostItem` with
          `equipmentTypeSlug`), `plannedDuration`, `rentalDate`, and optional `specialTariffId`, `specialPrice`,
          `discountPercent`
        * Response: `RentalCostCalculationResult` with `totalCost()` (post-discount) and `equipmentBreakdowns()`
          (per-item cost)

* **Interaction: `UpdateRentalService` → `TariffV2Facade`**
    * **Protocol:** In-process synchronous call (Spring Modulith Facade)
    * **Payload Changes:** Same `RentalCostCalculationCommand` structure as above, sourcing pricing overrides
      from stored `Rental` fields rather than the inbound request

* **Interaction: `CreateRentalService` → `FinanceFacade` (holdFunds)**
    * **Protocol:** In-process synchronous call (Spring Modulith Facade)
    * **Payload Changes:** `holdFunds(rentalId, amount)` where `amount` is `RentalCostCalculationResult.totalCost()`
      (after discount). Error response when balance is insufficient must carry `availableBalance` and `requiredAmount`.

---

## 5. Updated Interaction Sequence

### Happy Path — Rental Created with Discount

1. External client sends `POST /api/rentals` with equipment IDs, planned duration, and `discountPercent = 10`.
2. `RentalCommandController` validates: `discountPercent` in [0, 100]; neither `specialTariffId` nor
   `discountPercent` both set; forwards command to `CreateRentalService`.
3. `CreateRentalService` calls `CustomerFacade.findById` to validate the customer exists.
4. `CreateRentalService` calls `EquipmentFacade.findByIds` to load equipment records.
5. `RequestedEquipmentValidator` confirms all requested equipment IDs resolved to AVAILABLE units.
6. `CreateRentalService` builds `RentalCostCalculationCommand` with `EquipmentCostItem` list (each carrying
   `equipmentTypeSlug`), planned duration, rental date, and `discountPercent = 10`.
7. `CreateRentalService` calls `TariffV2Facade.calculateRentalCost(command)` → receives
   `RentalCostCalculationResult`.
8. Per-item `estimatedCost` is set on each `RentalEquipment` from `equipmentBreakdowns()`.
9. `Rental` aggregate is constructed with `discountPercent = 10`; `RentalEquipment` children include
   `equipmentTypeSlug`.
10. `RentalRepository.save` persists the new `Rental` with its children.
11. `CreateRentalService` calls `FinanceFacade.holdFunds(rentalId, totalCost)` with the discounted V2 total.
12. `SpringApplicationEventPublisher` publishes `RentalCreated`.
13. Response (`201 Created`) returned to client.

### Happy Path — Rental Created with SPECIAL Pricing

1. External client sends `POST /api/rentals` with `specialTariffId = 99` and `specialPrice = 15.00`.
2. `RentalCommandController` verifies neither `discountPercent` is also set; forwards command.
3. `CreateRentalService` builds `RentalCostCalculationCommand` with `specialTariffId = 99` and
   `specialPrice = 15.00`.
4. `TariffV2Facade.calculateRentalCost(command)` validates the tariff exists and is of `pricingType = SPECIAL`;
   returns `totalCost = 15.00`.
5. `Rental` is persisted with `specialTariffId = 99` and `specialPrice = 15.00`.
6. `FinanceFacade.holdFunds(rentalId, 15.00)` is called.
7. `RentalCreated` event published; `201 Created` returned.

### Happy Path — PATCH Updates Equipment on DRAFT Rental

1. External client sends `PATCH /api/rentals/{id}` with updated equipment IDs.
2. `RentalCommandController` forwards to `UpdateRentalService`.
3. `UpdateRentalService` loads the `Rental` aggregate from `RentalRepository`.
4. `EquipmentFacade.findByIds` loads the new equipment set; `RequestedEquipmentValidator` confirms availability.
5. `UpdateRentalService` builds `RentalCostCalculationCommand` using new equipment type slugs and stored pricing
   overrides from `Rental`.
6. `TariffV2Facade.calculateRentalCost(command)` returns the refreshed result.
7. `RentalEquipment.estimatedCost` is updated per item from the new breakdown.
8. `RentalRepository.save` persists the updated aggregate.
9. `SpringApplicationEventPublisher` publishes `RentalUpdated`; response returned.

### Unhappy Path — Both specialTariffId and discountPercent Set

1. External client sends `POST /api/rentals` with both `specialTariffId` and `discountPercent`.
2. `RentalCommandController` detects the mutual-exclusivity violation.
3. Response: `400 Bad Request` with error body explaining the mutual-exclusivity constraint.

### Unhappy Path — discountPercent Out of Range

1. External client sends `POST /api/rentals` with `discountPercent = 110`.
2. `RentalCommandController` fails bean-validation on the `discountPercent` field.
3. Response: `400 Bad Request`.

### Unhappy Path — specialTariffId Not SPECIAL Type

1. External client sends `POST /api/rentals` with `specialTariffId = 7` (an HOURLY tariff).
2. `TariffV2Facade.calculateRentalCost(command)` validates the tariff pricing type and signals a domain error.
3. `CreateRentalService` propagates the error.
4. Response: `422 Unprocessable Content`.

### Unhappy Path — Insufficient Wallet Balance

1. All validation passes; `FinanceFacade.holdFunds` is reached.
2. The customer's wallet balance is below `totalCost`.
3. `FinanceFacade.holdFunds` returns an insufficient-funds error carrying `availableBalance` and `requiredAmount`.
4. Response: error structure with `availableBalance` and `requiredAmount` returned to client.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** All pricing computation is delegated to the Tariff module boundary. The Rental module must
  never re-derive costs from raw tariff rate fields. After this story, no import of `TariffFacade` (V1) types may
  exist in the rental module.

* **Scale & Performance:** The V2 cost calculation and the `holdFunds` call must complete within the same
  synchronous transaction scope as the prior V1 flow, targeting under 2 seconds end-to-end. The single batch
  `calculateRentalCost` call replaces the prior per-item loop, reducing inter-module call overhead at creation time.
