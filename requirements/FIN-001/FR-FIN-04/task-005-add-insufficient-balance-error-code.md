# Task 005: Add `INSUFFICIENT_BALANCE` Error Code to `ErrorCodes`

> **Applied Skill:** N/A — follows the existing `@UtilityClass` constant pattern in `ErrorCodes`.

## 1. Objective

Register a named error-code constant for the insufficient-balance failure so that the exception handler and any
future callers reference the same string without duplication.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/ErrorCodes.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None.

**Code to Add/Replace:**

* **Location:** Inside the `ErrorCodes` utility class, add the new constant after the last existing constant
  (`RESOURCE_OPTIMISTIC_LOCK`).

Replace:

```java
    public static final String RESOURCE_OPTIMISTIC_LOCK = "shared.resource.optimistic_lock";
}
```

With:

```java
    public static final String RESOURCE_OPTIMISTIC_LOCK = "shared.resource.optimistic_lock";
    public static final String INSUFFICIENT_BALANCE = "finance.insufficient_balance";
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
