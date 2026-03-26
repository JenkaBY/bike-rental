# Task 004: Extend AccountRepository Port — findByCustomerId

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — Domain repository is a pure interface (port); one repository
> per aggregate root; no JPA/Spring imports in the domain layer.

## 1. Objective

Add `findByCustomerId(CustomerRef customerRef)` to the `AccountRepository` domain port. This method is used
exclusively by `CreateCustomerAccountService` to check whether a Customer Finance Account already exists for the
given customer — satisfying the duplicate-prevention requirement in Scenario 3 of FR-FIN-02.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/AccountRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required (add to the existing import block):**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import java.util.Optional;
```

**Replace the entire file with:**

```java
package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Account getSystemAccount();

    Optional<Account> findByCustomerId(CustomerRef customerRef);
}
```

**Change summary:**

- Added `Optional<Account> findByCustomerId(CustomerRef customerRef)` below the existing `getSystemAccount()` method.
- Added `CustomerRef` and `Optional` imports. No raw `UUID` import needed in the domain port.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
