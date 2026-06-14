# Task 001: Extend EquipmentCostBreakdown interface with nullable equipmentId

> **Applied Skill:** `java.instructions.md` — backward-compatible default method; `springboot.instructions.md` —
> JSpecify null-safety

## 1. Objective

Add a `default @Nullable Long equipmentId()` method to the `EquipmentCostBreakdown` interface so that all existing
V1 implementations automatically return `null` (no callers change), while the new V2 implementation can override it
with the actual equipment unit ID.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/EquipmentCostBreakdown.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import org.jspecify.annotations.Nullable;
```

Add this import below the existing `import java.time.Duration;` line.

---

**Code to Add:**

* **Location:** Add the new default method immediately before the `String pricingType();` declaration (i.e., after the
  `String tariffName();` line).
* **Snippet:**

```java
    @Nullable
    default Long equipmentId() {
        return null;
    }
```

The file after modification should look like:

```java
package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public interface EquipmentCostBreakdown {

    String equipmentType();

    Long tariffId();

    String tariffName();

    @Nullable
    default Long equipmentId() {
        return null;
    }

    String pricingType();

    Money itemCost();

    Duration billedDuration();

    default boolean forgivenessApplied() {
        return forgiven() != null && !forgiven().isPositive();
    }

    Duration overtime();

    Duration forgiven();

    BreakdownCostDetails calculationBreakdown();
}
```

## 4. Validation Steps

skip
