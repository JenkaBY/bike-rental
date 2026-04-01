# Task 002: Create RecordWithdrawalUseCase

> **Applied Skill:** `java.instructions.md` — use-case interface with nested command/result records; follows exact
> same structure as `RecordDepositUseCase`.

## 1. Objective

Create the `RecordWithdrawalUseCase` port interface with its nested `RecordWithdrawalCommand` and `WithdrawalResult`
records. This is the application-layer contract that `WithdrawalCommandController` (Task 005) calls and
`RecordWithdrawalService` (Task 003) implements.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/RecordWithdrawalUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public interface RecordWithdrawalUseCase {

    WithdrawalResult execute(RecordWithdrawalCommand command);

    record RecordWithdrawalCommand(
            UUID customerId,
            Money amount,
            PaymentMethod payoutMethod,
            String operatorId,
            IdempotencyKey idempotencyKey
    ) {
    }

    record WithdrawalResult(
            UUID transactionId,
            Instant recordedAt
    ) {
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
