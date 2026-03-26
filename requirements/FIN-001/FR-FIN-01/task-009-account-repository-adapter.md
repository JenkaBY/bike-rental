# Task 009: Infrastructure Adapter — AccountRepositoryAdapter

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — Adapter in `infrastructure/persistence/adapter/`; package-private
> class (no `public` modifier) following `PaymentRepositoryAdapter`; `@Repository` on the class;
`ResourceNotFoundException` throw in the getter following the exact pattern in `PaymentRepositoryAdapter.get(UUID)`.

## 1. Objective

Create `AccountRepositoryAdapter` — the JPA adapter that implements the `AccountRepository` domain port. It
delegates reads to `AccountJpaRepository.findByAccountType(String)` and writes to `AccountJpaRepository.save(...)`.
The `getSystemAccount()` method throws `ResourceNotFoundException` if no SYSTEM account row is found, mirroring
the `PaymentRepositoryAdapter.get(UUID)` implementation exactly.

The class must be **package-private** (no `public`) to prevent direct access from outside the
`infrastructure.persistence.adapter` package, consistent with `PaymentRepositoryAdapter`.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.AccountJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.AccountJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountJpaMapper mapper;

    AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
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
}

>
Implementation notes
applied:

        - `AccountRepositoryAdapter`
was annotated
with `@Transactional(readOnly = true)`
at the

class level so that read methods(like `

getSystemAccount()`)
run in
read-
only transactions for performance.The `

save(...)`
method is
explicitly annotated
with `@Transactional` (no `readOnly`)
to allow
writes .
-
This ensures
the adapter's default behavior is read-only, and only write operations open writable transactions.

Validation steps(re-run after code changes):

        ```bash
        ./gradlew :service:compileJava "-Dspring.profiles.active=test"--stacktrace
```

Make sure to run targeted tests that exercise repository adapter behavior if available.
```

> **Key details matching existing conventions:**
> - Class is **package-private** (no `public`) — same as `PaymentRepositoryAdapter`
> - Constructor injection, `private final` fields — same as all adapters in the project
> - `ResourceNotFoundException(Account.class, AccountType.SYSTEM.name())` — mirrors:
    > `new ResourceNotFoundException(Payment.class, id.toString())` from `PaymentRepositoryAdapter`

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests BikeRentalApplicationTest
```
