# US-TR-007: Tariff V2 — New Pricing Model

**Status:** Pending  
**Priority:** HIGH  
**Module:** tariff  
**Created:** 2026-03-18  
**Dependencies:** US-TR-001 ✅ (existing tariff infrastructure)  
**Integration:** Rental module integration will be done separately

---

## Latest decisions summary

- Pricing calculation implementation: moved into `TariffV2` subclasses. Each tariff implements `calculateCost(Duration)` and returns `RentalCostV2`.
- Removed `PricingStrategyV2` and its implementations + factory. Calculation code was migrated into `domain/model/*TariffV2.java` classes.
- `SelectTariffV2ForRentalUseCase` now returns a `TariffV2` (selection only). Callers should then obtain cost via `tariff.calculateCost(duration)`.
- `BatchRentalCostCalculationService` handles SPECIAL mode as a group-level short-circuit (no per-item calculations) and uses `tariff.calculateCost(...)` for normal items.


## 1. Business Context

The current tariff model (V1) uses a flat period-based pricing approach: a single price per period
(HALF_HOUR / HOUR / DAY) with the cheapest tariff auto-selected. The business requires a fundamentally
different pricing model that supports **degressive hourly pricing** (decreasing rate per hour),
**flat hourly pricing**, **one-time issuance fees**, **daily rentals with overtime**, and
**operator-defined special pricing**.

The new model (V2) must coexist with V1 — new tables, new domain classes (suffixed with `V2`),
and new endpoints (`/api/v2/tariffs`). No changes to existing V1 implementation.

### Design Principles

1. **No equipment-type hardcoding.** The code must never contain `if (type == "bicycle")` or similar
   branching. Equipment type is always a lookup parameter — never a business-logic discriminator.
   Any equipment type (bicycle, scooter, child_seat, segway, etc.) can be added dynamically
   and assigned any pricing type through tariff configuration via API.
2. **All pricing parameters are API-configurable.** Every numerical value used in cost calculation
   (`firstHourPrice`, `hourlyDiscount`, `minimumHourlyPrice`, `minimumDurationSurcharge`,
   `overtimeHourlyPrice`, etc.) is a per-tariff field editable through the tariff CRUD API.
   No global constants or hardcoded values.
3. **Automatic tariff selection.** Given an equipment type and a duration, the system evaluates
   all active tariffs for that equipment type, calculates the cost with each, and selects
   the cheapest result. This allows the same equipment to have multiple tariffs (hourly, daily)
   with the optimal one chosen automatically.
4. **Duration-sensitive recalculation.** Initial cost uses planned duration; final cost uses actual
   duration. If the actual duration falls into a different pricing bracket, the system re-selects
   the optimal tariff. Example: customer plans 1h (hourly tariff selected), returns after 30 min
   (system recalculates and may select a cheaper tariff for that duration).

---

## 2. Business Requirements

### 2.1 Pricing Types

| Pricing Type          | Equipment Example | Description                                                                                                           |
|-----------------------|-------------------|-----------------------------------------------------------------------------------------------------------------------|
| **DEGRESSIVE_HOURLY** | Bicycle           | First hour at base rate, each subsequent hour discounted by a fixed step. Floor price applies after reaching minimum. |
| **FLAT_HOURLY**       | Scooter           | Constant hourly rate, no discounts. Cost = hours * rate.                                                              |
| **DAILY**             | Bicycle, Scooter  | Fixed 24-hour rate. Overtime charged at a per-hour rate.                                                              |
| **FLAT_FEE**          | Child seat        | Per-day issuance fee. Constant within one 24h period, multiplied by number of days.                                   |
| **SPECIAL**           | Any               | Operator-set fixed price (can be zero). All other pricing rules are bypassed.                                         |

### 2.2 Degressive Hourly Pricing (Bicycles)

**Parameters:** `firstHourPrice`, `hourlyDiscount`, `minimumHourlyPrice`

**Formula for hour N:** `max(firstHourPrice − (N−1) * hourlyDiscount, minimumHourlyPrice)`

**Examples** (firstHourPrice=9, hourlyDiscount=2, minimumHourlyPrice=1):

| Duration | Calculation           | Total |
|----------|-----------------------|-------|
| 1 hour   | 9                     | 9     |
| 2 hours  | 9 + 7                 | 16    |
| 3 hours  | 9 + 7 + 5             | 21    |
| 4 hours  | 9 + 7 + 5 + 3         | 24    |
| 5 hours  | 9 + 7 + 5 + 3 + 1     | 25    |
| 6 hours  | 9 + 7 + 5 + 3 + 1 + 1 | 26    |
| 10 hours | 9 + 7 + 5 + 3 + 1*6   | 30    |

After reaching the minimum hourly price, all subsequent hours are charged at that minimum.

### 2.3 Incomplete Hour Billing

When the last hour is not fully used, charge proportionally per 5-minute interval:

1. Count full completed hours — charge each at its degressive (or flat) rate
2. For the remaining minutes (already rounded down to 5 min by rental module):
    - Determine the rate for the next hour
    - Per 5-minute rate = `hourRate / 12` (since 60 min / 5 min = 12 intervals)
   - Remaining cost = `intervals * perFiveMinRate`

**Example** (bicycle, 1h 20min):

- Hour 1: 9 BYN
- Remaining 20 min = 4 intervals, hour 2 rate = 7 BYN, per 5-min = 7/12 ≈ 0.58
- Remaining cost: 4 * 0.58 = 2.33 BYN
- **Total: 11.33 BYN**

### 2.4 Minimum Duration Tariff (30 Minutes)

For hourly pricing types (DEGRESSIVE_HOURLY, FLAT_HOURLY), a minimum rental duration of 30 minutes applies.

**Formula:** `firstHourPrice / 2 + minimumDurationSurcharge`

**Parameters:** `minimumDurationMinutes` (default 30), `minimumDurationSurcharge` (e.g., 1 BYN)

**Example** (bicycle): 9 / 2 + 1 = 5.50 BYN for any rental ≤ 30 minutes.

### 2.5 Daily Rental

**Parameters:** `dailyPrice`, `overtimeHourlyPrice` (configurable per tariff via API)

- Base charge: `dailyPrice` for up to 24 hours
- Overtime after 24h: `overtimeHourlyPrice` per extra hour (proportional to 5-min intervals)
- `overtimeHourlyPrice` is set independently per DAILY tariff — not derived from other tariffs
- Multi-day rentals (> 24h significant overtime): handled via SPECIAL tariff (individual negotiation)

**Example** (dailyPrice=20, overtimeHourlyPrice=1):

- 24 hours: 20 BYN
- 25 hours: 20 + 1 = 21 BYN
- 26h 30min: 20 + 2 + 6*(1/12) = 22.50 BYN

### 2.6 Flat Fee (Child Seats)

**Parameters:** `issuanceFee`

Per-day charge for the fact of issuance. Within a single 24-hour period, the price is
constant. If the rental spans multiple days, the fee is multiplied.

**Formula:** `cost = issuanceFee * ceil(durationMinutes / 1440)`

**Examples** (issuanceFee=1):
| Duration | Days (ceil) | Cost |
|---|---|---|
| 30 min | 1 | 1 BYN |
| 12 hours | 1 | 1 BYN |
| 24 hours | 1 | 1 BYN |
| 25 hours | 2 | 2 BYN |
| 48 hours | 2 | 2 BYN |
| 49 hours | 3 | 3 BYN |

### 2.7 Special Pricing

**Parameters:** none stored in tariff — price is provided by the operator at rental time

- A tariff with `pricingType = SPECIAL` is a marker — it has no pricing fields
- The operator provides `specialPrice` when creating or editing the rental
- `specialPrice` can be zero (free rental) or any positive value
- All duration-based calculation rules are bypassed
- Cost = `specialPrice` regardless of duration
- **Discount is NOT applied** to SPECIAL-priced items (see §2.9)

