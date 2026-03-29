# Task 008: Create TransactionJpaRepository (Spring Data)

> **Applied Skill:** `spring-boot-data-ddd` — Spring Data JPA repository interface; follows exact same style as
> `AccountJpaRepository` and `PaymentJpaRepository`.

## 1. Objective

Create the Spring Data JPA repository for `TransactionJpaEntity`. Only a `save` operation is needed for this
story; the interface inherits it from `JpaRepository`.

## 2. File to Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/repository/TransactionJpaRepository.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**
```java
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
```

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, CustomerRef customerId);
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

The interface must compile without errors.
