# Task 002: Migrate `ReturnEquipmentService` to `TariffV2Facade`

> **Applied Skill:** `spring-boot-data-ddd` — Service layer, dependency inversion; delegate cost computation to
> external module facade only.

## 1. Objective

Replace the `TariffFacade` (V1) per-item loop in `ReturnEquipmentService` with a single batch call to
`TariffV2Facade.calculateRentalCost`. Use `RentalCostCommandMapper.toReturnCommand` (added in Task 001) to build the
command and use `EquipmentCostBreakdown.itemCost()` to set per-item `finalCost`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### Step 0 — Add computed `getFinalCost()` to `Rental` domain model

> **Note:** `Rental` already has a `private Money finalCost` field; Lombok `@Getter` would generate a plain getter.
> We override it with an explicit computed method (Lombok will skip its own when an explicit method exists).
> `calculateCost()` is not reused here because it lacks null filtering — unreturned equipment items have
> `null` finalCost during a partial return.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`
* **Action:** Modify Existing File

**Location:** Insert the following method directly after the closing brace of `getEstimatedCost()`.

```java
    public Money getFinalCost() {
        return calculateCost(RentalEquipment::getFinalCost);;
    }
```

**Verify** that the existing `private Money finalCost;` field is still present — it must not be removed because
`completeWithStatus(Money, RentalStatus)` assigns to it directly and `completeForDebt()` checks it via field
access.

---

### Step A — Replace imports

**Remove** these four import lines:

```java
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import java.util.HashMap;
import java.util.Map;
```

**Add** these imports (place them with the other `com.github.jenkaby.bikerental` imports):

```java
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
```

---

### Step B — Replace field declaration

* **Location:** Replace the `private final TariffFacade tariffFacade;` field declaration.

**Old:**

```java
    private final TariffFacade tariffFacade;
```

**New:**

```java
    private final TariffV2Facade tariffV2Facade;
    private final RentalCostCommandMapper costCommandMapper;
```

---

### Step C — Replace constructor parameter and assignment

* **Location:** Replace the constructor signature and the `this.tariffFacade` assignment line.

**Old:**

```java
    ReturnEquipmentService(
            RentalRepository rentalRepository,
            RentalDurationCalculator durationCalculator,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            RentalEventMapper eventMapper,
            EventPublisher eventPublisher,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.durationCalculator = durationCalculator;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }
```

**New:**

```java
    ReturnEquipmentService(
            RentalRepository rentalRepository,
            RentalDurationCalculator durationCalculator,
            TariffV2Facade tariffV2Facade,
            RentalCostCommandMapper costCommandMapper,
            FinanceFacade financeFacade,
            RentalEventMapper eventMapper,
            EventPublisher eventPublisher,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.durationCalculator = durationCalculator;
        this.tariffV2Facade = tariffV2Facade;
        this.costCommandMapper = costCommandMapper;
        this.financeFacade = financeFacade;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }
```

---

### Step D — Replace cost-calculation block inside `execute`

* **Location:** Replace the block starting at `Money totalCost = Money.zero();` through the closing brace of the
  per-item for-loop (the loop that calls `tariffFacade.calculateRentalCost`).

**Old:**

```java
        Money totalCost = Money.zero();
        Map<Long, RentalCost> rentalCostMapToEquipment = new HashMap<>();
        for (var equipment : equipmentsToReturn) {
            RentalCost cost = tariffFacade.calculateRentalCost(
                    equipment.getTariffId(),
                    rental.getActualDuration(),
                    durationResult.billableMinutes(),
                    rental.getPlannedDuration()
            );
            equipment.setFinalCost(cost.totalCost());
            rentalCostMapToEquipment.put(equipment.getEquipmentId(), cost);
            totalCost = totalCost.add(cost.totalCost());
        }
```

**New:**

```java
        var costCommand = costCommandMapper.toReturnCommand(
                rental,
                equipmentsToReturn,
                durationResult.billableMinutes());
        var costResult = tariffV2Facade.calculateRentalCost(costCommand);

        var breakdowns = costResult.equipmentBreakdowns();
        for (int i = 0; i < equipmentsToReturn.size(); i++) {
            equipmentsToReturn.get(i).setFinalCost(breakdowns.get(i).itemCost());
        }
```

---

### Step E — Update partial-return branch

* **Location:** Replace the `if (!rental.allEquipmentReturned())` block.

**Old:**

```java
        if (!rental.allEquipmentReturned()) {
            Rental saved = rentalRepository.save(rental);
            log.info("Partial return recorded for rental {}", saved.getId());
            RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, totalCost);
            eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
            return new ReturnEquipmentResult(saved, rentalCostMapToEquipment, null);
        }
```

**New:**

```java
        if (!rental.allEquipmentReturned()) {
            Rental saved = rentalRepository.save(rental);
            log.info("Partial return recorded for rental {}", saved.getId());
            RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, saved.getFinalCost());
            eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
            return new ReturnEquipmentResult(saved, null);
        }
```

---

### Step F — Update final-return total-cost accumulation and result construction

* **Location:** Replace the `previouslyReturnedCost` block and the final `return` statement.

**Old:**

```java
        // TODO Move to rental class
        Money previouslyReturnedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
                .filter(e -> !equipmentsToReturn.contains(e))
                .map(RentalEquipment::getFinalCost)
                .reduce(Money.zero(), Money::add);

        var totalFinalCost = previouslyReturnedCost.add(totalCost);
```

**New:**

```java
        Money previouslyReturnedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
                .filter(e -> !equipmentsToReturn.contains(e))
                .map(RentalEquipment::getFinalCost)
                .reduce(Money.zero(), Money::add);

        var totalFinalCost = previouslyReturnedCost.add(costResult.totalCost());
```

* **Location (same method, further down):** Replace the final `return` statement.

**Old:**

```java
        return new ReturnEquipmentResult(saved, rentalCostMapToEquipment, settlementInfo);
```

**New:**

```java
        return new ReturnEquipmentResult(saved, settlementInfo);
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
