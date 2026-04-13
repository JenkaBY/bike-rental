# Task 006: Extend `CreateRentalRequest` and `CreateRentalCommand` with V2 Pricing Override Fields

> **Applied Skill:** `.github/skills/spring-mvc-controller-test/SKILL.md` — request body field validation  
> **Applied Skill:** `.github/skills/mapstruct-hexagonal/SKILL.md` — Pattern 1, web command mapper

## 1. Objective

Surface the three new V2 pricing-override fields (`specialTariffId`, `specialPrice`, `discountPercent`) in the
HTTP request DTO and the internal use-case command. Add Bean Validation constraints for `discountPercent`
(range [0, 100]) and a cross-field constraint that rejects requests supplying both `specialTariffId` and
`discountPercent` simultaneously.

## 2. Files to Modify

---

### 2a. `PercentValue.java` — New shared validation annotation

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/web/support/PercentValue.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.shared.web.support;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Min(value = 0L, message = "must be between 0 and 100")
@Max(value = 100L, message = "must be between 0 and 100")
@Target({TYPE_USE, METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface PercentValue {

    String message() default "must be between 0 and 100";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

---

### 2b. `CreateRentalRequest.java` — HTTP DTO

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/CreateRentalRequest.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import java.math.BigDecimal;
import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import com.github.jenkaby.bikerental.shared.web.support.PercentValue;
// io.swagger.v3.oas.annotations.media.Schema already imported
```

**Code to Add/Replace:**

Replace the entire record declaration. Current content:

```java
@Schema(description = "Request body for creating a rental via Fast Path")
public record CreateRentalRequest(
        @Schema(description = "Customer UUID") @NotNull(message = "Customer ID is required") UUID customerId,
        @Schema(description = "List of Equipment IDs to rent (preferred)") List<@Positive @NotNull Long> equipmentIds,
        @Schema(description = "Planned rental duration (ISO-8601)", example = "PT2H") @NotNull(message = "Duration is required") Duration duration,
        @Schema(description = "Optional tariff ID; auto-selected if not provided", example = "5") Long tariffId,
        @Schema(description = "Operator identifier", example = "operator-1") @NotBlank String operatorId
) {
}
```

Replace with:

```java
@Schema(description = "Request body for creating a rental via Fast Path")
public record CreateRentalRequest(
        @Schema(description = "Customer UUID") @NotNull(message = "Customer ID is required") UUID customerId,
        @Schema(description = "List of Equipment IDs to rent (preferred)") List<@Positive @NotNull Long> equipmentIds,
        @Schema(description = "Planned rental duration (ISO-8601)", example = "PT2H") @NotNull(message = "Duration is required") Duration duration,
        @Schema(description = "Optional tariff ID; auto-selected if not provided", example = "5") Long tariffId,
        @Schema(description = "Operator identifier", example = "operator-1") @NotBlank String operatorId,
        @Schema(description = "ID of a SPECIAL-type V2 tariff; mutually exclusive with discountPercent", example = "99")
        Long specialTariffId,
        @Schema(description = "Operator-provided fixed total; required when specialTariffId is set", example = "15.00")
        @MoneyAmount
        BigDecimal specialPrice,
        @Schema(description = "Discount percentage applied to the non-special subtotal (0-100); ignored when specialTariffId is set", example = "10")
        @PercentValue
        Integer discountPercent
) {
}
```

---

### 2c. Mutual-Exclusivity Cross-Field Validation

The mutual-exclusivity rule (`specialTariffId` and `discountPercent` cannot both be non-null) must be enforced.
Add a class-level `@AssertTrue` constraint directly inside the record using a `default` accessor method (Bean
Validation supports this on records in Jakarta EE 11 / Spring Boot 4).

Append the following accessor method **inside the record body** (after the last component, before the closing
brace):

```java
    @AssertTrue(message = "specialTariffId and discountPercent are mutually exclusive")
    @Schema(hidden = true)
    default boolean isSpecialTariffAndDiscountMutuallyExclusive() {
        return specialTariffId == null || discountPercent == null;
    }
```

Add the import:

```java
import jakarta.validation.constraints.AssertTrue;
```

---

### 2d. `CreateRentalUseCase.CreateRentalCommand` — Internal Command

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/CreateRentalUseCase.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
```

**Code to Add/Replace:**

Current `CreateRentalCommand` record:

```java
    record CreateRentalCommand(
            UUID customerId,
            List<Long> equipmentIds,
            Duration duration,
            String operatorId,
            Long tariffId  // Optional - Left it for special cases when need to apply custom tariff
    ) {
    }
```

Replace with:

```java
    record CreateRentalCommand(
            UUID customerId,
            List<Long> equipmentIds,
            Duration duration,
            String operatorId,
            Long tariffId,
            Long specialTariffId,
            Money specialPrice,
            DiscountPercent discountPercent
    ) {
    }
```

---

### 2e. `RentalCommandMapper` — DTO → Command mapping

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/mapper/RentalCommandMapper.java`
* **Action:** Modify Existing File

Two type conversions are needed:

- `BigDecimal → Money` for `specialPrice` — handled by `MoneyMapper` (already in `uses`)
- `Integer → DiscountPercent` for `discountPercent` — handled by `DiscountMapper` (must be added to `uses`)

Add `DiscountMapper.class` to the `@Mapper` `uses` list:

```java
// current
@Mapper(uses = {MoneyMapper.class, RentalQueryMapper.class})
// replace with
@Mapper(uses = {MoneyMapper.class, RentalQueryMapper.class, DiscountMapper.class})
```

Add the import:

```java
import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
```

No `@Mapping` annotations are needed — MapStruct matches fields by name and resolves the type conversions
automatically via the delegate mappers.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
