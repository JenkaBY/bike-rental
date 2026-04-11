# Initial User Request — RENTAL-001

## Original Request

Integrate TariffV2 into the rental process (creation, updates, and settlement). The existing `TariffFacade` (V1) is
`@Deprecated(forRemoval=true)` and must be replaced by `TariffV2Facade` in all rental flows. Remove `TariffFacade`
from the tariff module once all consumers are migrated.

---

## Clarifications Captured

### Migration Scope

**Option A — Full switch selected:** All new rentals use V2 for creation and settlement. The V1 `TariffFacade` is
removed from the tariff module in a dedicated cleanup story. No dual-version flag or backward-compatibility shim is
needed.

### Special Pricing at Creation

The operator may set a SPECIAL tariff for the entire rental group at creation time. `CreateRentalRequest` gains
optional fields `specialTariffId` (Long) and `specialPrice` (Money). These are stored on the `Rental` aggregate and
re-used at settlement without requiring the operator to re-enter them.

### Discount at Creation

The operator discount (`discountPercent`, 0–100, integer) is applicable at rental creation and affects the planned
cost passed to `holdFunds`. The discount is stored on the `Rental` aggregate and re-applied automatically at
settlement.

### RentalEquipment.tariffId

V2 auto-selects the cheapest tariff per item at calculation time; no tariff ID needs to be pre-selected or stored.
`RentalEquipment.tariffId` becomes nullable and is populated from the V2 per-item breakdown result where available,
or dropped entirely — to be decided during technical design.

### Forgiveness Rule

The forgiveness rule (up to N minutes overtime forgiven) is handled entirely inside `TariffV2Facade`. The rental
module passes actual duration; the tariff module applies forgiveness internally.

### Applicable FRs

| FR       | Title                                         | Scope                                                           |
|----------|-----------------------------------------------|-----------------------------------------------------------------|
| FR-TR-01 | Migrate Rental Creation & Draft Updates to V2 | CreateRentalService, UpdateRentalService, holdFunds integration |
| FR-TR-02 | Migrate Rental Settlement to V2               | Return / settlement services, remove V1 from Rental module      |
| FR-TR-03 | Remove TariffFacade V1 from Tariff Module     | Delete TariffFacade, TariffFacadeImpl, and all V1 classes       |
