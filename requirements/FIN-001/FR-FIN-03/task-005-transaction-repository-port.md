# Task 005: Create TransactionRepository Domain Port

> **Applied Skill:** `java.instructions.md` — domain port interface in `domain/repository/`, no Spring imports;
> mirrors the existing `AccountRepository` and `PaymentRepository` contracts.

## 1. Objective

Define the `TransactionRepository` domain port interface. This is the only dependency that `RecordDepositService`
will require for persisting the `Transaction` aggregate. No query methods are needed for this story.

## 2. File to Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/TransactionRepository.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**
```java
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import java.util.Optional;
import java.util.UUID;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
```

```java
package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    // Idempotency lookup is intentionally scoped to the customer to avoid returning a transaction
    // that belongs to a different customer when the same idempotency key is reused across customers.
    Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

The interface must compile without errors.
