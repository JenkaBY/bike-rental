# Task 003: Adapt remaining TariffV2 subclasses to new calculateCost signature

> **Applied Skill:** `java.instructions.md` — minimal targeted changes, no over-engineering;
> `springboot.instructions.md` — constructor injection; immutability

## 1. Objective

Migrate the four remaining concrete subclasses (`FlatHourlyTariffV2`, `DailyTariffV2`,
`DegressiveHourlyTariffV2`, `SpecialTariffV2`) to implement the new
`calculateCost(LocalDateTime startAt, LocalDateTime returnAt)` signature. Each subclass computes
`Duration.between(startAt, returnAt)` at the start of the method and then delegates to its existing
Duration-based logic — no business logic changes in these four files.

After completing this task the project **must compile** without errors.

## 2. Files to Modify

Four separate files, each with the same adaptation pattern.

---

### 2a. FlatHourlyTariffV2

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/FlatHourlyTariffV2.java`
* **Action:** Modify Existing File

**Import to add** (below the existing `import java.time.Duration;`):

```java
import java.time.LocalDateTime;
```

**Replace the method signature and first line:**

* **Location:** The `@Override calculateCost` method — change only the signature and add one line at the top of the
  body.
* **Remove:**

```java
    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        if (isNegative(duration)) {
```

* **Replace with:**

```java
    @Override
    public RentalCostV2 calculateCost(LocalDateTime startAt, LocalDateTime returnAt) {
        Duration duration = Duration.between(startAt, returnAt);
        if (isNegative(duration)) {
```

> The rest of the method body is unchanged.

---

### 2b. DailyTariffV2

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/DailyTariffV2.java`
* **Action:** Modify Existing File

**Import to add** (below the existing `import java.time.Duration;`):

```java
import java.time.LocalDateTime;
```

**Replace the method signature and first line:**

* **Location:** The `@Override calculateCost` method.
* **Remove:**

```java
    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        if (isNegative(duration)) {
```

* **Replace with:**

```java
    @Override
    public RentalCostV2 calculateCost(LocalDateTime startAt, LocalDateTime returnAt) {
        Duration duration = Duration.between(startAt, returnAt);
        if (isNegative(duration)) {
```

> The rest of the method body is unchanged.

---

### 2c. DegressiveHourlyTariffV2

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/DegressiveHourlyTariffV2.java`
* **Action:** Modify Existing File

**Import to add** (below the existing `import java.time.Duration;`):

```java
import java.time.LocalDateTime;
```

**Replace the method signature and first line:**

* **Location:** The `@Override calculateCost` method.
* **Remove:**

```java
    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        if (isNegative(duration)) {
```

* **Replace with:**

```java
    @Override
    public RentalCostV2 calculateCost(LocalDateTime startAt, LocalDateTime returnAt) {
        Duration duration = Duration.between(startAt, returnAt);
        if (isNegative(duration)) {
```

> The rest of the method body is unchanged.

---

### 2d. SpecialTariffV2

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/SpecialTariffV2.java`
* **Action:** Modify Existing File

**Import to add** (below the existing `import java.time.Duration;`):

```java
import java.time.LocalDateTime;
```

**Replace the entire `calculateCost` method:**

* **Location:** The `@Override calculateCost` method.
* **Remove:**

```java
    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        return new BaseRentalCostV2(price, new BreakdownCostDetails.Special());
    }
```

* **Replace with:**

```java
    @Override
    public RentalCostV2 calculateCost(LocalDateTime startAt, LocalDateTime returnAt) {
        return new BaseRentalCostV2(price, new BreakdownCostDetails.Special());
    }
```

> `SpecialTariffV2.calculateCost` is never invoked by the calculation service (special mode uses a fixed group
> price). The signature change is required only to satisfy the abstract contract.

## 4. Validation Steps

skip