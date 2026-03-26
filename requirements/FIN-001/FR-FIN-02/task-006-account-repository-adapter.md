# Task 006: Extend AccountRepositoryAdapter — Implement findByCustomerId

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — Adapter is package-private; `@Repository` on the class;
> `@Transactional(readOnly = true)` at class level; write methods override with `@Transactional`. Follow the exact
> same delegation pattern as the existing `getSystemAccount()` method.

## 1. Objective

Implement the `findByCustomerId(UUID customerId)` operation on `AccountRepositoryAdapter`, satisfying the new
contract added to `AccountRepository` in Task 004. The implementation delegates to the new
`AccountJpaRepository.findByCustomerId(UUID)` method added in Task 005 and maps the result via `AccountJpaMapper`.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required (add to the existing import block):**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import java.util.Optional;
```

**Replace the entire file with:**

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.AccountJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountJpaMapper mapper;

    AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Account save(Account account) {
        var entity = mapper.toEntity(account);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Account getSystemAccount() {
        return jpaRepository.findByAccountType(AccountType.SYSTEM.name())
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, AccountType.SYSTEM.name()));
    }

    @Override
    public Optional<Account> findByCustomerId(CustomerRef customerRef) {
        return jpaRepository.findByCustomerId(customerRef.id())
                .map(mapper::toDomain);
    }
}
```

**Change summary:**

- Added `Optional<Account> findByCustomerId(CustomerRef customerRef)` as a new `@Override` at the bottom of the class.
- The UUID is extracted via `customerRef.id()` before being passed to the JPA derived query (which operates on the raw
  `customer_id` column).
- The method participates in the class-level `@Transactional(readOnly = true)` — no additional annotation needed.
- Added `CustomerRef` import; no raw `UUID` import needed in the adapter.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