### 2.8 Forgiveness Rule

**Parameter:** `app.rental.forgiveness.overtime-duration` in `application.yaml`
(global constant, same for all tariffs and equipment types)

The existing V1 property `RentalProperties.ForgivenessProperties.overtimeDuration` is reused.
This value is NOT per-tariff and NOT editable via API — it is an application-level constant.

When actual rental duration exceeds planned duration by no more than the forgiveness threshold,
the overtime is forgiven — the customer is charged based on **planned** duration, not actual.

**Conditions:**

- Applies only during **final** calculation (when `actualDurationMinutes` is provided)
- Applies only when `actualDuration > plannedDuration` (overtime scenario)
- If `actualDuration − plannedDuration ≤ forgivenessMinutes` → **forgiven**, charge for `plannedDuration`
- If `actualDuration − plannedDuration > forgivenessMinutes` → **not forgiven**, charge for `actualDuration`
- When forgiven, tariff auto-selection re-runs with `plannedDuration`
  (in case a different tariff is cheaper for the shorter duration)
- Does NOT apply to SPECIAL pricing type (duration-irrelevant)
- Applies to FLAT_FEE (per-day pricing — forgiveness prevents crossing into the next day)

**Examples** (forgivenessMinutes=7, planned 60min):
| Actual | Overtime | Forgiven? | Billed for |
|---|---|---|---|
| 55 min | −5 (early) | n/a | 55 min (actual) |
| 60 min | 0 | n/a | 60 min |
| 65 min | +5 | ✓ yes | 60 min (planned) |
| 67 min | +7 | ✓ yes | 60 min (planned) |
| 68 min | +8 | ✗ no | 68 min (actual) |
| 90 min | +30 | ✗ no | 90 min (actual) |

**Result transparency:** when forgiveness is applied, the per-item breakdown includes
`forgivenessApplied=true`, `overtimeMinutes`, and `forgivenMinutes` so the operator
can see what happened.

### 2.9 Operator Discount

**Parameter:** `discountPercent` (0–100, passed at calculation time, NOT stored in tariff)

- Discount is a percentage set by the operator at rental level
- **Discount applies only to non-SPECIAL items.** SPECIAL-priced items are excluded from discount.
- Discount is NOT a tariff field — it is a runtime parameter provided when calculating the final cost
- Discount percent must be in range [0, 100]. A value of 0 means no discount.

**Two mutually exclusive modes:**

**Normal mode** (no `specialTariffId`):

1. Calculate cost for each equipment item individually (via pricing strategy)
2. Sum all item costs → `subtotal`
3. Apply discount: `discountAmount = subtotal * discountPercent / 100`
4. Final cost: `totalCost = subtotal − discountAmount`

**SPECIAL mode** (`specialTariffId` is set):

1. Skip all per-item calculation
2. `totalCost = specialPrice` (operator-provided total for entire group)
3. Discount is **NOT applied** — `discountPercent` is ignored
4. `specialPricingApplied = true`

**Example — normal mode** (2 bicycles + 1 child seat, 2h, 10% discount):

- Bicycle 1: 16 BYN, Bicycle 2: 16 BYN, Child seat: 1 BYN
- Subtotal: 33 BYN
- Discount 10%: 3.30 BYN
- **Total: 29.70 BYN**

**Example — SPECIAL mode** (2 bicycles + 1 child seat, specialPrice=15):

- **Total: 15.00 BYN** (discount not applied, per-item costs = 0)

### 2.9 Automatic Tariff Selection

Given an `equipmentTypeSlug` and a `durationMinutes`, the system:

1. Finds all **active** tariffs for that equipment type that are **valid on the rental date**
2. Calculates the cost using each tariff's pricing strategy
3. Selects the tariff with the **lowest total cost**

This enables scenarios where the same equipment type has both hourly and daily tariffs:

- Bicycle, 3 hours: DEGRESSIVE_HOURLY → 21 BYN vs DAILY → 20 BYN → **picks DAILY**
- Bicycle, 1 hour: DEGRESSIVE_HOURLY → 9 BYN vs DAILY → 20 BYN → **picks DEGRESSIVE_HOURLY**

**Recalculation on return:** when equipment is returned, the system recalculates using actual
duration and re-selects the optimal tariff. If a different tariff is now cheaper, it is used
for the final cost.

**FLAT_FEE and SPECIAL tariffs** participate in selection normally. If a FLAT_FEE tariff
(e.g., child seat at 1 BYN) is the cheapest option for a given equipment type and duration,
it will be selected.

### 2.10 Time Rounding Rules

| Event         | Rounding      | Direction        |
|---------------|---------------|------------------|
| Rental start  | Nearest 5 min | **UP** (ceil)    |
| Rental return | Nearest 5 min | **DOWN** (floor) |

**Example:** Customer arrives 12:02, returns 14:23 → billable period: 12:05 – 14:20 = 2h 15min.

> Note: Time rounding is applied before calling the cost calculator. The tariff module receives
> the already-rounded duration. Integration with rounding will be implemented during rental module integration.

---

## 3. Domain Model Design

### 3.1 PricingType Enum

```java

@Getter
@RequiredArgsConstructor
public enum PricingType {
    DEGRESSIVE_HOURLY("tariff.pricing-type.degressive-hourly.title",
            "tariff.pricing-type.degressive-hourly.description"),
    FLAT_HOURLY("tariff.pricing-type.flat-hourly.title",
            "tariff.pricing-type.flat-hourly.description"),
    DAILY("tariff.pricing-type.daily.title",
            "tariff.pricing-type.daily.description"),
    FLAT_FEE("tariff.pricing-type.flat-fee.title",
            "tariff.pricing-type.flat-fee.description"),
    SPECIAL("tariff.pricing-type.special.title",
            "tariff.pricing-type.special.description");

    private final String codeTitle;
    private final String codeDescription;
}
```

**Message keys to add to `messages.properties` (EN):**

```properties
tariff.pricing-type.degressive-hourly.title=Degressive Hourly
tariff.pricing-type.degressive-hourly.description=Decreasing hourly rate with a floor price. Each subsequent hour is cheaper.
tariff.pricing-type.flat-hourly.title=Flat Hourly
tariff.pricing-type.flat-hourly.description=Constant hourly rate. Cost proportional to rental duration.
tariff.pricing-type.daily.title=Daily
tariff.pricing-type.daily.description=Fixed 24-hour rate with per-hour overtime charges.
tariff.pricing-type.flat-fee.title=Flat Fee
tariff.pricing-type.flat-fee.description=Per-day issuance fee. Constant within one 24-hour period.
tariff.pricing-type.special.title=Special
tariff.pricing-type.special.description=Operator-set fixed price. All calculation rules are bypassed.
```

**Message keys to add to `messages_ru.properties` (RU):**

```properties
tariff.pricing-type.degressive-hourly.title=Почасовой с понижением
tariff.pricing-type.degressive-hourly.description=Убывающая почасовая ставка с минимальным порогом. Каждый последующий час дешевле.
tariff.pricing-type.flat-hourly.title=Почасовой
tariff.pricing-type.flat-hourly.description=Постоянная почасовая ставка. Стоимость пропорциональна длительности аренды.
tariff.pricing-type.daily.title=Суточный
tariff.pricing-type.daily.description=Фиксированная суточная ставка с почасовой доплатой за превышение.
tariff.pricing-type.flat-fee.title=Фиксированная плата
tariff.pricing-type.flat-fee.description=Посуточная плата за выдачу. Постоянная в пределах одних суток.
tariff.pricing-type.special.title=Специальный
tariff.pricing-type.special.description=Цена задаётся оператором. Все правила расчёта игнорируются.
```

