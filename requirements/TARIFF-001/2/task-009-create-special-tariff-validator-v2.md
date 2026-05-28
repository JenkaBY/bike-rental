# Task 009: Create SpecialTariffConsistencyValidatorV2 and update annotation

> **Applied Skill:** `java.instructions.md` — single-responsibility; `springboot.instructions.md` — Bean Validation
> multi-type constraint

## 1. Objective

The `@SpecialTariffConsistency` annotation is placed on `CostCalculationV2Request` but the existing
`SpecialTariffConsistencyValidator` only handles `CostCalculationRequest`. Create a dedicated validator for
`CostCalculationV2Request` and register it alongside the V1 validator on the constraint annotation.

## 2. Files to Modify / Create

---

### 2a. Create SpecialTariffConsistencyValidatorV2.java

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/validation/SpecialTariffConsistencyValidatorV2.java`
* **Action:** Create New File

**Full file content:**

```java
package com.github.jenkaby.bikerental.tariff.web.query.validation;

import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpecialTariffConsistencyValidatorV2
        implements ConstraintValidator<SpecialTariffConsistency, CostCalculationV2Request> {

    @Override
    public boolean isValid(CostCalculationV2Request value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        boolean isTariffIdProvided = value.specialTariffId() != null;
        boolean isPriceProvided = value.specialPrice() != null;
        return isTariffIdProvided == isPriceProvided;
    }
}
```

---

### 2b. Update SpecialTariffConsistency.java annotation

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/query/validation/SpecialTariffConsistency.java`
* **Action:** Modify Existing File

**Code to Replace:**

* **Location:** The `@Constraint` annotation line.
* **Remove:**

```java
@Constraint(validatedBy = SpecialTariffConsistencyValidator.class)
```

* **Replace with:**

```java
@Constraint(validatedBy = {SpecialTariffConsistencyValidator.class, SpecialTariffConsistencyValidatorV2.class})
```

## 4. Validation Steps

skip