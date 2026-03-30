# Task 006: Create `InsufficientBalanceException`

> **Applied Skill:** N/A — follows the established `BikeRentalException` subclass pattern (see
> `EquipmentNotAvailableException` for reference).

## 1. Objective

Create a new domain exception for the finance module that is thrown when an admin deduction would push
`CUSTOMER_WALLET` below zero. The exception carries the `finance.insufficient_balance` error code so that
the exception handler can surface it as a `422 Unprocessable Entity` with a machine-readable error code.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/exception/InsufficientBalanceException.java`
* **Action:** Create New File (also creates the new `exception/` package directory)

## 3. Code Implementation

**Imports Required:** All listed below are included in the snippet.

**Code to Add/Replace:**

* **Location:** New file — paste the entire snippet as the file content.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.finance.domain.exception;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class InsufficientBalanceException extends BikeRentalException {

    public static final String ERROR_CODE = "finance.insufficient_balance";

    private static final String MESSAGE_TEMPLATE =
            "Insufficient wallet balance. Available: %s, requested deduction: %s";

    public InsufficientBalanceException(Money available, Money requested) {
        super(MESSAGE_TEMPLATE.formatted(available.amount(), requested.amount()), ERROR_CODE);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
