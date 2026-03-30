# Task 005: Centralize `INSUFFICIENT_BALANCE` Error Code

> **Applied Skill:** N/A — follows the existing `@UtilityClass` constant pattern in `ErrorCodes`.

## 1. Objective

`InsufficientBalanceException` already declares its own `ERROR_CODE = "finance.insufficient_balance"` string
literal, and `ErrorCodes.INSUFFICIENT_BALANCE` already holds the same value. The two constants are duplicates.

Centralize by making `InsufficientBalanceException.ERROR_CODE` reference `ErrorCodes.INSUFFICIENT_BALANCE`
so the string literal exists in exactly one place. `ErrorCodes.java` requires no change.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/exception/InsufficientBalanceException.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
```

**Code to Add/Replace:**

* **Location:** Replace the inline string literal in the `ERROR_CODE` field initializer.

Replace:

```java
    public static final String ERROR_CODE = "finance.insufficient_balance";
```

With:

```java
    public static final String ERROR_CODE = ErrorCodes.INSUFFICIENT_BALANCE;
```

No changes to `FinanceRestControllerAdvice` are needed — it already reads the code via `ex.getErrorCode()`
which delegates to `BikeRentalException`, so it picks up the new value automatically.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
