# Task 002: Add `INSUFFICIENT_FUNDS` and `HOLD_REQUIRED` to `ErrorCodes`

> **Applied Skill:** `java.instructions.md` — utility class constants; keep `ErrorCodes` as the single source of truth
> for all error code strings.

## 1. Objective

Add two new error code string constants to `ErrorCodes` so that both the new `RentalRestControllerAdvice` handlers and
any future code can reference them without magic strings.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/ErrorCodes.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None — file already compiles without additional imports.

**Code to Add/Replace:**

* **Location:** Inside the `ErrorCodes` class body, directly **after** the existing `INSUFFICIENT_HOLD` constant (last
  line of the class body).

**Before:**

```java
    public static final String INSUFFICIENT_BALANCE = "finance.insufficient_balance";
    public static final String OVER_BUDGET_SETTLEMENT = "finance.over_budget_settlement";
    public static final String INSUFFICIENT_HOLD = "finance.insufficient_hold";
}
```

**After:**

```java
    public static final String INSUFFICIENT_BALANCE = "finance.insufficient_balance";
    public static final String OVER_BUDGET_SETTLEMENT = "finance.over_budget_settlement";
    public static final String INSUFFICIENT_HOLD = "finance.insufficient_hold";
    public static final String INSUFFICIENT_FUNDS = "rental.insufficient_funds";
    public static final String HOLD_REQUIRED = "rental.hold.required";
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