### 3.2 TariffV2 Aggregate Root

```java
public class TariffV2 {
    private Long id;
    private String name;
    private String description;
    private String equipmentTypeSlug;
    private PricingType pricingType;

    // DEGRESSIVE_HOURLY
    private Money firstHourPrice;
    private Money hourlyDiscount;
    private Money minimumHourlyPrice;

    // FLAT_HOURLY
    private Money hourlyPrice;

    // DAILY
    private Money dailyPrice;
    private Money overtimeHourlyPrice;

    // FLAT_FEE
    private Money issuanceFee;

    // SPECIAL — no pricing fields; price provided at runtime by operator

    // Minimum duration (DEGRESSIVE_HOURLY, FLAT_HOURLY)
    private Integer minimumDurationMinutes;
    private Money minimumDurationSurcharge;

    // Validity & status
    private LocalDate validFrom;
    private LocalDate validTo;
    private TariffV2Status status; // ACTIVE, INACTIVE
}
```

### 3.3 Field Applicability Matrix

| Field                    | DEG_HOURLY | FLAT_HOURLY |   DAILY    |  FLAT_FEE  | SPECIAL |
|--------------------------|:----------:|:-----------:|:----------:|:----------:|:-------:|
| firstHourPrice           | ✓ required |      —      |     —      |     —      |    —    |
| hourlyDiscount           | ✓ required |      —      |     —      |     —      |    —    |
| minimumHourlyPrice       | ✓ required |      —      |     —      |     —      |    —    |
| hourlyPrice              |     —      | ✓ required  |     —      |     —      |    —    |
| dailyPrice               |     —      |      —      | ✓ required |     —      |    —    |
| overtimeHourlyPrice      |     —      |      —      | ✓ required |     —      |    —    |
| issuanceFee              |     —      |      —      |     —      | ✓ required |    —    |
| (no tariff fields)       |     —      |      —      |     —      |     —      | SPECIAL |
| minimumDurationMinutes   | ✓ optional | ✓ optional  |     —      |     —      |    —    |
| minimumDurationSurcharge | ✓ optional | ✓ optional  |     —      |     —      |    —    |

---

## 4. Database Design

### 4.1 Table: `tariffs_v2`

```sql
CREATE TABLE tariffs_v2
(
    id                         BIGSERIAL PRIMARY KEY,
    name                       VARCHAR(200) NOT NULL,
    description                VARCHAR(1000),
    equipment_type_slug        VARCHAR(50)  NOT NULL,
    pricing_type               VARCHAR(50)  NOT NULL,

    -- DEGRESSIVE_HOURLY
    first_hour_price           DECIMAL(10, 2),
    hourly_discount            DECIMAL(10, 2),
    minimum_hourly_price       DECIMAL(10, 2),

    -- FLAT_HOURLY
    hourly_price               DECIMAL(10, 2),

    -- DAILY
    daily_price                DECIMAL(10, 2),
    overtime_hourly_price      DECIMAL(10, 2),

    -- FLAT_FEE
    issuance_fee               DECIMAL(10, 2),

    -- SPECIAL: no pricing columns (price provided at runtime by operator)

    -- Minimum duration (DEGRESSIVE_HOURLY, FLAT_HOURLY)
    minimum_duration_minutes   INT,
    minimum_duration_surcharge DECIMAL(10, 2),

    -- Validity
    valid_from                 DATE         NOT NULL,
    valid_to                   DATE,
    status                     VARCHAR(50)  NOT NULL DEFAULT 'INACTIVE',

    -- Audit
    created_at                 TIMESTAMPTZ  NOT NULL,
    updated_at                 TIMESTAMPTZ  NOT NULL
);
```

### 4.2 Seed Data

| name           | equipment_type_slug | pricing_type      | key fields                                         |
|----------------|---------------------|-------------------|----------------------------------------------------|
| Hourly Bicycle | bicycle             | DEGRESSIVE_HOURLY | first=9, discount=2, min=1, minDur=30, surcharge=1 |
| Hourly Scooter | scooter             | FLAT_HOURLY       | hourly=5, minDur=30, surcharge=1                   |
| Daily Bicycle  | bicycle             | DAILY             | daily=20, overtime=1                               |
| Daily Scooter  | scooter             | DAILY             | daily=25, overtime=2                               |
| Child Seat     | child_seat          | FLAT_FEE          | issuance=1                                         |
| Special Rate   | bicycle             | SPECIAL           | (no pricing fields — price set at rental time)     |

---

## 5. Class Structure (tariff module)

All new classes are placed under `tariff/` package alongside existing V1 classes.

```
tariff/
  ├── (existing V1 classes — untouched)
  │
  ├── TariffV2Facade.java                          ← public module API
  ├── TariffV2FacadeImpl.java
  ├── TariffV2Info.java                             ← public DTO record
  ├── RentalCostV2.java                             ← per-item cost (used internally by strategies)
  ├── RentalCostCalculationCommand.java             ← batch calculation input
  ├── RentalCostCalculationResult.java              ← batch calculation output (interface)
  ├── EquipmentCostBreakdown.java                   ← per-equipment detail (interface)
  ├── EquipmentCostItem.java                        ← per-equipment input
  ├── DiscountDetail.java                           ← discount percent + amount
  │
  ├── application/
  │   ├── mapper/
  │   │   ├── TariffV2CommandToDomainMapper.java
  │   │   └── TariffV2ToInfoMapper.java
  │   ├── service/
  │   │   ├── CreateTariffV2Service.java
  │   │   ├── UpdateTariffV2Service.java
  │   │   ├── GetTariffV2ByIdService.java
  │   │   ├── GetAllTariffsV2Service.java
  │   │   ├── GetActiveTariffsV2ByEquipmentTypeService.java
  │   │   ├── ActivateTariffV2Service.java
  │   │   ├── DeactivateTariffV2Service.java
  │   │   ├── GetPricingTypesService.java ← iterates PricingType enum, resolves i18n via MessageService
  │   │   ├── SelectTariffV2Service.java  ← auto-selects cheapest tariff per item
  │   │   └── BatchRentalCostCalculationService.java ← multi-item + discount orchestrator
  │   ├── usecase/
  │   │   ├── CreateTariffV2UseCase.java
  │   │   ├── UpdateTariffV2UseCase.java
  │   │   ├── GetTariffV2ByIdUseCase.java
  │   │   ├── GetAllTariffsV2UseCase.java
  │   │   ├── GetActiveTariffsV2ByEquipmentTypeUseCase.java
  │   │   ├── ActivateTariffV2UseCase.java
  │   │   ├── DeactivateTariffV2UseCase.java
  │   │   ├── GetPricingTypesUseCase.java  ← list pricing types with i18n
  │   │   ├── SelectTariffV2UseCase.java   ← auto-select by type + duration
  │   │   └── BatchRentalCostCalculationUseCase.java ← multi-item + discount
  │   └── validator/
  │       └── TariffV2PricingValidator.java         ← validates fields per PricingType
  │
  ├── domain/
  │   ├── model/
  │   │   ├── TariffV2.java                         ← aggregate root
  │   │   ├── TariffV2Status.java                   ← enum: ACTIVE, INACTIVE
  │   │   └── PricingType.java                      ← enum: 5 types
  │   ├── repository/
  │   │   └── TariffV2Repository.java               ← port interface
  │   └── service/
  │       ├── BaseRentalCostV2Result.java            ← RentalCostV2 implementation (per-item)
  │       └── BaseRentalCostCalculationResult.java   ← RentalCostCalculationResult impl (batch)
  │
  ├── infrastructure/
  │   └── persistence/
  │       ├── adapter/
  │       │   └── TariffV2RepositoryAdapter.java
  │       ├── entity/
  │       │   └── TariffV2JpaEntity.java
  │       ├── mapper/
  │       │   └── TariffV2JpaMapper.java
  │       └── repository/
  │           └── TariffV2JpaRepository.java
  │
  └── web/
      ├── command/
      │   ├── TariffV2CommandController.java        ← /api/v2/tariffs
      │   ├── dto/
      │   │   └── TariffV2Request.java
      │   └── mapper/
      │       └── TariffV2CommandMapper.java
      ├── query/
      │   ├── TariffV2QueryController.java          ← /api/v2/tariffs (GET endpoints)
      │   ├── TariffV2CalculationController.java    ← POST /api/v2/tariffs/calculate
      │   ├── dto/
      │   │   ├── TariffV2Response.java
      │   │   ├── PricingTypeResponse.java          ← { slug, title, description }
      │   │   ├── CostEstimateV2Response.java
      │   │   ├── BatchCostCalculationRequest.java  ← POST body for batch calculation
      │   │   └── BatchCostCalculationResponse.java ← response with breakdowns + totals
      │   └── mapper/
      │       ├── TariffV2QueryMapper.java
      │       └── BatchCalculationMapper.java       ← maps command/result ↔ request/response
      └── error/
          └── TariffV2RestControllerAdvice.java
```

