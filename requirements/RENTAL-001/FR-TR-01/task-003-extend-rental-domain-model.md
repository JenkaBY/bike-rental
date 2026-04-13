# Task 003: Extend `Rental` Domain Aggregate with V2 Pricing Fields

> **Applied Skill:** `.github/skills/spring-boot-data-ddd/SKILL.md` — Value objects, aggregate fields, immutability

## 1. Objective

Add three new nullable fields to the `Rental` domain aggregate: `specialTariffId` (Long), `specialPrice` (Money),
and `discountPercent` (Integer). These are stored at rental creation and re-read at settlement without operator
re-entry. Update the `Rental.Builder` (via Lombok `@Builder`) so `CreateRentalService` can populate them at
construction time.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
// Money is already imported
```

**Code to Add/Replace:**

* **Location:** In the `Rental` class body, directly **after** the `private Money estimatedCost;` field
  (around line 43 of the current file). Add three new field declarations:

```java
    private Money estimatedCost;
    private Money finalCost;

    private Long specialTariffId;
    private Money specialPrice;
    private DiscountPercent discountPercent;
```

The existing content at that location is:

```java
    private Money estimatedCost;
    private Money finalCost;
```

Replace it with:

```java
    private Money estimatedCost;
    private Money finalCost;

    private Long specialTariffId;
    private Money specialPrice;
    private DiscountPercent discountPercent;
```

> Lombok `@Builder` and `@AllArgsConstructor(access = AccessLevel.PRIVATE)` already on the class will
> automatically include the new fields in the generated builder and constructor. No manual setter is needed —
> these fields are set once at creation and never mutated by the domain.

---

### 3b — Add a custom `build()` override to enforce pricing invariants

Lombok merges into any inner class named `RentalBuilder` that you define manually. Add the following static
inner class **anywhere inside the `Rental` class body** (e.g. at the bottom, before the last `}`):

```java
    public static class RentalBuilder {
        public Rental build() {
            if (specialTariffId != null && discountPercent != null) {
                throw new IllegalArgumentException(
                        "specialTariffId and discountPercent are mutually exclusive");
            }
            if (specialTariffId != null && specialPrice == null) {
                throw new IllegalArgumentException(
                        "specialPrice is required when specialTariffId is set");
            }
            return new Rental(id, customerId, equipments, status, startedAt, expectedReturnAt,
                    actualReturnAt, plannedDuration, actualDuration, estimatedCost, finalCost,
                    specialTariffId, specialPrice, discountPercent, createdAt, updatedAt);
        }
    }
```

> **Important:** The argument order in `new Rental(...)` must exactly match the field declaration order in the
> class body (which is the order Lombok uses for `@AllArgsConstructor`). After completing this task the full
> field order will be:
> `id, customerId, equipments, status, startedAt, expectedReturnAt, actualReturnAt, plannedDuration,
> actualDuration, estimatedCost, finalCost, specialTariffId, specialPrice, discountPercent, createdAt, updatedAt`
>
> Lombok will inject the remaining builder setter methods (`id(...)`, `customerId(...)`, etc.) into the
> `RentalBuilder` class automatically at compile time.

---

### 3c — Update `getEstimatedCost()` to apply special pricing and discount

The existing method:

```java
    public Money getEstimatedCost() {
        return this.equipments.stream()
                .map(RentalEquipment::getEstimatedCost)
                .reduce(Money.zero(), Money::add);
    }
```

Replace with:

```java
    public Money getEstimatedCost() {
        return calculateCost(RentalEquipment::getEstimatedCost);
    }

    private Money calculateCost(Function<RentalEquipment, Money> costExtractor) {
        if (specialPrice != null) {
            return specialPrice;
        }
        var subtotal = this.equipments.stream()
                .map(costExtractor)
                .reduce(Money.zero(), Money::add);
        if (discountPercent != null) {
            return subtotal.subtract(discountPercent.multiply(subtotal));
        }
        return subtotal;
    }
```

Add the import:

```java
import java.util.function.Function;
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
