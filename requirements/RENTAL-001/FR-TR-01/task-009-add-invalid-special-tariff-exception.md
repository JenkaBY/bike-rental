# Task 009: Promote `InvalidSpecialTariffTypeException` to Tariff Public API and Wire 422 in
`RentalRestControllerAdvice`

> **Applied Skill:** None (exception relocation + Spring MVC advice pattern already established in the codebase)

## 1. Objective

`InvalidSpecialTariffTypeException` already exists at `tariff.domain.exception` (module-internal). Because
the Rental module's `RentalRestControllerAdvice` must catch it, it must be in the tariff module's **public**
package (`com.github.jenkaby.bikerental.tariff`), following the same pattern as `SuitableTariffNotFoundException`.

The constructor currently takes `PricingType` (an internal domain type). To avoid exposing an internal type
through the public API, the constructor signature is changed to accept `String actualType` instead.
The internal caller (`RentalCostCalculationService`) is updated to pass `pricingType.name()`.

No changes to `shared/` are required.

## 2. Files to Create / Modify

---

### 2a. `InvalidSpecialTariffTypeException.java` — Move to tariff public package

* **Old File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/exception/InvalidSpecialTariffTypeException.java`
* **New File Path:** `service/src/main/java/com/github/jenkaby/bikerental/tariff/InvalidSpecialTariffTypeException.java`
* **Action:** Delete the old file; create the new file at the path above.

```java
package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class InvalidSpecialTariffTypeException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.special.type_invalid";
    private static final String MESSAGE_TEMPLATE = "Tariff with id %s has pricing type '%s', but SPECIAL is required";

    public InvalidSpecialTariffTypeException(Long tariffId, String actualType) {
        super(MESSAGE_TEMPLATE.formatted(tariffId, actualType), ERROR_CODE, new TariffTypeDetails(tariffId, actualType));
    }

    public TariffTypeDetails getDetails() {
        return getParams()
                .map(p -> (TariffTypeDetails) p)
                .orElseThrow(() -> new IllegalStateException("Expected TariffTypeDetails in exception parameters"));
    }

    public record TariffTypeDetails(Long tariffId, String actualPricingType) {
    }
}
```

---

### 2b. `RentalCostCalculationService.java` — Update import and constructor call

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/RentalCostCalculationService.java`
* **Action:** Modify Existing File

Replace import:

```java
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialTariffTypeException;
```

With:

```java
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
```

Find the throw site:

```java
throw new InvalidSpecialTariffTypeException(command.specialTariffId(), specialTariff.getPricingType());
```

Replace with:

```java
throw new InvalidSpecialTariffTypeException(command.specialTariffId(), specialTariff.getPricingType().name());
```

---

### 2c. `TariffRestControllerAdvice.java` — Update import only

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/tariff/web/error/TariffRestControllerAdvice.java`
* **Action:** Modify Existing File

Replace import:

```java
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialTariffTypeException;
```

With:

```java
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
```

No other changes — the handler body is unchanged.

---

### 2d. `RentalRestControllerAdvice.java` — Add handler for `InvalidSpecialTariffTypeException`

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/error/RentalRestControllerAdvice.java`
* **Action:** Modify Existing File

**Import to add:**

```java
import com.github.jenkaby.bikerental.tariff.InvalidSpecialTariffTypeException;
```

**Location:** After the last existing `@ExceptionHandler` method, before the `private String resolveCorrelationId()`
helper.

```java
    @ExceptionHandler(InvalidSpecialTariffTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSpecialTariffType(InvalidSpecialTariffTypeException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Invalid special tariff type: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Invalid special tariff type");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