---

## 6. API Endpoints

### 6.1 Command Endpoints (`TariffV2CommandController`)

| Method  | Path                              | Description                  |
|---------|-----------------------------------|------------------------------|
| `POST`  | `/api/v2/tariffs`                 | Create a new V2 tariff       |
| `PUT`   | `/api/v2/tariffs/{id}`            | Update an existing V2 tariff |
| `PATCH` | `/api/v2/tariffs/{id}/activate`   | Activate tariff              |
| `PATCH` | `/api/v2/tariffs/{id}/deactivate` | Deactivate tariff            |

### 6.2 Query Endpoints (`TariffV2QueryController`)

| Method | Path                            | Description                                                 |
|--------|---------------------------------|-------------------------------------------------------------|
| `GET`  | `/api/v2/tariffs/{id}`          | Get tariff by ID                                            |
| `GET`  | `/api/v2/tariffs`               | List all tariffs (paginated)                                |
| `GET`  | `/api/v2/tariffs/active`        | Active tariffs, optional `?equipmentType=` filter           |
| `GET`  | `/api/v2/tariffs/pricing-types` | List all pricing types with localized title and description |
| `GET`  | `/api/v2/tariffs/selection`     | Auto-select cheapest tariff for equipment type + duration   |
| `GET`  | `/api/v2/tariffs/cost-estimate` | Calculate cost for a specific tariff + duration             |

### 6.3 Pricing Types Endpoint

`GET /api/v2/tariffs/pricing-types`

Returns all available pricing types with localized title and description.
Localization is resolved via `Accept-Language` header using `MessageService`.

**Response:**

```json
[
  {
    "slug": "DEGRESSIVE_HOURLY",
    "title": "Degressive Hourly",
    "description": "Decreasing hourly rate with a floor price. Each subsequent hour is cheaper."
  },
  {
    "slug": "FLAT_HOURLY",
    "title": "Flat Hourly",
    "description": "Constant hourly rate. Cost proportional to rental duration."
  },
  {
    "slug": "DAILY",
    "title": "Daily",
    "description": "Fixed 24-hour rate with per-hour overtime charges."
  },
  {
    "slug": "FLAT_FEE",
    "title": "Flat Fee",
    "description": "Per-day issuance fee. Constant within one 24-hour period."
  },
  {
    "slug": "SPECIAL",
    "title": "Special",
    "description": "Operator-set fixed price. All calculation rules are bypassed."
  }
]
```

With `Accept-Language: ru`:

```json
[
  {
    "slug": "DEGRESSIVE_HOURLY",
    "title": "Почасовой с понижением",
    "description": "Убывающая почасовая ставка с минимальным порогом. Каждый последующий час дешевле."
  }
]
```

### 6.5 Tariff Selection Endpoint (single item)

`GET /api/v2/tariffs/selection?equipmentType={slug}&durationMinutes={min}&rentalDate={date}`

Evaluates all active tariffs for the equipment type, calculates cost with each,
returns the cheapest. Response includes the selected tariff details and cost breakdown.

`rentalDate` is optional (defaults to today). Used for `validFrom`/`validTo` filtering.

### 6.6 Cost Estimate Endpoint (single item, specific tariff)

`GET /api/v2/tariffs/cost-estimate?tariffId={id}&durationMinutes={min}`

Calculates cost for a **specific** tariff (no auto-selection). Returns detailed breakdown:
full hours with per-hour rates, partial hour interval cost, and total.

### 6.7 Batch Cost Calculation Endpoint (multi-item + discount)

`POST /api/v2/tariffs/calculate`

**Request body:**
**Normal flow (auto-select tariffs, apply discount):**

```json
{
  "equipments": [
    {
      "equipmentTypeSlug": "bicycle"
    },
    {
      "equipmentTypeSlug": "bicycle"
    },
    {
      "equipmentTypeSlug": "child_seat"
    }
  ],
  "plannedDurationMinutes": 120,
  "actualDurationMinutes": null,
  "discountPercent": 10,
  "rentalDate": "2026-03-18"
}
```

**SPECIAL flow (operator sets total price for entire group):**

```json
{
  "equipments": [
    {
      "equipmentTypeSlug": "bicycle"
    },
    {
      "equipmentTypeSlug": "bicycle"
    },
    {
      "equipmentTypeSlug": "child_seat"
    }
  ],
  "plannedDurationMinutes": 120,
  "specialTariffId": 6,
  "specialPrice": 15.00,
  "rentalDate": "2026-03-18"
}
```

When `specialTariffId` is set, all per-item calculation is bypassed.
`specialPrice` is the total for the entire group. `discountPercent` is ignored.

POST is used because the request body is complex (list of equipment items + parameters).
This is a **query** operation (no side effects) despite using POST.

Returns: `RentalCostCalculationResult` as described in §5.5.
This endpoint delegates to `TariffV2Facade.calculateRentalCost()`.

---

## 7. Cost Calculation Algorithms

### 7.1 Pricing calculation location

- The per-tariff pricing calculation has been moved into the tariff model. Each concrete tariff
  class implements the calculation via `TariffV2.calculateCost(Duration)` and returns a
  `RentalCostV2` instance (amount + breakdown). This removes the previous indirection through
  `PricingStrategyV2` and the factory. SPECIAL pricing remains a batch-level short-circuit (see §7.6).

Example (conceptual):

```java
TariffV2 tariff = // found active tariff
RentalCostV2 cost = tariff.calculateCost(billedDuration);
```

### 7.0 Automatic Tariff Selection Algorithm (SelectTariffV2ForRentalService)

```
Input: equipmentTypeSlug, durationMinutes, rentalDate (optional, defaults to today)
Output: TariffV2 + RentalCostV2 (selected tariff with its calculated cost)

1. activeTariffs = findAllActive(equipmentTypeSlug)
       .filter(t -> t.isValidOn(rentalDate))
2. if activeTariffs is empty → throw SuitableTariffNotFoundException

3. candidates = []
   for each tariff in activeTariffs:
       cost = tariff.calculateCost(duration)
       candidates.add( {tariff, cost} )

4. return candidates.minBy(cost.totalCost)
```

This ensures the cheapest tariff is always selected regardless of pricing type.
No equipment-type-specific logic — the algorithm is universal.

### 7.0.1 Forgiveness-Aware Duration Resolution (BatchRentalCostCalculationService)

The forgiveness threshold is injected from `RentalProperties.forgiveness.overtimeDuration`
(application constant, same for all tariffs). The service determines the **billed duration**
once, before per-item tariff selection:

