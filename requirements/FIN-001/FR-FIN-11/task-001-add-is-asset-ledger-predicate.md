# Task 001: Add `isAssetLedger()` Predicate to `LedgerType`

> **Applied Skill:** `java.instructions.md` — pattern-matching switch expression; expressive naming; no inline
> comments.

## 1. Objective

Add an `isAssetLedger()` predicate to the `LedgerType` enum that returns `true` for `CASH`, `CARD_TERMINAL`, and
`BANK_TRANSFER`. This isolates account-category knowledge inside the enum so `SubLedger` can branch on it without
hard-coding ledger names.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/LedgerType.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Add the new method immediately after the closing brace of the existing `isSystemLedger()` method,
  before the closing brace of the enum.

* **Current file state (for reference):**

```java
    public boolean isSystemLedger() {
        return switch (this) {
            case CASH, CARD_TERMINAL, BANK_TRANSFER, REVENUE, ADJUSTMENT -> true;
            default -> false;
        };
    }
}
```

* **Snippet — add `isAssetLedger()` after `isSystemLedger()`:**

```java
    public boolean isAssetLedger() {
        return switch (this) {
            case CASH, CARD_TERMINAL, BANK_TRANSFER -> true;
            default -> false;
        };
    }
```

**Full resulting enum body (for clarity):**

```java
public enum LedgerType {
    CASH,
    CARD_TERMINAL,
    BANK_TRANSFER,
    REVENUE,
    ADJUSTMENT,
    CUSTOMER_WALLET,
    CUSTOMER_HOLD;

    public boolean isSystemLedger() {
        return switch (this) {
            case CASH, CARD_TERMINAL, BANK_TRANSFER, REVENUE, ADJUSTMENT -> true;
            default -> false;
        };
    }

    public boolean isAssetLedger() {
        return switch (this) {
            case CASH, CARD_TERMINAL, BANK_TRANSFER -> true;
            default -> false;
        };
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
