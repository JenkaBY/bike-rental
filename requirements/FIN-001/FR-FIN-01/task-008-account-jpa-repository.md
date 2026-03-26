# Task 008: Spring Data Repository — AccountJpaRepository

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — One Spring Data `JpaRepository` per aggregate root; derived query
> methods by property name; `@Repository` on the interface following `PaymentJpaRepository`.

## 1. Objective

Create the Spring Data JPA repository for `AccountJpaEntity`. It needs a single derived query method
`findByAccountType(String)` that `AccountRepositoryAdapter` (task-009) will call to implement
`getSystemAccount()`. Follows the exact structure of `PaymentJpaRepository`.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/repository/AccountJpaRepository.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full interface body.

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
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```
