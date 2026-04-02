# Task 005: Create `SettleRentalUseCase` Interface

> **Applied Skill:** `java.instructions.md` — Java records for command/result types; interface for port
> definition. `springboot.instructions.md` — hexagonal port contract (interface in `application/usecase`).
> **Depends on:** task-004 (none strictly at compile time, but `SettlementResult` mirrors `SettlementInfo`).

## 1. Objective

Create the `SettleRentalUseCase` port interface — the internal application boundary between
`FinanceFacadeImpl` and `SettleRentalService`. Declare the nested `SettleRentalCommand` and
`SettlementResult` records inside the interface following the same pattern as `RentalHoldUseCase`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/SettleRentalUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public interface SettleRentalUseCase {

    SettlementResult execute(SettleRentalCommand command);

    record SettleRentalCommand(CustomerRef customerRef, RentalRef rentalRef, Money finalCost, String operatorId) {}

    record SettlementResult(
            TransactionRef captureTransactionRef,
            @Nullable TransactionRef releaseTransactionRef,
            Instant recordedAt) {}
}
```

## 4. Validation Steps

No need to validated
