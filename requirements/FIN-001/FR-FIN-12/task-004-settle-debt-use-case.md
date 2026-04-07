# Task 004: Create SettleDebtUseCase Interface

> **Applied Skill:** `java.instructions.md` — Use-case interfaces follow the project pattern: one `execute` method,
> nested `Command` and `Result` records as inner types (mirror `ReturnEquipmentUseCase`).

## 1. Objective

Create the `SettleDebtUseCase` interface with nested `SettleDebtCommand` and `SettleDebtResult` records in the Rental
module's `application/usecase` package.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/SettleDebtUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
```

**Code to Add/Replace:**

* **Location:** New file — entire content below

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;

public interface SettleDebtUseCase {

    SettleDebtResult execute(SettleDebtCommand command);

    record SettleDebtCommand(
            CustomerRef customerRef,
            RentalRef rentalRef,
            String operatorId
    ) {
    }

    record SettleDebtResult(boolean settled) {
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
