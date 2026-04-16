# System Design: FR-TR-02 - Migrate Rental Settlement to TariffV2

## 1. Architectural Overview

This story completes the V2 tariff migration inside the Rental module by replacing the per-item, per-call settlement
pattern in `ReturnEquipmentService` with a single batch call to `TariffV2Facade.calculateRentalCost`. The command is
built from `RentalEquipment.equipmentTypeSlug` (stored at creation via FR-TR-01), actual durations computed by
`RentalDurationCalculatorImpl`, and the pricing overrides (`discountPercent`, `specialTariffId`, `specialPrice`)
carried on the `Rental` aggregate since FR-TR-01.

After this story, the Rental module contains zero import references to `TariffFacade` (V1) types. The public V1
tariff contract has no callers remaining in the rental bounded context, and the ground is cleared for the V1 deletion
in FR-TR-03.

---

## 2. Impacted Components

* **`ReturnEquipmentService`:** Must replace
  `TariffFacade.calculateRentalCost(tariffId, actualDuration, billableMinutes, plannedDuration)` with a single
  `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)` covering all currently-returned items in one
  batch. Must build the command from `RentalEquipment.equipmentTypeSlug`, billable durations (from
  `RentalDurationCalculatorImpl`), `Rental.plannedDuration`, `Rental.discountPercent`, `Rental.specialTariffId`,
  `Rental.specialPrice`, and the rental start date. Must set `RentalEquipment.finalCost` per item from the
  corresponding `EquipmentCostBreakdown.cost()`. Must pass `RentalCostCalculationResult.totalCost()` to
  `FinanceFacade.settleRental` on final return. Must remove the `TariffFacade` (V1) injection entirely.

* **`CreateRentalService`:** Remove any residual `TariffFacade` (V1) injection that was not already eliminated in
  FR-TR-01.

* **`UpdateRentalService`:** Remove any residual `TariffFacade` (V1) injection that was not already eliminated in
  FR-TR-01.

* **`TariffV2Facade`** *(tariff module boundary, consumed — no internal change in this story)*: Consumed by
  `ReturnEquipmentService` with a `RentalCostCalculationCommand` covering all returned items in one batch. V2
  applies the forgiveness rule internally; the rental module passes only actual duration and planned duration
  without implementing forgiveness logic.

* **`FinanceFacade`** *(finance module boundary)*: Must expose `settleRental(rentalId, totalCost)` to perform
  final settlement against the customer hold using the V2 batch total. Called only on final return (all equipment
  returned).

* **`RentalCommandMapper` / `ReturnEquipmentResult`** *(rental web layer)*: Any reference to V1 `RentalCost`
  interface in these types must be migrated to use V2 equivalents (`EquipmentCostBreakdown` /
  `RentalCostCalculationResult`) before the V1 types are deleted in FR-TR-03. The `RentalReturnResponse` shape
  **changes**: per-item cost breakdown fields are removed. The response carries the updated `Rental` object only
  (status, `finalCost` aggregate). This is a **breaking API change**.

---

## 3. Abstract Data Schema Changes

No new schema changes are required for this story. All necessary columns (`equipment_type_slug`, `special_tariff_id`,
`special_price`, `discount_percent`) were added as part of FR-TR-01. The existing `rental_equipment.tariff_id`
column is nullable (set in FR-TR-01) and is ignored by settlement.

---

## 4. Component Contracts & Payloads

* **Interaction: External Client → `RentalCommandController` (POST /api/rentals/return)**
    * **Protocol:** REST / HTTP
  * **Payload Changes (Request):** `ReturnEquipmentRequest` is unchanged.
  * **Payload Changes (Response — Breaking):** `RentalReturnResponse` no longer contains a per-item cost
    breakdown. The response body is the updated `Rental` object only (including status and aggregate
    `finalCost`). Callers requiring itemised cost details must invoke the idempotent
    `POST /api/tariffs/calculation` endpoint separately.

* **Interaction: `ReturnEquipmentService` → `TariffV2Facade`**
    * **Protocol:** In-process synchronous call (Spring Modulith Facade)
    * **Payload Changes:**
        * Command: `RentalCostCalculationCommand` containing `equipments` (list of `EquipmentCostItem` with
          `equipmentTypeSlug` for each returned item), `actualDuration` (billable minutes from
          `RentalDurationCalculatorImpl`), `plannedDuration`, `rentalDate` (from `Rental.startedAt`), and stored
          overrides `discountPercent`, `specialTariffId`, `specialPrice` from the `Rental` aggregate
        * Response: `RentalCostCalculationResult` with `totalCost()` (final cost after forgiveness and discount)
          and `equipmentBreakdowns()` (per-item final cost)

