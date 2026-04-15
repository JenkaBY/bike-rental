# Task 001: Add `toReturnCommand` to `RentalCostCommandMapper`

> **Applied Skill:** `spring-boot-data-ddd` — MapStruct abstract mapper extension pattern; constructor injection for
> collaborators.

## 1. Objective

Add a `toReturnCommand(Rental, List<RentalEquipment>, Duration)` method to `RentalCostCommandMapper` that builds a
`RentalCostCalculationCommand` for the equipment-return scenario, using already-stored `RentalEquipment.equipmentType`
instead of querying `EquipmentFacade` again.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/mapper/RentalCostCommandMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following three imports directly after the existing `import java.time.LocalDate;` line:

```java
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;
import java.time.Duration;
```

**Code to Add:**

* **Location:** Append the following method at the end of the class body, after the closing brace of the existing
  `toCommand(Rental rental, List<EquipmentInfo> equipments)` method (the last method in the file).

```java
    public RentalCostCalculationCommand toReturnCommand(
            Rental rental,
            List<RentalEquipment> equipmentsToReturn,
            Integer actualDurationMinutes) {
        var costItems = equipmentsToReturn.stream()
                .map(e -> new EquipmentCostItem(e.getEquipmentType()))
                .toList();
        return new RentalCostCalculationCommand(
                costItems,
                rental.getPlannedDuration(),
                Duration.ofMinutes(actualDurationMinutes),
                rental.getDiscountPercent(),
                rental.getSpecialTariffId(),
                rental.getSpecialPrice(),
                rental.getStartedAt().toLocalDate());
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