```
Input: plannedDuration (Duration), actualDuration (Duration, nullable)
Injected: forgivenessThreshold = rentalProperties.forgiveness.overtimeDuration

if actualDuration is null → estimate mode:
    billedDuration = plannedDuration
    forgivenessApplied = false

else if actualDuration ≤ plannedDuration → returned on time or early:
    billedDuration = actualDuration
    forgivenessApplied = false

else → overtime:
    overtime = actualDuration − plannedDuration
    if overtime ≤ forgivenessThreshold:
        billedDuration = plannedDuration      ← forgiven, charge for planned
        forgivenessApplied = true
        forgiven = overtime
    else:
        billedDuration = actualDuration       ← not forgiven, charge for actual
        forgivenessApplied = false
```

All duration values are `Duration` objects internally. Conversion to `int` minutes
happens only at the web layer boundary (via `DurationMapper`).

Since the forgiveness threshold is global (not per-tariff), the `billedDuration` is
resolved **once** for the entire rental, then used for all equipment items.
Tariff auto-selection runs with `billedDuration`, so when forgiveness reduces the
duration, a cheaper tariff may be selected.

**Not applied in SPECIAL mode:** when `specialTariffId` is set, forgiveness logic
is skipped entirely (SPECIAL bypasses all calculation rules).

### 7.2 DegressiveHourlyPricingStrategy

```
Input: tariff, durationMinutes
Output: RentalCostV2

if durationMinutes <= 0 → return zero cost
if durationMinutes <= tariff.minimumDurationMinutes →
    return tariff.firstHourPrice / 2 + tariff.minimumDurationSurcharge

fullHours = durationMinutes / 60
remainingMinutes = durationMinutes % 60
totalCost = 0

for hour = 1..fullHours:
    rate = max(firstHourPrice − (hour−1) * hourlyDiscount, minimumHourlyPrice)
    totalCost += rate

if remainingMinutes > 0:
    nextHourRate = max(firstHourPrice − fullHours * hourlyDiscount, minimumHourlyPrice)
    intervals = remainingMinutes / 5
    perInterval = nextHourRate / 12
    totalCost += intervals * perInterval

return totalCost
```

### 7.3 FlatHourlyPricingStrategy

```
if durationMinutes <= 0 → zero
if durationMinutes <= minimumDurationMinutes →
    hourlyPrice / 2 + minimumDurationSurcharge

fullHours = durationMinutes / 60
remainingMinutes = durationMinutes % 60
totalCost = fullHours * hourlyPrice

if remainingMinutes > 0:
    intervals = remainingMinutes / 5
    perInterval = hourlyPrice / 12
    totalCost += intervals * perInterval

return totalCost
```

### 7.4 DailyPricingStrategy

```
if durationMinutes <= 0 → zero
if durationMinutes <= 1440 (24h) → dailyPrice

overtimeMinutes = durationMinutes − 1440
fullOvertimeHours = overtimeMinutes / 60
remainingOvertimeMin = overtimeMinutes % 60
totalCost = dailyPrice + fullOvertimeHours * overtimeHourlyPrice

if remainingOvertimeMin > 0:
    intervals = remainingOvertimeMin / 5
    perInterval = overtimeHourlyPrice / 12
    totalCost += intervals * perInterval

return totalCost
```

### 7.5 FlatFeePricingStrategy

```
if durationMinutes <= 0 → return issuanceFee  (minimum 1 day)

days = ceil(durationMinutes / 1440)
return issuanceFee * days
```

### 7.6 Special Pricing (group-level, handled in BatchRentalCostCalculationService)

SPECIAL pricing is NOT handled by a per-item strategy. It is resolved at the
**batch level** in `BatchRentalCostCalculationService`:

```
if command.specialTariffId is not null:
    validate specialTariffId → must exist and have pricingType = SPECIAL
    validate specialPrice → must be non-null and ≥ 0
    
    for each equipment in command.equipments:
        breakdown = EquipmentCostBreakdown(
            equipmentTypeSlug = equipment.equipmentTypeSlug,
            tariffId = command.specialTariffId,
            pricingType = SPECIAL,
            itemCost = Money.zero(),
            calculationBreakdown = "Special tariff applied to group"
        )
    
    return result(
        subtotal = specialPrice,
        discount = DiscountDetail.none(),     // discount NOT applied
        totalCost = specialPrice,
        specialPricingApplied = true
    )
```

No per-item pricing strategy is invoked. The `PricingStrategyV2Factory` does NOT
need a `SpecialPricingStrategy` — SPECIAL is a short-circuit at the batch level.

---

## 8. Validation Rules

### 8.1 TariffV2PricingValidator

Validates that required fields for each `PricingType` are present and valid:

| PricingType       | Required Fields                                    | Constraints                                                                                                                                             |
|-------------------|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| DEGRESSIVE_HOURLY | firstHourPrice, hourlyDiscount, minimumHourlyPrice | All > 0; minimumHourlyPrice ≤ firstHourPrice; hourlyDiscount > 0                                                                                        |
| FLAT_HOURLY       | hourlyPrice                                        | > 0                                                                                                                                                     |
| DAILY             | dailyPrice, overtimeHourlyPrice                    | dailyPrice > 0; overtimeHourlyPrice > 0                                                                                                                 |
| FLAT_FEE          | issuanceFee                                        | ≥ 0                                                                                                                                                     |
| SPECIAL           | (none on tariff)                                   | No pricing fields on tariff. `specialTariffId` + `specialPrice` validated in `RentalCostCalculationCommand` (≥ 0, required when specialTariffId is set) |

Fields not applicable to the given pricing type must be null.

### 8.2 General Validation

- `name`: required, max 200 chars
- `equipmentTypeSlug`: required, must reference an existing equipment type
- `pricingType`: required, valid enum value
- `validFrom`: required
- `validTo`: optional, must be after `validFrom` if present

---

## 9. Implementation Plan

### Phase 1: Domain & Infrastructure (foundation)

| #   | Subtask            | Description                                       |
|-----|--------------------|---------------------------------------------------|
| 1.1 | Domain model       | `TariffV2`, `TariffV2Status`, `PricingType` enums |
| 1.2 | Repository port    | `TariffV2Repository` interface                    |
| 1.3 | Database migration | Liquibase XML for `tariffs_v2` table              |
| 1.4 | JPA entity         | `TariffV2JpaEntity` with all columns              |
| 1.5 | JPA repository     | `TariffV2JpaRepository` (Spring Data)             |
| 1.6 | Repository adapter | `TariffV2RepositoryAdapter`                       |
| 1.7 | JPA mapper         | `TariffV2JpaMapper` (MapStruct)                   |

### Phase 2: Application Layer — CRUD

| #    | Subtask                  | Description                                                                                                                                                        |
|------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 2.1  | Pricing validator        | `TariffV2PricingValidator` — per-type field validation                                                                                                             |
| 2.2  | Create use case          | `CreateTariffV2UseCase` / `CreateTariffV2Service`                                                                                                                  |
| 2.3  | Update use case          | `UpdateTariffV2UseCase` / `UpdateTariffV2Service`                                                                                                                  |
| 2.4  | Get by ID use case       | `GetTariffV2ByIdUseCase` / `GetTariffV2ByIdService`                                                                                                                |
| 2.5  | Get all use case         | `GetAllTariffsV2UseCase` / `GetAllTariffsV2Service`                                                                                                                |
| 2.6  | Get active by type       | `GetActiveTariffsV2ByEquipmentTypeUseCase` / Service                                                                                                               |
| 2.7  | Activate/Deactivate      | `ActivateTariffV2UseCase`, `DeactivateTariffV2UseCase`                                                                                                             |
| 2.8  | Pricing types listing    | `GetPricingTypesUseCase`/`Service` — iterates `PricingType` enum, resolves `codeTitle`/`codeDescription` via `MessageService`, returns `List<PricingTypeResponse>` |
| 2.9  | i18n messages            | Add `tariff.pricing-type.*` keys to `messages.properties` and `messages_ru.properties`                                                                             |
| 2.10 | Command-to-domain mapper | `TariffV2CommandToDomainMapper`                                                                                                                                    |
| 2.11 | Tariff-to-info mapper    | `TariffV2ToInfoMapper`                                                                                                                                             |

