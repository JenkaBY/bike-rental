# Task 001: Change TariffV2 abstract method signature to accept temporal period

> **Applied Skill:** `java.instructions.md` — immutability, no dead code; `springboot.instructions.md` — handle edge
> cases clearly

## 1. Objective

Replace the `calculateCost(Duration)` abstract method on `TariffV2` with
`calculateCost(LocalDateTime startAt, LocalDateTime returnAt)` so that each concrete subclass can perform
calendar-aware cost calculations using actual rental start and end times. Remove the now-dead
`getNumberOfDays(Duration)` utility method.

> ⚠️ **This task breaks compilation until tasks 002–003 are also complete.** Do NOT run a build validation until after
> completing task-003.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/TariffV2.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import java.time.LocalDateTime;
```

Add this import below the existing `import java.time.Duration;` line.

---

**Change 1 — Replace the abstract method signature.**

* **Location:** Replace the entire abstract method declaration at the bottom of the class body.
* **Remove this line:**

```java
    public abstract RentalCostV2 calculateCost(Duration duration);
```

* **Replace with:**

```java
    public abstract RentalCostV2 calculateCost(LocalDateTime startAt, LocalDateTime returnAt);
```

---

**Change 2 — Remove the dead `getNumberOfDays` utility.**

* **Location:** The static helper method after `isNegative(Duration)`.
* **Remove this entire method:**

```java
    protected static int getNumberOfDays(Duration duration) {
        return (int) Math.ceil((double) duration.toMinutes() / Duration.ofDays(1).toMinutes());
    }
```

> `getNumberOfDays` was only called by `FlatFeeTariffV2`. After task-002 the flat-fee implementation no longer uses
> it. Removing it prevents dead code accumulation.

## 4. Validation Steps

skip