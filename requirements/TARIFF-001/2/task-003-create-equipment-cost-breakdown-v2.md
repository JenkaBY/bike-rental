# Task 003: Create EquipmentCostBreakdownV2 record

> **Applied Skill:** `java.instructions.md` — immutable records; `spring-boot-data-ddd` — domain value object pattern

## 1. Objective

Create a new `EquipmentCostBreakdownV2` record that mirrors `BaseEquipmentCostBreakdown` but adds
`@Nullable Long equipmentId`
as its first component. This record is used exclusively by `RentalCostCalculationV2Service` to carry the physical
equipment unit ID through to the API response.

`BaseEquipmentCostBreakdown` is left unchanged — V1 callers continue using it; `equipmentId()` returns `null` via the
interface default added in task-001.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/service/EquipmentCostBreakdownV2.java`
* **Action:** Create New File

## 3. Code Implementation

**Full file content:**

```java
package com.github.jenkaby.bikerental.tariff.domain.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public record EquipmentCostBreakdownV2(
        @Nullable Long equipmentId,
        String equipmentType,
        Long tariffId,
        String tariffName,
        String pricingType,
        Money itemCost,
        Duration billedDuration,
        Duration overtime,
        Duration forgiven,
        BreakdownCostDetails calculationBreakdown
) implements EquipmentCostBreakdown {
}
```

## 4. Validation Steps

skip