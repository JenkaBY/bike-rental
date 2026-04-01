# Task 009: Update AccountRepository Port, AccountJpaMapper, and AccountRepositoryAdapter

> **Applied Skill:** `mapstruct-hexagonal` — split `toDomain`/`toEntity` into subtype-specific methods;
> `java.instructions.md` — pattern-matching switch for polymorphic `save`; narrows repository return types
> to concrete subclasses.

## 1. Objective

Narrow the `AccountRepository` port and update the infrastructure layer to produce and consume the typed
`CustomerAccount` / `SystemAccount` subclasses introduced in task-008:

- `AccountRepository.getSystemAccount()` returns `SystemAccount`.
- `AccountRepository.findByCustomerId(...)` returns `Optional<CustomerAccount>`.
- `AccountJpaMapper` splits the single `toDomain` / `toEntity` pair into subtype-specific methods.
- `AccountRepositoryAdapter` routes `save()` through a pattern-matching switch and dispatches `toDomain`
  based on `AccountJpaEntity.getAccountType()`.

## 2. File to Modify / Create

* **File Path (1):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/AccountRepository.java`
* **Action:** Modify Existing File — replace entire contents

* **File Path (2):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/mapper/AccountJpaMapper.java`
* **Action:** Modify Existing File — replace entire contents

* **File Path (3):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`
* **Action:** Modify Existing File — replace entire contents

## 3. Code Implementation

### File 1 — `AccountRepository.java` (replace entirely)

```java
package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.SystemAccount;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    SystemAccount getSystemAccount();

    Optional<CustomerAccount> findByCustomerId(CustomerRef customerRef);
}
```

---

### File 2 — `AccountJpaMapper.java` (replace entirely)

> **MapStruct note:** `getAccountType()` is an abstract method on `Account`; each concrete subclass returns its
> fixed `AccountType`. MapStruct finds `getAccountType()` via getter convention and maps it to the `accountType`
> column in `AccountJpaEntity` automatically. `unmappedTargetPolicy=ERROR` is satisfied because every target
> field on both `CustomerAccount` and `SystemAccount` is explicitly mapped or resolved.

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.domain.model.SystemAccount;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import com.github.jenkaby.bikerental.shared.mapper.CustomerRefMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {MoneyMapper.class, CustomerRefMapper.class})
public interface AccountJpaMapper {

    @Mapping(target = "customerRef", source = "customerId")
    CustomerAccount toCustomerAccountDomain(AccountJpaEntity entity);

    SystemAccount toSystemAccountDomain(AccountJpaEntity entity);

    @Mapping(target = "customerId", source = "customerRef")
    @Mapping(target = "createdAt", ignore = true)
    AccountJpaEntity toEntity(CustomerAccount domain);

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AccountJpaEntity toEntity(SystemAccount domain);

    @AfterMapping
    default void setSubLedgerRelationships(@MappingTarget AccountJpaEntity result) {
        if (result.getSubLedgers() != null) {
            result.getSubLedgers().forEach(sl -> sl.setAccount(result));
        }
    }

    SubLedger toDomain(SubLedgerJpaEntity entity);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubLedgerJpaEntity toEntity(SubLedger domain);
}
```

> **`@AfterMapping` note:** The previous signature was `setSubLedgerRelationships(Account domain, @MappingTarget ...)`.
> The `domain` parameter was unused, so it is dropped. MapStruct applies this `@AfterMapping` to both `toEntity`
> overloads because both produce an `AccountJpaEntity` as their `@MappingTarget`.

---

### File 3 — `AccountRepositoryAdapter.java` (replace entirely)

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.SystemAccount;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
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
        AccountJpaEntity entity = switch (account) {
            case CustomerAccount ca -> mapper.toEntity(ca);
            case SystemAccount sa -> mapper.toEntity(sa);
            default -> throw new IllegalStateException("Unknown account type: " + account.getClass().getSimpleName());
        };
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public SystemAccount getSystemAccount() {
        return jpaRepository.findByAccountType(AccountType.SYSTEM)
                .map(mapper::toSystemAccountDomain)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, AccountType.SYSTEM.name()));
    }

    @Override
    public Optional<CustomerAccount> findByCustomerId(CustomerRef customerRef) {
        return jpaRepository.findByCustomerId(customerRef.id())
                .map(mapper::toCustomerAccountDomain);
    }

    private Account toDomain(AccountJpaEntity entity) {
        return entity.getAccountType() == AccountType.CUSTOMER
                ? mapper.toCustomerAccountDomain(entity)
                : mapper.toSystemAccountDomain(entity);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
