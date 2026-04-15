# Task 005: Simplify `RentalCommandMapper.toReturnResponse`

> **Applied Skill:** `mapstruct-hexagonal` — Abstract mapper concrete methods must only map types that exist in the
> updated contracts; dead V1 references must be removed.

## 1. Objective

`RentalCommandMapper.toReturnResponse` currently builds a `List<CostBreakdown>` from `breakDownCosts`. Remove that
logic (and the helper `toCostBreakdown` method) since `ReturnEquipmentResult` no longer carries a breakdown map and
`RentalReturnResponse` no longer has a `costs` field.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/mapper/RentalCommandMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### Step A — Remove unused imports

**Remove** these three import lines:

```java
import com.github.jenkaby.bikerental.tariff.RentalCost;
import java.util.HashMap;
import java.util.Map;
```

### Step B — Replace `toReturnResponse` and remove `toCostBreakdown`

* **Location:** Replace the two concrete methods `toReturnResponse` and `toCostBreakdown` at the bottom of the class.

**Old (both methods):**

```java
    public RentalReturnResponse toReturnResponse(ReturnEquipmentResult result) {
        RentalResponse rentalResponse = rentalQueryMapper.toResponse(result.rental());
        var costsBreakdown = result.breakDownCosts().entrySet().stream()
                .map(entry -> toCostBreakdown(entry.getKey(), entry.getValue()))
                .toList();
        var settlementResponse = settlementMapper.toResponse(result.settlementInfo());
        return new RentalReturnResponse(rentalResponse, costsBreakdown, settlementResponse);
    }

    RentalReturnResponse.CostBreakdown toCostBreakdown(Long equipmentId, RentalCost cost) {
        return new RentalReturnResponse.CostBreakdown(
                equipmentId,
                cost.baseCost().amount(),
                cost.overtimeCost().amount(),
                cost.totalCost().amount(),
                cost.actualMinutes(),
                cost.billableMinutes(),
                cost.plannedMinutes(),
                cost.overtimeMinutes(),
                cost.forgivenessApplied(),
                cost.calculationMessage()
        );
    }
```

**New (single method only):**

```java
    public RentalReturnResponse toReturnResponse(ReturnEquipmentResult result) {
        RentalResponse rentalResponse = rentalQueryMapper.toResponse(result.rental());
        var settlementResponse = settlementMapper.toResponse(result.settlementInfo());
        return new RentalReturnResponse(rentalResponse, settlementResponse);
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
