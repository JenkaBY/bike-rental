# Task 005: Create RentalCostCalculationV2UseCase interface

> **Applied Skill:** `java.instructions.md` — interface-first design; `springboot.instructions.md` — hexagonal use-case
> pattern

## 1. Objective

Define the application-layer port contract that `RentalCostCalculationV2Service` (task-006) will implement and
`TariffV2FacadeImpl` (task-007) will depend on. The interface lives in
`tariff/application/usecase/` — consistent with `RentalCostCalculationUseCase`.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/usecase/RentalCostCalculationV2UseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Full file content:**

```java
package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;

public interface RentalCostCalculationV2UseCase {

    RentalCostCalculationResult execute(RentalCostCalculationV2Command command);
}
```

## 4. Validation Steps

skip
