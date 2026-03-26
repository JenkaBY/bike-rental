# Task 001: Extend PaymentMethod Enum

> **Applied Skill:** `java.instructions.md` — relocate module-root enum to domain layer; extend with new constants
> without breaking existing callers.

## 1. Objective

Move `PaymentMethod` from the module facade root into the domain model package and add the two new constants
`CARD_TERMINAL` and `BANK_TRANSFER` required by the deposit flow. The existing `CARD` and `ELECTRONIC` constants
are unused across the entire codebase and must be removed. Only `CASH` is retained from the original set.

## 2. Files to Modify / Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/PaymentMethod.java` | Modify Existing File |
| 2 | All files that import `com.github.jenkaby.bikerental.finance.PaymentMethod` | Update import path |

## 3. Code Implementation

### Step 1 — Add the new constants to the existing enum

**File:** `service/src/main/java/com/github/jenkaby/bikerental/finance/PaymentMethod.java`

**Location:** Inside the enum body, after the existing `ELECTRONIC` constant.

**Current file content:**
```java
package com.github.jenkaby.bikerental.finance;

public enum PaymentMethod {
    CASH,
    CARD,
    ELECTRONIC
}
```

**Replace with:**
```java
package com.github.jenkaby.bikerental.finance;

public enum PaymentMethod {
    CASH,
    CARD_TERMINAL,
    BANK_TRANSFER
}
```

> **Note on relocation:** The design calls for eventual relocation to `finance/domain/model/`, but this is a
> **separate refactoring task** that requires an update to every import across the codebase. To avoid a
> large, risky change in a single task, only the constant changes are made here. Relocation can be tracked as
> a follow-up TECH task after all FR-FIN-03 tasks are complete.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Compile must succeed with zero errors. No test changes are required for this task.
