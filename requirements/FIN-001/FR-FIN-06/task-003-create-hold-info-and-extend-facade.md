# Task 003: Create `HoldInfo` Facade DTO and Extend `FinanceFacade`

> **Applied Skill:** `java.instructions.md` — Java Records for DTOs; facade interface as the sole cross-module
> contract.
> **Depends on:** `task-001` (`TransactionRef` and `RentalRef` must exist).

## 1. Objective

Add the `HoldInfo` result record to the `finance` facade package, then extend `FinanceFacade` with the
`holdFunds` method. Both files live in `com.github.jenkaby.bikerental.finance` — the same package as the
existing `FinanceFacade`, `PaymentInfo`, and `PaymentMethod` — so they are visible to the Rental module across
the Spring Modulith boundary.

## 2. File to Modify / Create

### File 1

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/HoldInfo.java`
* **Action:** Create New File

### File 2

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacade.java`
* **Action:** Modify Existing File

---

## 3. Code Implementation

### File 1 — `HoldInfo.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import java.time.Instant;
```

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.TransactionRef;

import java.time.Instant;

public record HoldInfo(TransactionRef transactionRef, Instant recordedAt) {
}
```

---

### File 2 — `FinanceFacade.java`

**Location:** Add the new method at the **bottom** of the interface, after the existing `getPayments` method.

**Imports to add at top of file:**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
```

*(Note: `Money` is already imported in current file — do not duplicate.)*

**Old code (last method):**

```java
    List<PaymentInfo> getPayments(Long rentalId);
}
```

**New code:**

```java
    List<PaymentInfo> getPayments(Long rentalId);

    HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost);
}
```

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