* **Interaction: `ReturnEquipmentService` → `FinanceFacade` (settleRental)**
    * **Protocol:** In-process synchronous call (Spring Modulith Facade)
    * **Payload Changes:** `settleRental(rentalId, totalCost)` where `totalCost` is
      `RentalCostCalculationResult.totalCost()`. Called only on final return when all equipment items are
      RETURNED.

---

## 5. Updated Interaction Sequence

### Happy Path — Single Equipment Return (Within Forgiveness Window)

1. External client sends `POST /api/rentals/return` for a rental with one bicycle (discountPercent = 0).
2. `RentalCommandController` forwards to `ReturnEquipmentService`.
3. `ReturnEquipmentService` loads `Rental` via `RentalRepository`; validates status = ACTIVE.
4. `RentalDurationCalculatorImpl` computes billable minutes for the returned bicycle (e.g., 2h 5min actual →
   rounded to 2h 5min or nearest increment, whichever applies).
5. `ReturnEquipmentService` builds `RentalCostCalculationCommand` with one `EquipmentCostItem` (bicycle type slug),
   actual duration, planned duration, rental date, and no discount overrides.
6. `TariffV2Facade.calculateRentalCost(command)` applies forgiveness internally; returns `totalCost` based on
   planned 2h (overtime forgiven).
7. `RentalEquipment.finalCost` is set from the V2 per-item breakdown.
8. All equipment is returned → `Rental` status set to COMPLETED.
9. `ReturnEquipmentService` calls `FinanceFacade.settleRental(rentalId, totalCost)`.
10. `RentalRepository.save` persists the closed `Rental`.
11. `SpringApplicationEventPublisher` publishes `RentalCompleted`.
12. Response returned to client.

### Happy Path — Multi-Item Final Return with Discount

1. External client sends `POST /api/rentals/return` for a rental with bicycle and child seat (discountPercent = 10).
2. `ReturnEquipmentService` loads the `Rental` aggregate; validates ACTIVE.
3. `RentalDurationCalculatorImpl` computes billable minutes for each item.
4. `ReturnEquipmentService` builds `RentalCostCalculationCommand` with two `EquipmentCostItem` entries and
   `discountPercent = 10`.
5. `TariffV2Facade.calculateRentalCost(command)` calculates both items in one batch, applies 10% discount to
   the non-special subtotal; returns `totalCost` and per-item breakdown.
6. `RentalEquipment.finalCost` is set for each item.
7. All equipment returned → `FinanceFacade.settleRental(rentalId, discountedTotal)` called.
8. `Rental` closed; `RentalCompleted` event published.

### Happy Path — Partial Return

1. External client sends `POST /api/rentals/return` for only one of two rented bicycles.
2. `ReturnEquipmentService` loads the `Rental`; validates ACTIVE.
3. `RentalDurationCalculatorImpl` computes billable minutes for the returned bicycle only.
4. `RentalCostCalculationCommand` built with one `EquipmentCostItem`.
5. `TariffV2Facade.calculateRentalCost(command)` returns partial cost for that item.
6. `RentalEquipment.finalCost` is set for the returned item; the other item remains unreturned.
7. `Rental` status remains ACTIVE; `settleRental` is NOT called.
8. `RentalRepository.save` persists the partially-updated aggregate.
9. Response returned (rental still open).

### Happy Path — Final Return with SPECIAL Pricing

1. External client sends `POST /api/rentals/return` for all equipment on a rental with `specialTariffId = 99`
   and `specialPrice = 15.00`.
2. `ReturnEquipmentService` reads `Rental.specialTariffId = 99` and `Rental.specialPrice = 15.00`.
3. `RentalCostCalculationCommand` built with `specialTariffId = 99` and `specialPrice = 15.00`.
4. `TariffV2Facade.calculateRentalCost(command)` bypasses per-item rate calculation; returns `totalCost = 15.00`.
5. `FinanceFacade.settleRental(rentalId, 15.00)` called; `Rental` closed; `RentalCompleted` published.

### Verification Sequence — No V1 TariffFacade Import Remains

1. After FR-TR-02 changes are applied, any code search for `TariffFacade` import statements within the rental
   module packages returns zero results.
2. `CreateRentalService`, `UpdateRentalService`, and `ReturnEquipmentService` all depend only on `TariffV2Facade`.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** After this story, the Rental module must contain zero import references to `TariffFacade`
  (V1) or any V1 tariff DTO types (`TariffInfo`, `RentalCost`). All cost computation is fully delegated to the
  `TariffV2Facade` boundary.

* **Scale & Performance:** The single batch `TariffV2Facade.calculateRentalCost` call for all returned items
  replaces the prior N individual V1 calls (one per returned item). Settlement (including the `settleRental` call)
  must complete within the existing 2-second transaction time budget for final return.

* **Idempotency:** Callers needing a cost breakdown after return must use `POST /api/tariffs/calculation`.
  That endpoint is idempotent — repeated calls with the same inputs produce the same result without side effects
  on the rental or finance state.
