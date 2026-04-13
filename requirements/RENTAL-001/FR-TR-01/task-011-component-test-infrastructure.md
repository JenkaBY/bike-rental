# Task 011: Update Component-Test Infrastructure for V2 Pricing Fields

> **Applied Skills:**  
> `.github/skills/spring-boot-java-cucumber/SKILL.md` — Cucumber DataTableType transformers, step definition assertions

## 1. Objective

Three component-test infrastructure files must be updated so the upcoming `rental.feature` changes in Task 012
and Task 013 can reference and assert the new V2 pricing fields (`specialTariffId`, `specialPrice`,
`discountPercent`) in request preparation, DB seeding, and DB persistence assertions.

## 2. Files to Modify

---

### 2a. `RentalRequestTransformer.java` — map V2 pricing fields

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalRequestTransformer.java`
* **Action:** Modify Existing File

Find the current `transform` method body:

```java
    @DataTableType
    public CreateRentalRequest transform(Map<String, String> entry) {
        var customerId = Aliases.getCustomerId(entry.get("customerId"));
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var duration = DataTableHelper.toDuration(entry, "duration");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var operator = Aliases.getValueOrDefault(entry.get("operatorId"));

        return new CreateRentalRequest(
                customerId,
                equipmentIds,
                duration,
                tariffId,
                operator
        );
    }
```

Replace with:

```java
    @DataTableType
    public CreateRentalRequest transform(Map<String, String> entry) {
        var customerId = Aliases.getCustomerId(entry.get("customerId"));
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var duration = DataTableHelper.toDuration(entry, "duration");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var operator = Aliases.getValueOrDefault(entry.get("operatorId"));
        var specialTariffId = DataTableHelper.toLong(entry, "specialTariffId");
        var specialPrice = DataTableHelper.toBigDecimal(entry, "specialPrice");
        var discountPercent = DataTableHelper.toInt(entry, "discountPercent");

        return new CreateRentalRequest(
                customerId,
                equipmentIds,
                duration,
                tariffId,
                operator,
                specialTariffId,
                specialPrice,
                discountPercent
        );
    }
```

> **Note:** `DataTableHelper.toLong`, `toBigDecimal`, and `toInt` already exist (verified in
> `CostCalculationRequestTransformer`). The new `CreateRentalRequest` constructor order comes from Task 006.

---

### 2b. `RentalJpaEntityTransformer.java` — map V2 pricing fields

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalJpaEntityTransformer.java`
* **Action:** Modify Existing File

Find the builder construction in `transform`:

```java
        return RentalJpaEntity.builder()
                .id(id)
                .customerId(customerId)
                .status(status)
                .startedAt(startedAt)
                .expectedReturnAt(expectedReturnAt)
                .actualReturnAt(actualReturnAt)
                .plannedDurationMinutes(plannedDurationMinutes)
                .actualDurationMinutes(actualDurationMinutes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
```

Replace with (add three new field reads before the builder, then add them to the builder):

```java
        var specialTariffId = DataTableHelper.toLong(entry, "specialTariffId");
        var specialPrice = DataTableHelper.toBigDecimal(entry, "specialPrice");
        var discountPercent = DataTableHelper.toInt(entry, "discountPercent");

        return RentalJpaEntity.builder()
                .id(id)
                .customerId(customerId)
                .status(status)
                .startedAt(startedAt)
                .expectedReturnAt(expectedReturnAt)
                .actualReturnAt(actualReturnAt)
                .plannedDurationMinutes(plannedDurationMinutes)
                .actualDurationMinutes(actualDurationMinutes)
                .specialTariffId(specialTariffId)
                .specialPrice(specialPrice)
                .discountPercent(discountPercent)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
```

> **Note:** `RentalJpaEntity.specialTariffId`, `specialPrice`, `discountPercent` fields are added by Task 005.

---

### 2c. `RentalDbSteps.java` — add null-guarded assertions for new rental fields + null-guard tariffId on equipment

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalDbSteps.java`
* **Action:** Modify Existing File

#### 2c-i — Null-guard `tariffId` in `assertRentalEquipmentsPersisted`

Locate this unconditional assertion inside `assertRentalEquipmentsPersisted`:

```java
            softly.assertThat(actual.getTariffId()).as("Tariff ID").isEqualTo(exp.getTariffId());
            softly.assertThat(actual.getStatus()).as("Status").isEqualTo(exp.getStatus());
```

Replace with:

```java
            if (exp.getTariffId() != null) {
                softly.assertThat(actual.getTariffId()).as("Tariff ID").isEqualTo(exp.getTariffId());
            }
            softly.assertThat(actual.getStatus()).as("Status").isEqualTo(exp.getStatus());
```

#### 2c-ii — Add null-guarded assertions for V2 rental fields in `assertRentalsPersisted`

Locate the tail of the rental assertion block inside `assertRentalsPersisted` (after the `updatedAt` check, before
`softly.assertAll()`):

```java
            if (exp.getUpdatedAt() != null) {
                softly.assertThat(actual.getUpdatedAt()).as("Updated at")
                        .isCloseTo(exp.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
            }
            softly.assertAll();
```

Replace with:

```java
            if (exp.getUpdatedAt() != null) {
                softly.assertThat(actual.getUpdatedAt()).as("Updated at")
                        .isCloseTo(exp.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
            }
            if (exp.getSpecialTariffId() != null) {
                softly.assertThat(actual.getSpecialTariffId()).as("Special tariff ID")
                        .isEqualTo(exp.getSpecialTariffId());
            }
            if (exp.getSpecialPrice() != null) {
                softly.assertThat(actual.getSpecialPrice()).as("Special price")
                        .isEqualByComparingTo(exp.getSpecialPrice());
            }
            if (exp.getDiscountPercent() != null) {
                softly.assertThat(actual.getDiscountPercent()).as("Discount percent")
                        .isEqualTo(exp.getDiscountPercent());
            }
            softly.assertAll();
```

> **Note:** `RentalJpaEntity.getSpecialTariffId()`, `getSpecialPrice()`, `getDiscountPercent()` are
> added by Task 005.

## 4. Validation Steps

```bash
./gradlew :component-test:compileTestJava
```
