# Task 008: Fix BatchCalculationMapper — replace broken V2 methods with correct ones

> **Applied Skill:** `mapstruct-hexagonal` — MapStruct abstract mapper; `java.instructions.md` — no dead code

## 1. Objective

`BatchCalculationMapper` currently has two broken V2 methods that map `CostCalculationV2Request` →
`RentalCostCalculationCommand` (V1 type). These must be removed and replaced with correct V2 methods that map to
`RentalCostCalculationV2Command` and `EquipmentCostItemV2`.

**Removed methods:**

- `toCommand(CostCalculationV2Request)` → `RentalCostCalculationCommand` ← wrong target type
- `toItem(CostCalculationV2Request.EquipmentItemRequest)` → `EquipmentCostItem` ← wrong target type

**Added methods:**

- `toV2Command(CostCalculationV2Request)` → `RentalCostCalculationV2Command`
- `toV2Item(CostCalculationV2Request.EquipmentItemRequest)` → `EquipmentCostItemV2`

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/mapper/BatchCalculationMapper.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.tariff.EquipmentCostItemV2;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
```

Add these after the existing `import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;` line.
Also remove the now-unused import for `com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request`
if it was added with a fully-qualified name inline — it is now a proper import.

---

**Change 1 — Remove broken V2 toCommand method:**

* **Location:** Find and remove the entire method:

```java
    @Mapping(target = "plannedDuration", source = "plannedDurationMinutes")
    @Mapping(target = "actualDuration", source = "actualDurationMinutes")
    @Mapping(target = "discount", source = "discountPercent")
    @Mapping(target = "rentalDate", ignore = true)
    public abstract RentalCostCalculationCommand toCommand(com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request request);
```

---

**Change 2 — Remove broken V2 toItem method:**

* **Location:** Find and remove the entire method:

```java
    public abstract EquipmentCostItem toItem(com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request.EquipmentItemRequest item);
```

---

**Change 3 — Add correct V2 mapping methods:**

* **Location:** Add both methods after the existing
  `public abstract EquipmentCostItem toItem(CostCalculationRequest.EquipmentItemRequest item);` method.
* **Snippet:**

```java
    @Mapping(target = "plannedDuration", source = "plannedDurationMinutes")
    @Mapping(target = "discount", source = "discountPercent")
    public abstract RentalCostCalculationV2Command toV2Command(CostCalculationV2Request request);

    public abstract EquipmentCostItemV2 toV2Item(CostCalculationV2Request.EquipmentItemRequest item);
```

### Field mapping explanation for `toV2Command`

| Source (`CostCalculationV2Request`) | Target (`RentalCostCalculationV2Command`) | Converter                                               |
|-------------------------------------|-------------------------------------------|---------------------------------------------------------|
| `equipments`                        | `equipments`                              | uses `toV2Item` (MapStruct auto-selects by return type) |
| `startAt` (LocalDateTime)           | `startAt` (LocalDateTime)                 | identity                                                |
| `plannedDurationMinutes` (Integer)  | `plannedDuration` (Duration)              | `DurationMapper`                                        |
| `discountPercent` (Integer)         | `discount` (DiscountPercent)              | `DiscountMapper`                                        |
| `specialTariffId` (Long)            | `specialTariffId` (Long)                  | identity                                                |
| `specialPrice` (BigDecimal)         | `specialPrice` (Money)                    | `MoneyMapper`                                           |

For `toV2Item` all three fields (`equipmentId`, `equipmentType`, `returnAt`) have matching names and compatible types —
MapStruct auto-maps them without annotations.

## 4. Validation Steps

skip