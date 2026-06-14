# System Design: FR-2 — PUT /api/tariffs/calculations — Per-Equipment V2 Cost Calculation

## 1. Architectural Overview

The existing `POST /api/tariffs/calculate` endpoint uses a single shared billing duration for all equipment items in
a rental. This makes it impossible to calculate correct costs when different items are returned at different times
(partial returns), or when a flat-fee item spans a date boundary for a different period than another item.

This FR introduces a second calculation endpoint — `PUT /api/tariffs/calculations` — backed by a parallel V2
calculation stack within the tariff module. The new stack accepts a global rental `startAt` datetime and per-item
`returnAt` timestamps, derives each item's individual billing duration, and applies the FR-1 calendar-day rule for
flat-fee tariffs. The rest of the tariff module's structure (forgiveness logic, tariff selection, discount, special
tariff mode) is preserved. The facade contract (`TariffV2Facade`) is extended with a single new method so that the
rental module can invoke the same logic in-process without going through HTTP.

No data store schema changes are required; the endpoint is a pure calculation utility.

---

## 2. Impacted Components

* **`TariffV2CalculationController` (Tariff Web Query Layer):** Gains a new handler method mapped to
  `PUT /api/tariffs/calculations`. The handler validates the incoming V2 request, delegates to
  `TariffV2Facade.calculateRentalCostV2(...)`, and maps the result to `CostCalculationResponse`. It follows the
  same structural pattern as the existing `/calculate` handler.

* **`TariffV2Facade` (Tariff Module Facade Interface):** Gains a new method
  `calculateRentalCostV2(RentalCostCalculationV2Command): RentalCostCalculationResult`. This is the only
  cross-module entry point for V2 calculation; the rental module may call it in-process.

* **`TariffV2FacadeImpl` (Tariff Module Facade Implementation):** Implements the new facade method by delegating to
  `RentalCostCalculationV2UseCase`.

* **`RentalCostCalculationV2UseCase` (New Application Use-Case Port):** New interface in the tariff application
  layer that defines the V2 calculation contract for the application service.

* **`RentalCostCalculationV2Service` (New Application Service):** Implements `RentalCostCalculationV2UseCase`.
  Executes the per-item billing logic: derives each item's actual duration from `command.startAt()` to
  `item.returnAt()`, applies the forgiveness threshold against the global planned duration, selects one tariff per
  equipment type using `command.startAt().toLocalDate()`, invokes `tariff.calculateCost(startAt, returnAt, timezone)`
  (enhanced per FR-1), and aggregates per-item breakdowns into `RentalCostCalculationResult`. Special tariff mode
  and global discount are handled identically to `RentalCostCalculationService`.

* **`RentalCostCalculationV2Command` (Tariff Module Command Record):** Updated to reflect the revised request
  structure: a global `startAt` (`LocalDateTime`), a global `plannedDuration` (`Duration`), optional
  `discountPercent`, optional `specialTariffId` / `specialPrice`, and a list of `EquipmentCostItemV2` entries.
  The `rentalDate` field is removed; tariff selection date is derived from `startAt`.

* **`EquipmentCostItemV2` (Tariff Module Value Object):** Updated to remove the per-item `startAt` field (now
  global). Retains `equipmentId` (Long), `equipmentType` (String), and `returnAt` (`LocalDateTime`, nullable).

* **`CostCalculationResponse.EquipmentCostBreakdownResponse` (Shared Response Record):** Gains an `equipmentId`
  (Long) field so callers can correlate breakdown lines back to individual physical equipment units.

* **`BatchCalculationMapper` (Tariff Web Mapper):** Extended to map `CostCalculationV2Request` →
  `RentalCostCalculationV2Command` and to include `equipmentId` when producing `EquipmentCostBreakdownResponse`
  entries from V2 results.

---

## 3. Abstract Data Schema Changes

No persistent data schema changes. All computation is in-memory. No new database tables, columns, or relations.

---

## 4. Component Contracts & Payloads

* **Interaction: External Client → `TariffV2CalculationController`**
    * **Protocol:** REST (HTTP PUT)
    * **Payload Changes:** New request body `CostCalculationV2Request`:

      | Field | Type | Required |
            |---|---|---|
      | `equipments` | List of per-item objects | Yes |
      | `startAt` | LocalDateTime (ISO-8601) | Yes |
      | `plannedDurationMinutes` | Integer (> 0) | Yes |
      | `discountPercent` | Integer (0–100) | No |
      | `specialTariffId` | Long | No |
      | `specialPrice` | Decimal (≥ 0) | No |

      **Per-item object:**

      | Field | Type | Required |
            |---|---|---|
      | `equipmentId` | Long | Yes |
      | `equipmentType` | String | Yes |
      | `returnAt` | LocalDateTime (ISO-8601) | No |

      **Response** reuses `CostCalculationResponse` with one addition: each `EquipmentCostBreakdownResponse` entry
      now includes `equipmentId` (Long). Error responses follow the existing `ProblemDetail` structure.

