# Task 009: Create TransactionJpaMapper (MapStruct)

> **Applied Skill:** `mapstruct-hexagonal` — infrastructure/persistence mapper; `@AfterMapping` to wire
> bidirectional `transaction` back-reference; follows exact same style as `AccountJpaMapper`.

## 1. Objective

Create a MapStruct mapper that converts between the `Transaction` / `TransactionRecord` domain objects and
the `TransactionJpaEntity` / `TransactionRecordJpaEntity` JPA entities.

## 2. File to Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/mapper/TransactionJpaMapper.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**
```java
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import org.mapstruct.*;
```

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface TransactionJpaMapper {

    @Mapping(target = "records", source = "records")
    Transaction toDomain(TransactionJpaEntity entity);

    @Mapping(target = "records", source = "records")
    TransactionJpaEntity toEntity(Transaction domain);

    @AfterMapping
    default void setTransactionRelationships(Transaction domain, @MappingTarget TransactionJpaEntity result) {
        if (result.getRecords() != null) {
            result.getRecords().forEach(r -> r.setTransaction(result));
        }
    }

    @Mapping(target = "id", source = "id")
    TransactionRecord toDomain(TransactionRecordJpaEntity entity);

    @Mapping(target = "transaction", ignore = true)
    TransactionRecordJpaEntity toEntity(TransactionRecord domain);
}
```

> **Key points:**
> - `@AfterMapping` wires back the `transaction` reference on each `TransactionRecordJpaEntity` so JPA can
>   persist the FK `transaction_id` correctly — identical pattern to `AccountJpaMapper.setSubLedgerRelationships`.
> - `toEntity(TransactionRecord)` ignores `transaction` because the back-reference is set by `@AfterMapping`
>   on the parent mapper method.
> - Both domain `TransactionRecord` and entity `TransactionRecordJpaEntity` hold a `SubLedgerRef`, so MapStruct
>   maps the field directly — no expression or custom converter method is needed.
> - MapStruct compiler flag `-Amapstruct.unmappedTargetPolicy=ERROR` is active; every target field must be
>   either mapped or explicitly ignored.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

MapStruct annotation processor will validate all mappings at compile time. Build must succeed with zero
unmapped target field errors.
