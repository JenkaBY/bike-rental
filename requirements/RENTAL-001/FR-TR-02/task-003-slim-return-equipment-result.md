# Task 003: Slim Down `ReturnEquipmentResult`

> **Applied Skill:** `spring-boot-data-ddd` — Domain use-case result records carry only what the application layer
> needs; no cross-module types (V1 `RentalCost`) should leak through use-case boundaries.

## 1. Objective

Remove the `Map<Long, RentalCost> breakDownCosts` component from `ReturnEquipmentResult`. The response no longer
carries a per-item cost breakdown, so the use-case result only needs to expose the updated `Rental` aggregate and the
optional `SettlementInfo`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/ReturnEquipmentResult.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Replace the entire file contents** with:

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import org.jspecify.annotations.Nullable;

public record ReturnEquipmentResult(
        Rental rental,
        @Nullable SettlementInfo settlementInfo
) {
}
```

* **Diff summary:** Remove the `import com.github.jenkaby.bikerental.tariff.RentalCost;`,
  `import java.util.Map;`, and the `Map<Long, RentalCost> breakDownCosts` record component.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
