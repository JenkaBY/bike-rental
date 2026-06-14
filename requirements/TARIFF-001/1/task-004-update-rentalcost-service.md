# Task 004: Update RentalCostCalculationService to call new calculateCost signature

> **Applied Skill:** `java.instructions.md` — use `Optional` for null-safe defaults;
> `springboot.instructions.md` — inject `Clock` bean for server timezone

## 1. Objective

Update `RentalCostCalculationService.executeNormalMode` to synthesise `LocalDateTime billingStartAt` and
`billingReturnAt` from the existing `rentalDate` field, then pass them to `tariff.calculateCost(startAt, returnAt)`
instead of the removed `calculateCost(Duration)` overload.

> **Behavioral note for V1 callers:** Because V1 has no precise start time, `billingStartAt` is synthesised as
> `rentalDate.atStartOfDay()` (server-local midnight). A 24-hour rental will now produce `billingReturnAt =
> nextDay.atStartOfDay()`, which the flat-fee algorithm correctly counts as 2 calendar days (midnight-to-midnight
> spans two dates). This is the expected outcome per the approved FR-1 design.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/RentalCostCalculationService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Import to add** — insert below the existing `import java.time.Duration;` line:

```java
import java.time.LocalDateTime;
```

---

**Change — synthesise timestamps and update the `calculateCost` call site.**

* **Location:** Inside `executeNormalMode`, immediately before the `List<EquipmentCostBreakdown> breakdowns` line
  (i.e., after the forgiveness-logic block that computes `billedDuration`).

* **Remove:**

```java
        List<EquipmentCostBreakdown> breakdowns = new ArrayList<>();
        Money subtotal = Money.zero();
        Map<String, TariffV2> tariffCache = new HashMap<>();
        for (EquipmentCostItem item : command.equipments()) {
            TariffV2 tariff = tariffCache.computeIfAbsent(item.equipmentType(),
                    type -> selectTariffUseCase.execute(new SelectTariffV2UseCase.SelectTariffCommand(item.equipmentType(), billedDuration, rentalDate)));
            RentalCostV2 cost = tariff.calculateCost(billedDuration);
```

* **Replace with:**

```java
        LocalDateTime billingStartAt = rentalDate.atStartOfDay();
        LocalDateTime billingReturnAt = billingStartAt.plus(billedDuration);

        List<EquipmentCostBreakdown> breakdowns = new ArrayList<>();
        Money subtotal = Money.zero();
        Map<String, TariffV2> tariffCache = new HashMap<>();
        for (EquipmentCostItem item : command.equipments()) {
            TariffV2 tariff = tariffCache.computeIfAbsent(item.equipmentType(),
                    type -> selectTariffUseCase.execute(new SelectTariffV2UseCase.SelectTariffCommand(item.equipmentType(), billedDuration, rentalDate)));
            RentalCostV2 cost = tariff.calculateCost(billingStartAt, billingReturnAt);
```

> `rentalDate` is already resolved from `command.rentalDate()` with a `LocalDate.now(clock)` fallback two lines
> earlier in the same method — no additional null-handling is needed here.

## 4. Validation Steps

skip
