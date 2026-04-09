# Task 004: Create TransactionHistoryFilter Domain Record and Extend TransactionRepository Port

> **Applied Skill:** `spring-boot-data-ddd` — Repository Port Extension (domain interface)

## 1. Objective

Create the `TransactionHistoryFilter` domain record in the `finance` domain model layer (no Spring imports). Then add
a `findTransactionHistory` method to the `TransactionRepository` domain port using only shared value objects
(`Page<Transaction>`, `PageRequest`) and the new domain filter — keeping the port free of any Spring framework types.

## 2. File to Modify / Create

### 2a.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/TransactionHistoryFilter.java`
* **Action:** Create New File

### 2b.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/TransactionRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### 2a — `TransactionHistoryFilter.java`

```java
package com.github.jenkaby.bikerental.finance.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public record TransactionHistoryFilter(
        @Nullable LocalDate fromDate,
        @Nullable LocalDate toDate,
        @Nullable String sourceId,
        @Nullable TransactionSourceType sourceType
) {
    public static TransactionHistoryFilter empty() {
        return new TransactionHistoryFilter(null, null, null, null);
    }
}
```

---

### 2b — `TransactionRepository.java`

**Imports Required** (add alongside the existing imports):

```java
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
```

**Location:** Inside the `TransactionRepository` interface, after the last existing method
`findAllByRentalRefAndType(...)`.

**Snippet to add:**

```java
    Page<Transaction> findTransactionHistory(CustomerRef customerId,
                                             TransactionHistoryFilter filter,
                                             PageRequest pageRequest);
```

**Full resulting interface for reference:**

```java
package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId);

    Optional<Transaction> findByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    List<Transaction> findAllByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    Page<Transaction> findTransactionHistory(CustomerRef customerId,
                                             TransactionHistoryFilter filter,
                                             PageRequest pageRequest);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Compilation will fail in `TransactionRepositoryAdapter` until Task 005 is completed — that is expected. The compile
error confirms the contract change propagated correctly.
