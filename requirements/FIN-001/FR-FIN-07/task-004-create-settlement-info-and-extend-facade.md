# Task 004: Create `SettlementInfo` Facade DTO and Extend `FinanceFacade`

> **Applied Skill:** `java.instructions.md` — Java records for DTOs; `@Nullable` for optional fields.
> `springboot.instructions.md` — cross-module facade boundary contract.
> **Depends on:** task-002 (exception types must exist for Javadoc accuracy; compilation works without it
> since exceptions are unchecked).

## 1. Objective

Create the public `SettlementInfo` record that `FinanceFacade.settleRental` returns, then add the
`settleRental` method signature to `FinanceFacade`. The record lives in the `finance` package root
so the Rental module can reference it across the module boundary without importing Finance internals.

## 2. File to Modify / Create

### File 1 — `SettlementInfo.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/SettlementInfo.java`
* **Action:** Create New File

### File 2 — `FinanceFacade.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacade.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### File 1 — `SettlementInfo.java`

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public record SettlementInfo(
        List<TransactionRef> captureTransactionRefs,
        @Nullable TransactionRef releaseTransactionRef,
        Instant recordedAt) {
}
```

---

### File 2 — `FinanceFacade.java`

**Imports Required (add to existing import block):**

```java
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
```

**Location:** Add `settleRental` after the existing `holdFunds` method — at the end of the interface, before
the closing brace.

* **Old code:**

```java
    HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost);
}
```

* **New code:**

```java
    HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost);

    SettlementInfo settleRental(CustomerRef customerRef, RentalRef rentalRef, Money finalCost, String operatorId);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> **Expected failure:** `FinanceFacadeImpl` will fail to compile because it does not yet implement
> `settleRental`. That is intentional — task-007 adds the implementation.
> Run only the compilation step and confirm the only error is the unimplemented interface method.
