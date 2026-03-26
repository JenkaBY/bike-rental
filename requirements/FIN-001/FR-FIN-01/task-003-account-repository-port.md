# Task 003: Domain Port — AccountRepository

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — One repository interface per aggregate root, declared in the
> domain layer with no infrastructure imports.

## 1. Objective

Create the `AccountRepository` domain port interface in the `finance` module. It follows the same contract style as
`PaymentRepository`: `save` for writes and `getSystemAccount()` as a throwing getter that delegates internally to a
`findByAccountType("SYSTEM")` query (mirrors `PaymentRepository.get(UUID)`).

## 2. File to Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/AccountRepository.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.Account;
```

**Code to Add/Replace:**

* **Location:** New file — full interface body.

```java
package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Account;

public interface AccountRepository {

    Account save(Account account);

    Account getSystemAccount();
}
```

> **Note on `getSystemAccount()` semantics:** The implementation (task-009) must throw
> `ResourceNotFoundException` when no SYSTEM account row exists. This mirrors the `PaymentRepository.get(UUID)`
> pattern seen in `PaymentRepositoryAdapter`:
> ```java
> .orElseThrow(() -> new ResourceNotFoundException(Account.class, AccountType.SYSTEM.name()))
> ```

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```