* **Interaction: `TariffV2CalculationController` → `TariffV2Facade`**
    * **Protocol:** In-process method call (Spring bean)
    * **Payload Changes:** New method `calculateRentalCostV2(RentalCostCalculationV2Command)` added to the
      `TariffV2Facade` interface. The return type is the existing `RentalCostCalculationResult`.

* **Interaction: `TariffV2FacadeImpl` → `RentalCostCalculationV2UseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** Delegates `RentalCostCalculationV2Command` directly.

* **Interaction: `RentalCostCalculationV2Service` → `TariffV2.calculateCost`**
    * **Protocol:** In-process domain method call (enhanced per FR-1)
    * **Payload Changes:** Passes `(command.startAt(), item.returnAt(), serverTimezone)`. For estimate-mode items
      (null `returnAt`), `returnAt` is synthesised as `command.startAt() + plannedDuration`.

* **Interaction: Rental Module → `TariffV2Facade`**
    * **Protocol:** In-process Spring Modulith Facade call
    * **Payload Changes:** The rental module may now call `TariffV2Facade.calculateRentalCostV2(...)` directly.
      This crosses the module boundary only through the published facade interface; no rental module class imports
      any type from the tariff domain layer.

---

## 5. Updated Interaction Sequence

**Happy path — partial return (two items, different returnAt times)**

1. External client sends `PUT /api/tariffs/calculations` with `startAt`, two equipment items each having a distinct
   `returnAt`, and `plannedDurationMinutes`.
2. `TariffV2CalculationController` validates the request body (JSR-380); returns HTTP 400 + `ProblemDetail` on
   failure.
3. `BatchCalculationMapper` maps the request to `RentalCostCalculationV2Command`.
4. `TariffV2CalculationController` calls `TariffV2Facade.calculateRentalCostV2(command)`.
5. `TariffV2FacadeImpl` delegates to `RentalCostCalculationV2UseCase.execute(command)`.
6. `RentalCostCalculationV2Service` checks for special tariff mode — not active in this path.
7. For each distinct `equipmentType`, `RentalCostCalculationV2Service` calls
   `SelectTariffV2UseCase.execute(equipmentType, plannedDuration, startAt.toLocalDate())`. Result is cached per type.
   Returns HTTP 404 + `ProblemDetail` if no tariff found.
8. For **item A**:
    * Actual duration = `startAt → item.returnAt`.
    * Forgiveness check: overrun = `actualDuration − plannedDuration`. If overrun ≤ threshold → billed = planned;
      else billed = actual.
    * Calls `tariff.calculateCost(startAt, returnAt_A, serverTimezone)`.
    * `FlatFeeTariffV2` (if applicable) counts distinct calendar dates per FR-1.
    * Appends `EquipmentCostBreakdown` including `equipmentId` for item A.
9. For **item B**: same as step 8 using `returnAt_B`.
10. `RentalCostCalculationV2Service` aggregates breakdowns, applies optional global discount, and returns
    `RentalCostCalculationResult`.
11. `BatchCalculationMapper` maps the result to `CostCalculationResponse` (with `equipmentId` per breakdown entry).
12. `TariffV2CalculationController` returns HTTP 200.

**Unhappy path — special tariff mode**

* Steps 1–6 as above; special tariff ID is present.
* `RentalCostCalculationV2Service` loads the special tariff, validates it has `PricingType.SPECIAL`, validates
  `specialPrice ≥ 0`, sets each item's breakdown cost to zero, sets `specialPricingApplied = true`, and returns
  `specialPrice` as the total.

**Unhappy path — estimate mode (one item has no returnAt)**

* For the item with `returnAt = null`, `RentalCostCalculationV2Service` synthesises
  `returnAt = startAt + plannedDuration`.
* The item is billed at planned duration with no overtime. `estimate = true` is set on the result.

**Unhappy path — rental module in-process call**

* `ReturnEquipmentService` (or any future rental-module service) calls
  `TariffV2Facade.calculateRentalCostV2(command)` directly in-process.
* Execution follows steps 5–10 of the happy path. No HTTP round-trip occurs.

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No change. All tariff endpoints are currently open. No new security surface is introduced.
  Request body validation (JSR-380) guards against malformed input and returns structured `ProblemDetail` errors
  with `errorCode = CONSTRAINT_VIOLATION`.

* **Scale & Performance:** Tariff selection is cached per distinct `equipmentType` within a single request
  execution (Map-based in-memory cache, same pattern as V1). No additional database queries per item are introduced
  beyond one tariff lookup per distinct equipment type. Total DB reads = number of distinct equipment types in the
  request.