### Phase 3: Pricing Strategies & Cost Calculation

| #    | Subtask                   | Description                                                                                                                                                            |
|------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 3.1  | Strategy interface        | `PricingStrategyV2` interface                                                                                                                                          |
| 3.2  | Degressive strategy       | `DegressiveHourlyPricingStrategy` with unit tests                                                                                                                      |
| 3.3  | Flat hourly strategy      | `FlatHourlyPricingStrategy` with unit tests                                                                                                                            |
| 3.4  | Daily strategy            | `DailyPricingStrategy` with unit tests                                                                                                                                 |
| 3.5  | Flat fee strategy         | `FlatFeePricingStrategy` with unit tests                                                                                                                               |
| 3.6  | Strategy factory          | `PricingStrategyV2Factory` (4 strategies; SPECIAL is handled at batch level, not as a strategy)                                                                        |
| 3.7  | Cost result               | `RentalCostV2` interface + `BaseRentalCostV2Result`                                                                                                                    |
| 3.8  | Cost calculation service  | `CalculateRentalCostV2Service` — single-item cost                                                                                                                      |
| 3.9  | Tariff selection service  | `SelectTariffV2ForRentalService` — evaluates all active tariffs, picks cheapest                                                                                        |
| 3.10 | Batch calculation service | `BatchRentalCostCalculationService` — two modes: (1) normal: iterates equipment, delegates per-item, applies discount; (2) SPECIAL: short-circuits with operator price |
| 3.11 | Discount logic            | Discount calculation within batch service (normal mode only): `subtotal × percent / 100`                                                                               |

### Phase 4: Web Layer

| #   | Subtask                | Description                                                                                                                                |
|-----|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| 4.1 | Request DTO            | `TariffV2Request` with per-type validation                                                                                                 |
| 4.2 | Response DTOs          | `TariffV2Response`, `CostEstimateV2Response`                                                                                               |
| 4.3 | Batch DTOs             | `BatchCostCalculationRequest`, `BatchCostCalculationResponse`                                                                              |
| 4.4 | Web mappers            | `TariffV2CommandMapper`, `TariffV2QueryMapper`, `BatchCalculationMapper` (Duration ↔ int minutes conversion via existing `DurationMapper`) |
| 4.5 | Command controller     | `TariffV2CommandController` — POST, PUT, PATCH                                                                                             |
| 4.6 | Query controller       | `TariffV2QueryController` — GET endpoints                                                                                                  |
| 4.7 | Calculation controller | `TariffV2CalculationController` — POST /calculate                                                                                          |
| 4.8 | Error handler          | `TariffV2RestControllerAdvice`                                                                                                             |

### Phase 5: Facade & Module API

| #   | Subtask          | Description                                         |
|-----|------------------|-----------------------------------------------------|
| 5.1 | Facade interface | `TariffV2Facade` — public methods for other modules |
| 5.2 | Facade impl      | `TariffV2FacadeImpl` — delegates to use cases       |
| 5.3 | Public DTO       | `TariffV2Info` record                               |

**TariffV2Facade methods:**

```java
public interface TariffV2Facade {

    Optional<TariffV2Info> findById(Long tariffId);

    /**
     * Batch cost calculation for multiple equipment items.
     * Handles tariff auto-selection, per-item cost, discount, and totals.
     *
     * Used by rental module for both estimates (planned duration)
     * and final calculations (actual duration).
     */
    RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command);
}
```

### 5.4 Facade Command (input)

```java
public record RentalCostCalculationCommand(
        List<EquipmentCostItem> equipments,
        Duration plannedDuration,
        Duration actualDuration,             // null → estimate mode (use planned)
        BigDecimal discountPercent,          // null or 0 → no discount; NOT applied when special
        Long specialTariffId,               // null → auto-select per item; non-null → SPECIAL for entire group
        BigDecimal specialPrice,            // required when specialTariffId is set; total price for ALL equipment
        LocalDate rentalDate                // null → today
) {
    public Duration effectiveDuration() {
        return actualDuration != null ? actualDuration : plannedDuration;
    }
}

public record EquipmentCostItem(
        String equipmentType             // required — for tariff lookup; no per-item overrides
)
```

- `equipments` — one entry per physical equipment unit (two bicycles = two entries
  with the same slug). Each item gets its own tariff selection + cost calculation
  in the **normal** flow.
- `specialTariffId` + `specialPrice` — when set, **all** equipment uses the SPECIAL
  tariff. `specialPrice` is the **total price for the entire rental group**, not per-item.
  All auto-selection and calculation logic is bypassed. Discount is NOT applied.
- `actualDuration` — provided at return time. When null, the system uses
  `plannedDuration` and marks the result as an estimate.
- All duration fields use `Duration` internally. Web DTOs convert to/from `int` (minutes).

### 5.5 Facade Result (output)

```java
public interface RentalCostCalculationResult {

    /** Per-equipment cost breakdown — one entry per input equipment item, same order.
     *  In SPECIAL mode: each item shows pricingType=SPECIAL, itemCost=Money.zero(). */
    List<EquipmentCostBreakdown> equipmentBreakdowns();

    /** Sum of all equipment costs before discount (normal mode only). */
    Money subtotal();

    /** Discount details (percent + calculated amount). Not applied in SPECIAL mode. */
    DiscountDetail discount();

    /** Final cost: subtotal − discountAmount (normal) or specialPrice (SPECIAL). */
    Money totalCost();

    /** Duration used for calculation (actual if provided, otherwise planned). */
    Duration effectiveDuration();

    /** True if calculated with plannedDuration (estimate), false if actualDuration (final). */
    boolean estimate();

    /** True if SPECIAL tariff was applied to the entire group. */
    boolean specialPricingApplied();
}
```

### 5.6 Per-Equipment Breakdown

```java
public interface EquipmentCostBreakdown {

    /** Equipment type slug (e.g. "bicycle"). */
    String equipmentTypeSlug();

    /** ID of the tariff used (auto-selected, or specialTariffId in SPECIAL mode). */
    Long tariffId();

    /** Tariff display name. */
    String tariffName();

    /** Pricing type of the selected tariff. */
    PricingType pricingType();

    /** Calculated cost for this single equipment item (before rental-level discount).
     *  In SPECIAL mode: Money.zero() (total is at the group level). */
    Money itemCost();

    /** Duration actually billed (may differ from effective if forgiveness applied). */
    Duration billedDuration();

    /** Whether the forgiveness rule was applied for this item. */
    boolean forgivenessApplied();

    /** Overtime (actual − planned). Duration.ZERO if returned on time or early. */
    Duration overtime();

    /** Duration forgiven (Duration.ZERO if forgiveness not applied). */
    Duration forgiven();

    /**
     * Human-readable calculation breakdown.
     * Examples:
     *   "5h degressive: 9+7+5+3+1 = 25.00"
     *   "2h 20min flat: 2*5 + 4*(5/12) = 11.67"
     *   "Flat fee: 1.00"
     *   "Daily + 2h overtime: 20 + 2*1 = 22.00"
     *   "Special tariff applied to group"
     *   "1h degressive: 9.00 (7 min overtime forgiven)"
     */
    String calculationBreakdown();
}
```

### 5.7 Discount Detail

```java
public record DiscountDetail(
        BigDecimal percent,
        Money amount
) {
    public static DiscountDetail none() {
        return new DiscountDetail(BigDecimal.ZERO, Money.zero());
    }
}
```

