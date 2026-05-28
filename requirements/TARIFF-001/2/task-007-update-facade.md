# Task 007: Update TariffV2Facade and TariffV2FacadeImpl

> **Applied Skill:** `spring-boot-modulith` — cross-module Facade pattern; `java.instructions.md` — interface-first
> design

## 1. Objective

Expose the V2 calculation capability through the module's public facade so that the `rental` module (and the
`tariff` web layer) can call it without importing internal service classes.

## 2. Files to Modify

---

### 2a. TariffV2Facade.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffV2Facade.java`
* **Action:** Modify Existing File

**Code to Add:**

* **Location:** Add the new method after
  `RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command);`
* **Snippet:**

```java
    RentalCostCalculationResult calculateRentalCostV2(RentalCostCalculationV2Command command);
```

The file after modification:

```java
package com.github.jenkaby.bikerental.tariff;

import java.util.Optional;

public interface TariffV2Facade {

    Optional<TariffV2Info> findById(Long tariffId);

    RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command);

    RentalCostCalculationResult calculateRentalCostV2(RentalCostCalculationV2Command command);
}
```

---

### 2b. TariffV2FacadeImpl.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffV2FacadeImpl.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationV2UseCase;
```

Add after the existing `import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationUseCase;`
line.

**Change 1 — Add field:**

* **Location:** After the `private final RentalCostCalculationUseCase rentalCostCalculationUseCase;` field declaration.
* **Add:**

```java
    private final RentalCostCalculationV2UseCase rentalCostCalculationV2UseCase;
```

**Change 2 — Update constructor:**

* **Location:** Replace the existing constructor.
* **Remove:**

```java
    TariffV2FacadeImpl(GetTariffV2ByIdUseCase getTariffV2ByIdUseCase,
                       TariffV2ToInfoMapper tariffV2ToInfoMapper,
                       RentalCostCalculationUseCase rentalCostCalculationUseCase) {
        this.getTariffV2ByIdUseCase = getTariffV2ByIdUseCase;
        this.tariffV2ToInfoMapper = tariffV2ToInfoMapper;
        this.rentalCostCalculationUseCase = rentalCostCalculationUseCase;
    }
```

* **Replace with:**

```java
    TariffV2FacadeImpl(GetTariffV2ByIdUseCase getTariffV2ByIdUseCase,
                       TariffV2ToInfoMapper tariffV2ToInfoMapper,
                       RentalCostCalculationUseCase rentalCostCalculationUseCase,
                       RentalCostCalculationV2UseCase rentalCostCalculationV2UseCase) {
        this.getTariffV2ByIdUseCase = getTariffV2ByIdUseCase;
        this.tariffV2ToInfoMapper = tariffV2ToInfoMapper;
        this.rentalCostCalculationUseCase = rentalCostCalculationUseCase;
        this.rentalCostCalculationV2UseCase = rentalCostCalculationV2UseCase;
    }
```

**Change 3 — Implement new facade method:**

* **Location:** After the `calculateRentalCost` method.
* **Add:**

```java
    @Override
    public RentalCostCalculationResult calculateRentalCostV2(RentalCostCalculationV2Command command) {
        return rentalCostCalculationV2UseCase.execute(command);
    }
```

## 4. Validation Steps

skip
