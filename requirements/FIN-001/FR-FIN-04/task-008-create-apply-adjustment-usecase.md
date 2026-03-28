# Task 008: Create `ApplyAdjustmentUseCase` Interface

> **Applied Skill:** N/A — follows the established use-case interface pattern with nested command and result records
> (see `RecordDepositUseCase` at
> `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/RecordDepositUseCase.java`).

## 1. Objective

Define the application-layer port interface for the manual balance adjustment use case. The controller and service
are linked strictly through this interface; the command carries a signed `Money` amount (positive = top-up,
negative = deduction) plus mandatory `reason` and `operatorId` audit fields.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/ApplyAdjustmentUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** All listed below are included in the snippet.

**Code to Add/Replace:**

* **Location:** New file — paste the entire snippet as the file content.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public interface ApplyAdjustmentUseCase {

    AdjustmentResult execute(ApplyAdjustmentCommand command);

    record ApplyAdjustmentCommand(
            UUID customerId,
            Money amount,
            String reason,
            String operatorId
    ) {}

    record AdjustmentResult(
            UUID transactionId,
            Money newWalletBalance,
            Instant recordedAt
    ) {}
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
