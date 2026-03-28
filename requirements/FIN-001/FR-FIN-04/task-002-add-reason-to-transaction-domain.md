# Task 002: Add `reason` Field to `Transaction` Domain Model

> **Applied Skill:** `spring-boot-data-ddd` — value-object immutability rule (return new instances, no setters).

## 1. Objective

Extend the `Transaction` domain aggregate with a nullable `reason` field so that every adjustment transaction
carries a mandatory, auditable reason text. Because `@Builder` regenerates at compile time, all existing
`Transaction.builder()` call sites are unaffected — `reason` defaults to `null` when not supplied.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/Transaction.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import org.jspecify.annotations.Nullable;
```

(already present in the file — confirm before adding)

**Code to Add/Replace:**

* **Location:** In the `Transaction` class body, add the `reason` field immediately after the `idempotencyKey` field
  and before the `records` field.

Replace:

```java
    private final Instant recordedAt;
    private final IdempotencyKey idempotencyKey;
    private final List<TransactionRecord> records;
```

With:

```java
    private final Instant recordedAt;
    private final IdempotencyKey idempotencyKey;
    @Nullable
    private final String reason;
    private final List<TransactionRecord> records;
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
