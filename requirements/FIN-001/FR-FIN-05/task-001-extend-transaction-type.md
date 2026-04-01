# Task 001: Extend TransactionType with WITHDRAWAL

> **Applied Skill:** `java.instructions.md` — enum extension with backward-compatible value addition.

## 1. Objective

Add the `WITHDRAWAL` value to the existing `TransactionType` enum so that `RecordWithdrawalService` (Task 003)
can stamp every withdrawal journal entry with the correct transaction type.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionType.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None.

**Code to Add/Replace:**

* **Location:** Inside `TransactionType.java`, add `WITHDRAWAL` after `ADJUSTMENT` on a new line.

```java
public enum TransactionType {
    DEPOSIT,
    ADJUSTMENT,
    WITHDRAWAL,
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
