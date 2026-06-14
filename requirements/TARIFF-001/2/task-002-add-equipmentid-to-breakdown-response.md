# Task 002: Add equipmentId to CostCalculationResponse and update CalculationBreakdownMapper

> **Applied Skill:** `java.instructions.md` — immutability, JSpecify null-safety; `springboot.instructions.md` —
> MapStruct manual mapping

## 1. Objective

Add `@Nullable Long equipmentId` as the first field of the nested `EquipmentCostBreakdownResponse` record so that V2
endpoint responses can include the physical equipment unit ID. Update `CalculationBreakdownMapper.toBreakdownResponse()`
to pass `source.equipmentId()` — V1 breakdowns return `null` via the interface default method from task-001.

> ⚠️ **This task breaks the controller test** (`TariffV2CalculationControllerTest`) until task-011 updates the
> constructor calls. Do NOT run the full test suite between task-002 and task-011.

## 2. Files to Modify

### 2a. CostCalculationResponse.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/dto/CostCalculationResponse.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import org.jspecify.annotations.Nullable;
```

Add below the existing `import io.swagger.v3.oas.annotations.media.Schema;` line.

**Code to Replace:**

* **Location:** The inner `EquipmentCostBreakdownResponse` record definition.
* **Remove:**

```java
    @Schema(description = "Per-equipment cost breakdown")
    public record EquipmentCostBreakdownResponse(
            @NotNull String equipmentType,
            @NotNull Long tariffId,
            @NotNull String tariffName,
            @NotNull String pricingType,
            @NotNull BigDecimal itemCost,
            @NotNull Integer billedDurationMinutes,
            Integer overtimeMinutes,
            Integer forgivenMinutes,
            @NotNull BreakdownCostDetails calculationBreakdown
    ) {
    }
```

* **Replace with:**

```java
    @Schema(description = "Per-equipment cost breakdown")
    public record EquipmentCostBreakdownResponse(
            @Schema(description = "Physical equipment unit ID; null for V1 calculations")
            @Nullable Long equipmentId,
            @NotNull String equipmentType,
            @NotNull Long tariffId,
            @NotNull String tariffName,
            @NotNull String pricingType,
            @NotNull BigDecimal itemCost,
            @NotNull Integer billedDurationMinutes,
            Integer overtimeMinutes,
            Integer forgivenMinutes,
            @NotNull BreakdownCostDetails calculationBreakdown
    ) {
    }
```

---

### 2b. CalculationBreakdownMapper.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/mapper/CalculationBreakdownMapper.java`
* **Action:** Modify Existing File

**Code to Replace:**

* **Location:** The `toBreakdownResponse(EquipmentCostBreakdown source)` method body.
* **Remove:**

```java
    public CostCalculationResponse.EquipmentCostBreakdownResponse toBreakdownResponse(EquipmentCostBreakdown source) {
        return new CostCalculationResponse.EquipmentCostBreakdownResponse(
                source.equipmentType(),
                source.tariffId(),
                source.tariffName(),
                source.pricingType(),
                moneyMapper.toBigDecimal(source.itemCost()),
                durationMapper.toMinutes(source.billedDuration()),
                durationMapper.toMinutes(source.overtime()),
                durationMapper.toMinutes(source.forgiven()),
                source.calculationBreakdown()
        );
    }
```

* **Replace with:**

```java
    public CostCalculationResponse.EquipmentCostBreakdownResponse toBreakdownResponse(EquipmentCostBreakdown source) {
        return new CostCalculationResponse.EquipmentCostBreakdownResponse(
                source.equipmentId(),
                source.equipmentType(),
                source.tariffId(),
                source.tariffName(),
                source.pricingType(),
                moneyMapper.toBigDecimal(source.itemCost()),
                durationMapper.toMinutes(source.billedDuration()),
                durationMapper.toMinutes(source.overtime()),
                durationMapper.toMinutes(source.forgiven()),
                source.calculationBreakdown()
        );
    }
```

## 4. Validation Steps

skip
