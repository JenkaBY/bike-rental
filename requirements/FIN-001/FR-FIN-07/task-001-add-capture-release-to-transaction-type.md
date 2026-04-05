# Task 001: Add `CAPTURE` and `RELEASE` to `TransactionType`

> **Applied Skill:** `java.instructions.md` — enum extension; handle all new values in switch expressions.

## 1. Objective

Extend the `TransactionType` enum with the two values needed by the settlement flow: `CAPTURE` (for the revenue
capture entry) and `RELEASE` (for the excess-refund entry). Both values are stored as `VARCHAR(30)` in
`finance_transactions`; no Liquibase changeset is required.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionType.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None.

**Code to Replace:**

* **Location:** The enum body — append after the existing `HOLD` value.

* **Old code:**

```java
public enum TransactionType {
    DEPOSIT,
    ADJUSTMENT,
    WITHDRAWAL,
    HOLD
}
```

* **New code:**

```java
public enum TransactionType {
    DEPOSIT,
    ADJUSTMENT,
    WITHDRAWAL,
    HOLD,
    CAPTURE,
    RELEASE
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
