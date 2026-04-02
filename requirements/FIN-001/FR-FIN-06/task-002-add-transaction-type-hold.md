# Task 002: Add `HOLD` to `TransactionType`

> **Applied Skill:** `java.instructions.md` — extend existing enum without breaking existing switch consumers.
> **Depends on:** Nothing — standalone enum change.

## 1. Objective

Add the `HOLD` value to the `TransactionType` enum so the application service can tag hold transactions with the
correct type. `TransactionType` is stored as a `VARCHAR` column (`transaction_type`) in `finance_transactions`;
no Liquibase changeset is needed.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionType.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None.

**Location:** Add `HOLD` after the existing `WITHDRAWAL` value.

* **Old code:**

```java
public enum TransactionType {
    DEPOSIT,
    ADJUSTMENT,
    WITHDRAWAL,
}
```

* **New code:**

```java
public enum TransactionType {
    DEPOSIT,
    ADJUSTMENT,
    WITHDRAWAL,
    HOLD,
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
