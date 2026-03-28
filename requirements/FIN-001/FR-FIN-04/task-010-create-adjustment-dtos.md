# Task 010: Create `AdjustmentRequest` and `AdjustmentResponse` DTOs

> **Applied Skill:** N/A — mirrors the record DTO pattern of `RecordDepositRequest` /
> `RecordDepositResponse` in `finance/web/command/dto/`.

## 1. Objective

Define the HTTP request and response records for the `POST /api/finance/adjustments` endpoint. The
`AdjustmentRequest` enforces Bean Validation constraints on all mandatory fields. The `amount` field is a
signed `BigDecimal` — negative values represent deductions, positive values represent top-ups.

## 2. File to Modify / Create

**File 1:**

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/AdjustmentRequest.java`
* **Action:** Create New File

**File 2:**

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/AdjustmentResponse.java`
* **Action:** Create New File

## 3. Code Implementation

### `AdjustmentRequest.java`

```java
package com.github.jenkaby.bikerental.finance.web.command.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentRequest(
        @NotNull UUID customerId,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank String reason,
        @NotBlank String operatorId
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
