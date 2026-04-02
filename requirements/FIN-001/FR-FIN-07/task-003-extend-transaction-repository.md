# Task 003: Extend `TransactionRepository` with `findByRentalRefAndType`

> **Applied Skill:** `spring-boot-data-ddd` — repository per aggregate root; derived query methods on Spring
> Data JPA repositories. `java.instructions.md` — `Optional<T>` for possibly-absent values.
> **Depends on:** task-001.

## 1. Objective

Add a new lookup method to the `TransactionRepository` domain port, the Spring Data JPA
`TransactionJpaRepository`, and the `TransactionRepositoryAdapter`. This method is used by `SettleRentalService`
both for idempotency checks (look up an existing `CAPTURE` or `RELEASE`) and to retrieve the original `HOLD`
transaction to read `heldAmount`.

## 2. File to Modify / Create

### File 1 — `TransactionRepository.java` (domain port)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/TransactionRepository.java`
* **Action:** Modify Existing File

### File 2 — `TransactionJpaRepository.java` (Spring Data JPA)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/repository/TransactionJpaRepository.java`
* **Action:** Modify Existing File

### File 3 — `TransactionRepositoryAdapter.java` (adapter)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### File 1 — `TransactionRepository.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
```

**Location:** Add `findByRentalRefAndType` after the existing `findByIdempotencyKeyAndCustomerId` method.

* **Old code:**

```java
    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId);
}
```

* **New code:**

```java
    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId);

    Optional<Transaction> findByRentalRefAndType(RentalRef rentalRef, TransactionType type);
}
```

---

### File 2 — `TransactionJpaRepository.java`

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
```

**Location:** Add a new derived-query method after the existing `findByIdempotencyKeyAndCustomerId` method.

* **Old code:**

```java
    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, UUID customerId);
}
```

* **New code:**

```java
    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, UUID customerId);

    Optional<TransactionJpaEntity> findBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);
}
```

---

### File 3 — `TransactionRepositoryAdapter.java`

**Imports Required (add to the existing import block):**

```java
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
```

**Location:** Add `findByRentalRefAndType` after the existing `findByIdempotencyKeyAndCustomerId` method.

* **Old code:**

```java
    @Override
    public Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId) {
        return jpaRepository.findByIdempotencyKeyAndCustomerId(idempotencyKey.id(), customerId.id())
                .map(mapper::toDomain);
    }
}
```

* **New code:**

```java
    @Override
    public Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId) {
        return jpaRepository.findByIdempotencyKeyAndCustomerId(idempotencyKey.id(), customerId.id())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByRentalRefAndType(RentalRef rentalRef, TransactionType type) {
        return jpaRepository.findBySourceTypeAndSourceIdAndTransactionType(
                        TransactionSourceType.RENTAL,
                        String.valueOf(rentalRef.id()),
                        type)
                .map(mapper::toDomain);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
