# Task 005: Extend TransactionJpaRepository and Implement Repository Adapter

> **Applied Skill:** `spring-boot-data-ddd` — JPA Repository with Specification; Repository Adapter implementation

## 1. Objective

Extend `TransactionJpaRepository` to support `JpaSpecificationExecutor` so that dynamic Specification-based queries
are available. Then implement the new `findTransactionHistory` method in `TransactionRepositoryAdapter`. The adapter
is responsible for all Spring ↔ domain conversions: it converts the domain `PageRequest` into a Spring `Pageable`
(with fixed `recordedAt DESC` sort), converts the domain `TransactionHistoryFilter` into a composed
`Specification<TransactionJpaEntity>`, and wraps the Spring `Page` result back into the shared domain
`Page<Transaction>` value object. A mandatory customer sub-ledger scope predicate (restricts results to
`CUSTOMER_WALLET` and `CUSTOMER_HOLD` entries) is always AND-combined with the caller-supplied filter predicates.

## 2. File to Modify / Create

### 2a.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/repository/TransactionJpaRepository.java`
* **Action:** Modify Existing File

### 2b.

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### 2a — `TransactionJpaRepository`

**Location:** The interface declaration line (currently `extends JpaRepository<TransactionJpaEntity, UUID>`).

**Replace the declaration line with:**

```java
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>,
        JpaSpecificationExecutor<TransactionJpaEntity> {
```

**Imports Required** (add if not already present):

```java
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
```

The full resulting file:

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>,
        JpaSpecificationExecutor<TransactionJpaEntity> {

    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, UUID customerId);

    Optional<TransactionJpaEntity> findBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);

    List<TransactionJpaEntity> findAllBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);
}
```

---

### 2b — `TransactionRepositoryAdapter`

**Imports Required** (add to existing imports):

```java
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.time.ZoneOffset;
import java.util.List;
```

**Location:** After the closing brace of `findAllByRentalRefAndType(...)`, before the class closing brace `}`.

**Snippet to add:**

```java
    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findTransactionHistory(CustomerRef customerId,
                                                    TransactionHistoryFilter filter,
                                                    PageRequest pageRequest) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                Sort.by(Sort.Direction.DESC, "recordedAt"));

        Specification<TransactionJpaEntity> spec =
                customerSubLedgerScope(customerId).and(filterSpec(filter));

        var springPage = jpaRepository.findAll(spec, pageable);
        var items = springPage.getContent().stream().map(mapper::toDomain).toList();
        return new Page<>(items, springPage.getTotalElements(), pageRequest);
    }

    private static Specification<TransactionJpaEntity> customerSubLedgerScope(CustomerRef customerId) {
        return (root, query, cb) -> {
            var customerPredicate = cb.equal(root.get("customerId"), customerId.id());
            var recordsJoin = root.join("records", JoinType.INNER);
            var customerLedgerTypes = List.of(LedgerType.CUSTOMER_WALLET, LedgerType.CUSTOMER_HOLD);
            var ledgerPredicate = recordsJoin.get("ledgerType").in(customerLedgerTypes);
            return cb.and(customerPredicate, ledgerPredicate);
        };
    }

    private static Specification<TransactionJpaEntity> filterSpec(TransactionHistoryFilter filter) {
        Specification<TransactionJpaEntity> spec = Specification.where(null);
        if (filter.fromDate() != null) {
            var from = filter.fromDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("recordedAt"), from));
        }
        if (filter.toDate() != null) {
            var to = filter.toDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("recordedAt"), to));
        }
        if (filter.sourceId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sourceId"), filter.sourceId()));
        }
        if (filter.sourceType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sourceType"), filter.sourceType()));
        }
        return spec;
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Build must complete with zero errors (the compile error from Task 004 is now resolved).
