# Task 010: Create `AdjustmentRequest` and `AdjustmentResponse` DTOs

> **Applied Skill:** N/A — mirrors the record DTO pattern of `RecordDepositRequest` /
> `RecordDepositResponse` in `finance/web/command/dto/`.

## 1. Objective

Define the HTTP request and response records for the `POST /api/finance/adjustments` endpoint. The
`AdjustmentRequest` enforces Bean Validation constraints on all mandatory fields. The `amount` field is a
signed `BigDecimal` — negative values represent deductions, positive values represent top-ups.

## 2. File to Modify / Create

**File 1 — shared constraint annotation:**

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/web/support/MoneyAmount.java`
* **Action:** Create New File

**File 2:**

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/AdjustmentRequest.java`
* **Action:** Create New File

**File 3:**

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/AdjustmentResponse.java`
* **Action:** Create New File

## 3. Code Implementation

### `MoneyAmount.java`

Composed constraint following the `@Slug` pattern in `shared/web/support/`. Encapsulates
`@NotNull` and `@Digits(integer = 17, fraction = 2)` so any DTO field representing a monetary
`BigDecimal` can be annotated with a single `@MoneyAmount`.

```java
package com.github.jenkaby.bikerental.shared.web.support;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotNull
@Digits(integer = 17, fraction = 2)
@Target({TYPE_USE, METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface MoneyAmount {

    String message() default "must be a valid monetary amount";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

### `AdjustmentRequest.java`

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentRequest(
        @NotNull UUID customerId,
        @MoneyAmount BigDecimal amount,
        @NotBlank String reason,
        @NotBlank String operatorId,
        @NotNull UUID idempotencyKey
) {}
```

### `AdjustmentResponse.java`

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdjustmentResponse(
        UUID transactionId,
        BigDecimal newWalletBalance,
        Instant recordedAt
) {}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
