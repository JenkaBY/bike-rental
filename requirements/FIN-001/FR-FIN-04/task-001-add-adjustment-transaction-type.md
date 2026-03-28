# Task 001: Add `ADJUSTMENT` to `TransactionType` Enum

> **Applied Skill:** N/A — single enum constant addition following project domain enum convention.

## 1. Objective

Add the `ADJUSTMENT` value to the `TransactionType` domain enum so that the new adjustment journal entry can be
distinguished from deposit and other transaction types at compile time.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionType.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None.

**Code to Add/Replace:**

* **Location:** Inside the `TransactionType` enum body, add `ADJUSTMENT` after the existing `DEPOSIT` constant.
* **Snippet:**

```java
public enum TransactionType {
    DEPOSIT,
    ADJUSTMENT,
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
