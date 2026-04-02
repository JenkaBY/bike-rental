# Task 004: Create `RentalHoldUseCase` Interface

> **Applied Skill:** `java.instructions.md` — Java Records for command/result; interface as port contract;
> `springboot.instructions.md` — hexagonal architecture use-case port pattern.
> **Depends on:** `task-001` (`CustomerRef`, `RentalRef`), `task-002` (`TransactionType.HOLD`).

## 1. Objective

Create the `RentalHoldUseCase` interface in `finance/application/usecase/`, declaring a single `execute` method
with `RentalHoldCommand` and `HoldResult` as inner records — following the exact pattern used by
`RecordWithdrawalUseCase`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/RentalHoldUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import java.time.Instant;
```

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;

public interface RentalHoldUseCase {

    HoldResult execute(RentalHoldCommand command);

    record RentalHoldCommand(CustomerRef customerRef, RentalRef rentalRef, Money amount) {
    }

    record HoldResult(TransactionRef transactionRef, Instant recordedAt) {
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
