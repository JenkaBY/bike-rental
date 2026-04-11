# Task 004: Add `existsByRentalRefAndType` to `TransactionJpaRepository`

> **Applied Skill:** `spring-boot-data-ddd` — Spring Data derived query method; no native SQL; method name follows the
`existsBy` convention.

## 1. Objective

Add a derived `existsBy` query method to the JPA repository so that `TransactionRepositoryAdapter` can expose a cheap
existence check instead of loading a full entity list.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/repository/TransactionJpaRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None — `TransactionSourceType` and `TransactionType` are already imported.

**Code to Add/Replace:**

* **Location:** Inside the `TransactionJpaRepository` interface, directly **after** the
  `findAllBySourceTypeAndSourceIdAndTransactionType` method.

**Before:**

```java
    List<TransactionJpaEntity> findAllBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);
}
```

**After:**

```java
    List<TransactionJpaEntity> findAllBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);

    boolean existsBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