**Example 1 — Estimate, normal flow** (2 bicycles + 1 child seat, planned 2h, 10% discount):

```json
{
  "equipmentBreakdowns": [
    {
      "equipmentTypeSlug": "bicycle",
      "tariffId": 1,
      "tariffName": "Hourly Bicycle",
      "pricingType": "DEGRESSIVE_HOURLY",
      "itemCost": 16.00,
      "billedDurationMinutes": 120,
      "forgivenessApplied": false,
      "overtimeMinutes": 0,
      "forgivenMinutes": 0,
      "calculationBreakdown": "2h degressive: 9+7 = 16.00"
    },
    {
      "equipmentTypeSlug": "bicycle",
      "tariffId": 1,
      "tariffName": "Hourly Bicycle",
      "pricingType": "DEGRESSIVE_HOURLY",
      "itemCost": 16.00,
      "billedDurationMinutes": 120,
      "forgivenessApplied": false,
      "overtimeMinutes": 0,
      "forgivenMinutes": 0,
      "calculationBreakdown": "2h degressive: 9+7 = 16.00"
    },
    {
      "equipmentTypeSlug": "child_seat",
      "tariffId": 5,
      "tariffName": "Child Seat",
      "pricingType": "FLAT_FEE",
      "itemCost": 1.00,
      "billedDurationMinutes": 120,
      "forgivenessApplied": false,
      "overtimeMinutes": 0,
      "forgivenMinutes": 0,
      "calculationBreakdown": "Flat fee: 1.00"
    }
  ],
  "subtotal": 33.00,
  "discount": {
    "percent": 10,
    "amount": 3.30
  },
  "totalCost": 29.70,
  "effectiveDurationMinutes": 120,
  "estimate": true,
  "specialPricingApplied": false
}
```

**Example 2 — Final with forgiveness** (1 bicycle, planned 60min, actual 65min,
forgiveness threshold=7min, no discount):

```json
{
  "equipmentBreakdowns": [
    {
      "equipmentTypeSlug": "bicycle",
      "tariffId": 1,
      "tariffName": "Hourly Bicycle",
      "pricingType": "DEGRESSIVE_HOURLY",
      "itemCost": 9.00,
      "billedDurationMinutes": 60,
      "forgivenessApplied": true,
      "overtimeMinutes": 5,
      "forgivenMinutes": 5,
      "calculationBreakdown": "1h degressive: 9.00 (5 min overtime forgiven)"
    }
  ],
  "subtotal": 9.00,
  "discount": {
    "percent": 0,
    "amount": 0.00
  },
  "totalCost": 9.00,
  "effectiveDurationMinutes": 65,
  "estimate": false,
  "specialPricingApplied": false
}
```

**Example 3 — SPECIAL tariff, entire group** (2 bicycles + 1 child seat,
specialTariffId=6, specialPrice=15.00):

```json
{
  "equipmentBreakdowns": [
    {
      "equipmentTypeSlug": "bicycle",
      "tariffId": 6,
      "tariffName": "Special Rate",
      "pricingType": "SPECIAL",
      "itemCost": 0.00,
      "billedDurationMinutes": 120,
      "forgivenessApplied": false,
      "overtimeMinutes": 0,
      "forgivenMinutes": 0,
      "calculationBreakdown": "Special tariff applied to group"
    },
    {
      "equipmentTypeSlug": "bicycle",
      "tariffId": 6,
      "tariffName": "Special Rate",
      "pricingType": "SPECIAL",
      "itemCost": 0.00,
      "billedDurationMinutes": 120,
      "forgivenessApplied": false,
      "overtimeMinutes": 0,
      "forgivenMinutes": 0,
      "calculationBreakdown": "Special tariff applied to group"
    },
    {
      "equipmentTypeSlug": "child_seat",
      "tariffId": 6,
      "tariffName": "Special Rate",
      "pricingType": "SPECIAL",
      "itemCost": 0.00,
      "billedDurationMinutes": 120,
      "forgivenessApplied": false,
      "overtimeMinutes": 0,
      "forgivenMinutes": 0,
      "calculationBreakdown": "Special tariff applied to group"
    }
  ],
  "subtotal": 15.00,
  "discount": {
    "percent": 0,
    "amount": 0.00
  },
  "totalCost": 15.00,
  "effectiveDurationMinutes": 120,
  "estimate": true,
  "specialPricingApplied": true
}
```

### Phase 6: Testing

| #   | Subtask                | Description                                                                                         |
|-----|------------------------|-----------------------------------------------------------------------------------------------------|
| 6.1 | Domain model tests     | `TariffV2Test` — status transitions, validity checks                                                |
| 6.2 | Pricing strategy tests | Parameterized tests for each strategy (all examples from §2)                                        |
| 6.3 | Validator tests        | `TariffV2PricingValidatorTest` — valid/invalid combos                                               |
| 6.4 | Service tests          | Unit tests for all CRUD services                                                                    |
| 6.5 | Batch calc tests       | `BatchRentalCostCalculationServiceTest` — multi-item, discount, estimate vs final                   |
| 6.6 | Selection tests        | `SelectTariffV2ForRentalServiceTest` — auto-selection, cheapest wins                                |
| 6.7 | WebMvc tests           | `TariffV2CommandControllerTest`, `TariffV2QueryControllerTest`, `TariffV2CalculationControllerTest` |
| 6.8 | Component tests        | Cucumber feature files for V2 tariff scenarios                                                      |

### Phase 7: Seed Data & Documentation

| #   | Subtask             | Description                                         |
|-----|---------------------|-----------------------------------------------------|
| 7.1 | Seed data           | Liquibase CSV with example tariffs (§4.2)           |
| 7.2 | OpenAPI annotations | `@Tag`, `@Operation`, `@Schema` on all V2 endpoints |

---

## 10. Acceptance Criteria

### AC-1: CRUD Operations

- [ ] Operator can create a V2 tariff with any of the 5 pricing types
- [ ] Operator can update an existing V2 tariff
- [ ] Operator can activate/deactivate a V2 tariff
- [ ] Operator can list all V2 tariffs (paginated) and filter active by equipment type
- [ ] Validation rejects tariffs with missing or invalid fields for their pricing type

### AC-1b: Pricing Types Endpoint

- [ ] `GET /api/v2/tariffs/pricing-types` returns 5 entries (DEGRESSIVE_HOURLY, FLAT_HOURLY, DAILY, FLAT_FEE, SPECIAL)
- [ ] Each entry has `slug`, `title`, `description`
- [ ] `slug` matches enum value name (e.g. `"DEGRESSIVE_HOURLY"`)
- [ ] Default language (no `Accept-Language`) returns English titles and descriptions
- [ ] `Accept-Language: ru` returns Russian titles and descriptions
- [ ] Titles and descriptions come from `messages.properties` / `messages_ru.properties` via `MessageService`

### AC-2: Degressive Hourly Pricing

- [ ] 5 hours bicycle (firstHour=9, discount=2, min=1) → 25 BYN
- [ ] 6 hours → 26 BYN
- [ ] 10 hours → 30 BYN
- [ ] 1h 20min → 11.33 BYN (9 + 4*7/12)
- [ ] 25 min → 5.50 BYN (minimum duration: 9/2 + 1)

### AC-3: Flat Hourly Pricing

- [ ] 3 hours scooter (hourlyPrice=5) → 15 BYN
- [ ] 30 min → 3.50 BYN (minimum duration: 5/2 + 1)
- [ ] 1h 30min → 5 + 6*(5/12) = 7.50 BYN

### AC-4: Daily Pricing

- [ ] 24 hours (dailyPrice=20, overtime=1) → 20 BYN
- [ ] 25 hours → 21 BYN
- [ ] 26h 30min → 22.50 BYN

### AC-5: Flat Fee (per-day)

