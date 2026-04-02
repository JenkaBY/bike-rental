# Task 002: Create Settlement Exception Types and Add Error Codes

> **Applied Skill:** `java.instructions.md` — extend `BikeRentalException`; `UPPER_SNAKE_CASE` constants.
> `springboot.instructions.md` — handle edge cases with clear exception handling.
> **Depends on:** none.

## 1. Objective

Create two new domain exceptions in the finance module:

- `OverBudgetSettlementException` — thrown when `finalCost > heldAmount`, signalling the Rental module to
  delegate to the over-budget path (FR-FIN-08).
- `InsufficientHoldException` — thrown when no HOLD transaction is found for the rental, indicating a data
  integrity problem.

Also add the corresponding error-code constants to `ErrorCodes`.

## 2. File to Modify / Create

### File 1 — `ErrorCodes.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/ErrorCodes.java`
* **Action:** Modify Existing File

### File 2 — `OverBudgetSettlementException.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/exception/OverBudgetSettlementException.java`
* **Action:** Create New File

### File 3 — `InsufficientHoldException.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/exception/InsufficientHoldException.java`
* **Action:** Create New File

## 3. Code Implementation

### File 1 — `ErrorCodes.java`

**Location:** Add the two new constants below the existing `INSUFFICIENT_BALANCE` constant (last line of the
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
    public static final String INSUFFICIENT_HOLD = "finance.insufficient_hold";
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

    private static final String MESSAGE_TEMPLATE = "Settlement rejected: final cost %s exceeds held amount %s";

    public OverBudgetSettlementException(Money finalCost, Money heldAmount) {
        super(
                MESSAGE_TEMPLATE.formatted(finalCost, heldAmount),
                ERROR_CODE,
                new Details(finalCost, heldAmount)
        );
    }

    public Details getDetails() {
        return getParams().map(params -> (Details) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Money finalCost, Money heldAmount) {}
}
```

---

### File 3 — `InsufficientHoldException.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;
```

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

@Getter
public class InsufficientHoldException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.INSUFFICIENT_HOLD;

    private static final String MESSAGE_TEMPLATE = "No hold transaction found for rental %d";

    public InsufficientHoldException(Long rentalId) {
        super(
                MESSAGE_TEMPLATE.formatted(rentalId),
                ERROR_CODE,
                new Details(rentalId)
        );
    }

    public Details getDetails() {
        return getParams().map(params -> (Details) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId) {}
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
