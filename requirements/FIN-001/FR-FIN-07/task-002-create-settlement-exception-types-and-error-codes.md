# Task 002: Create Settlement Exception Types and Add Error Codes

> **Applied Skill:** `java.instructions.md` — extend `BikeRentalException`; `UPPER_SNAKE_CASE` constants.
> `springboot.instructions.md` — handle edge cases with clear exception handling.
> **Depends on:** none.

## 1. Objective

Create one new domain exception in the finance module:

- `OverBudgetSettlementException` — thrown when `finalCost > holdBalance + walletBalance`, signalling that
  the customer does not have enough funds across hold and wallet to cover the final cost.

Also add the corresponding error-code constant to `ErrorCodes`.

## 2. File to Modify / Create

### File 1 — `ErrorCodes.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/ErrorCodes.java`
* **Action:** Modify Existing File

### File 2 — `OverBudgetSettlementException.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/exception/OverBudgetSettlementException.java`
* **Action:** Create New File

## 3. Code Implementation

### File 1 — `ErrorCodes.java`

**Location:** Add the new constant below the existing `INSUFFICIENT_BALANCE` constant (last line of the
class body).

* **Old code:**

```java
    public static final String INSUFFICIENT_BALANCE = "finance.insufficient_balance";
}
```

* **New code:**

```java
    public static final String INSUFFICIENT_BALANCE = "finance.insufficient_balance";
    public static final String OVER_BUDGET_SETTLEMENT = "finance.over_budget_settlement";
}
```

---

### File 2 — `OverBudgetSettlementException.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;
```

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.domain.exception;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

@Getter
public class OverBudgetSettlementException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.OVER_BUDGET_SETTLEMENT;

    private static final String MESSAGE_TEMPLATE = "Settlement rejected: final cost %s exceeds available balance %s";

    public OverBudgetSettlementException(Money finalCost, Money availableAmount) {
        super(
                MESSAGE_TEMPLATE.formatted(finalCost, availableAmount),
                ERROR_CODE,
                new Details(finalCost, availableAmount)
        );
    }

    public Details getDetails() {
        return getParams().map(params -> (Details) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Money finalCost, Money availableAmount) {}
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