- [ ] Child seat (issuanceFee=1), 30 min → 1 BYN
- [ ] 12 hours → 1 BYN
- [ ] 24 hours → 1 BYN
- [ ] 25 hours → 2 BYN (2 days)
- [ ] 48 hours → 2 BYN
- [ ] 49 hours → 3 BYN (3 days)
- [ ] Zero-duration → 1 BYN (minimum 1 day)

### AC-6: Special Pricing (group-level)

- [ ] SPECIAL tariff with specialPrice=0, any duration → totalCost=0, specialPricingApplied=true
- [ ] SPECIAL tariff with specialPrice=15, 3 items → totalCost=15, all items show pricingType=SPECIAL, itemCost=0
- [ ] No duration-based calculation applied when SPECIAL
- [ ] specialTariffId set but specialPrice missing → validation error
- [ ] specialPrice < 0 → validation error
- [ ] specialTariffId references a non-SPECIAL tariff → validation error
- [ ] SPECIAL tariff has no pricing fields stored in DB
- [ ] Discount is completely ignored when specialTariffId is set
- [ ] specialTariffId and discountPercent both set → discountPercent ignored, no error

### AC-7: Validation

- [ ] DEGRESSIVE_HOURLY without firstHourPrice → 400 Bad Request
- [ ] FLAT_HOURLY with firstHourPrice set → 400 (inapplicable field)
- [ ] SPECIAL with any pricing field set (hourlyPrice, issuanceFee, etc.) → 400 (inapplicable field)
- [ ] Missing pricingType → 400
- [ ] minimumHourlyPrice > firstHourPrice → 400

### AC-8: Forgiveness Rule (global, from `app.rental.forgiveness.overtime-duration`)

- [ ] Planned 60min, actual 65min, threshold=7min → billed for 60min, forgivenessApplied=true, forgivenMinutes=5
- [ ] Planned 60min, actual 67min, threshold=7min → billed for 60min, forgivenessApplied=true, forgivenMinutes=7
- [ ] Planned 60min, actual 68min, threshold=7min → billed for 68min, forgivenessApplied=false
- [ ] Planned 60min, actual 55min → billed for 55min (early return, no forgiveness logic)
- [ ] Planned 60min, actual 60min → billed for 60min, no overtime
- [ ] Forgiveness threshold applies uniformly to all equipment items in the rental
- [ ] FLAT_FEE: forgiveness prevents day boundary crossing (planned 24h, actual 24h+5min → 1 day not 2)
- [ ] SPECIAL tariff: forgiveness irrelevant (cost = fixedPrice regardless)
- [ ] Forgiveness applied → tariff re-selected with planned duration (may pick cheaper tariff)
- [ ] Estimate mode (actual=null): forgiveness not evaluated, forgivenessApplied=false
- [ ] Result includes overtimeMinutes, forgivenMinutes, forgivenessApplied per equipment item
- [ ] Forgiveness threshold is NOT stored in tariff — read from `RentalProperties`

### AC-9: Batch Calculation & Discount

- [ ] Facade accepts multiple equipment items and returns per-item breakdowns + totals
- [ ] 2 bicycles + 1 child seat, 2h, no discount → subtotal=33, total=33
- [ ] Same as above with 10% discount → subtotal=33, discountAmount=3.30, total=29.70
- [ ] 50% discount on 1 bicycle 1h (cost=9) → total=4.50
- [ ] 0% discount → discountAmount=0, total=subtotal
- [ ] Discount percent > 100 → validation error
- [ ] Discount percent < 0 → validation error
- [ ] Discount and SPECIAL are mutually exclusive modes: SPECIAL ignores discount entirely
- [ ] Each equipment item in result has its own tariffId, tariffName, pricingType, itemCost, calculationBreakdown
- [ ] Result `estimate=true` when only plannedDuration is provided
- [ ] Result `estimate=false` when actualDuration is provided
- [ ] With actualDuration: tariff may be re-selected (planned 1h → actual 30min → cheaper tariff)

### AC-10: Automatic Tariff Selection

- [ ] `GET /api/v2/tariffs/selection?equipmentType=bicycle&durationMinutes=60` returns the cheapest active tariff
- [ ] Bicycle 3h: if DEGRESSIVE_HOURLY=21 and DAILY=20, selects DAILY
- [ ] Bicycle 1h: if DEGRESSIVE_HOURLY=9 and DAILY=20, selects DEGRESSIVE_HOURLY
- [ ] Bicycle 30min planned → returns after 30min: selects cheapest tariff for 30min duration
- [ ] Bicycle 1h planned → returns after 30min: recalculation selects cheapest tariff for 30min
- [ ] No active tariffs for equipment type → 404 with SuitableTariffV2NotFoundException
- [ ] Selection logic has zero references to specific equipment type slugs in code

### AC-11: Isolation

- [ ] V1 endpoints (`/api/tariffs`) continue to work unchanged
- [ ] V1 domain classes are not modified
- [ ] V1 database table `tariffs` is not modified
- [ ] Rental module is not modified

### AC-12: Cost Estimate Endpoint

- [ ] `GET /api/v2/tariffs/cost-estimate?tariffId=1&durationMinutes=300` returns correct cost breakdown
- [ ] Missing tariffId → 400
- [ ] Non-existent tariffId → 404
- [ ] durationMinutes ≤ 0 → 400 (for non-FLAT_FEE/SPECIAL types)

### AC-13: No Equipment Type Hardcoding

- [ ] Zero occurrences of string literals "bicycle", "scooter", "child_seat" etc. in tariff V2 Java code
- [ ] All pricing strategies accept any equipment type slug — no type-specific branching
- [ ] Adding a new equipment type requires zero code changes in tariff module

---

## 11. Resolved Questions

1. **Equipment types are dynamic.** No hardcoding of equipment type slugs in code.
   New types (child_seat, segway, etc.) can be added via the equipment module at any time.
   Tariffs reference equipment types by slug — purely as a lookup parameter.
   **→ Resolved: no code changes needed for new equipment types.**

2. **`minimumDurationSurcharge` is per-tariff, API-editable.** Each tariff stores its own
   surcharge value. Operator can set/change it through the tariff CRUD API.
   **→ Resolved: field on TariffV2, editable via PUT /api/v2/tariffs/{id}.**

3. **`overtimeHourlyPrice` is a separate configurable field on DAILY tariffs.**
   Set per-tariff via API. Not derived from other tariffs or parameters.
   **→ Resolved: independent field, operator sets the value they want.**

4. **Tariff is auto-selected** based on equipment type + duration. The system calculates
   cost with every active tariff for the equipment type and picks the cheapest.
   On return, recalculates with actual duration — may select a different tariff.
   **→ Resolved: auto-selection by cost, no manual operator choice needed.**

5. **No hardcoding anywhere.** All pricing parameters are per-tariff fields.
   Pricing strategies are generic — they work with any equipment type.
   The same strategy (e.g., DEGRESSIVE_HOURLY) can be assigned to any equipment type.
   **→ Resolved: fully parameterized, equipment-type-agnostic code.**

## 12. Remaining Open Questions

1. **Rounding edge case:** If a rental starts at exactly XX:00 or XX:05 (on a 5-min boundary),
   is the start time unchanged (no rounding needed)? (Assumed yes — will be confirmed during
   rental module integration.)

---

## 13. Technical Notes

- All monetary values use the existing `Money` value object (BigDecimal, scale=2, HALF_UP rounding)
- MapStruct compiler flags: `unmappedTargetPolicy=ERROR` — all fields must be explicitly mapped
- Follow existing project conventions: no inline comments, `@ApiTest` for WebMvc tests, AssertJ assertions
- `PricingStrategyV2Factory` is a Spring `@Component` that returns the correct strategy based on `PricingType`
- Pricing strategies are stateless — can be singletons
