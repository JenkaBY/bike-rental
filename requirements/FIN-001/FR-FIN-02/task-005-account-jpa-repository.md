# Task 005: Extend AccountJpaRepository — findByCustomerId Derived Query

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — One Spring Data JPA repository per aggregate root; use
> derived query method names for simple lookups; repository interface is `public` because it is consumed by the
> adapter in the same infrastructure layer.

## 1. Objective

Add a Spring Data JPA derived query method `findByCustomerId(UUID customerId)` to `AccountJpaRepository`. This
backs the new `AccountRepository.findByCustomerId(UUID)` domain port operation added in Task 004.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/repository/AccountJpaRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required (add to the existing import block):**

```java
// Optional is already in java.util — no extra imports needed beyond what Spring Data provides
```

**Replace the entire file with:**

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByAccountType(String accountType);

    Optional<AccountJpaEntity> findByCustomerId(UUID customerId);
}
```

**Change summary:**

- Added `Optional<AccountJpaEntity> findByCustomerId(UUID customerId)` derived query below `findByAccountType`.
- Spring Data JPA will generate the WHERE clause `WHERE customer_id = ?` automatically from the method name,
  matching the `customer_id` column on `finance_accounts`.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
