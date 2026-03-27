# Task 010: Create TransactionRepositoryAdapter

> **Applied Skill:** `spring-boot-data-ddd` — hexagonal infrastructure adapter; package-private class
> implementing the domain port; follows exact same style as `AccountRepositoryAdapter`.

## 1. Objective

Create the JPA adapter that implements the `TransactionRepository` domain port. It receives a domain
`Transaction`, converts it to a JPA entity via `TransactionJpaMapper`, persists it through
`TransactionJpaRepository`, then maps the saved entity back to domain and returns it.

## 2. File to Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**
```java
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
```

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;

    TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public Transaction save(Transaction transaction) {
        var entity = mapper.toEntity(transaction);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(java.util.UUID idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toDomain);
    }

    // Note: The domain port now accepts an `IdempotencyKey` value object; the adapter converts
    // `IdempotencyKey` ↔ `UUID` when delegating to the JPA repository. Ensure `IdempotencyKeyMapper`
    // is available in `TransactionJpaMapper`.
}
```

> **Note:** The class is package-private (no `public` access modifier) following the convention established
> by `AccountRepositoryAdapter`. It is still registered as a Spring bean via `@Repository`.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Compilation must succeed. The Spring context wiring will be validated during component tests in Task 011.
