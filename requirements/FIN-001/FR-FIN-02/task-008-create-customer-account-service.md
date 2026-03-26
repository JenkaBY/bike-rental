# Task 008: Create CreateCustomerAccountService

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — `@Service`; `@Transactional` on the public method;
> build domain objects via Lombok builder before delegating to the repository port; never return JPA entities.
> `java.instructions.md` — Constructor injection only; no `@Autowired`; package-private class following existing
> service conventions (`RecordPaymentService`, `GetPaymentByIdService` are `public` — this service is package-private
> because it is not part of the module's public API).

## 1. Objective

Implement `CreateCustomerAccountService` — the application service that creates a Customer Finance Account.

Business rules enforced:

1. If `AccountRepository.findByCustomerId(customerId)` returns a non-empty result, throw
   `ResourceConflictException` (Scenario 3: duplicate prevention).
2. Otherwise, build an `Account` of type `CUSTOMER` with the provided `customerId` (wrapped in `CustomerRef`) and
   two `SubLedger` children: `CUSTOMER_WALLET` (balance `0.00`) and `CUSTOMER_HOLD` (balance `0.00`).
3. Generate UUID v7 identifiers for the `Account` and both `SubLedger` objects using `UuidGenerator`.
4. Persist via `AccountRepository.save(account)`.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/CreateCustomerAccountService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.CreateCustomerAccountUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.CreateCustomerAccountUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
class CreateCustomerAccountService implements CreateCustomerAccountUseCase {

    private final AccountRepository accountRepository;
    private final UuidGenerator uuidGenerator;

    CreateCustomerAccountService(AccountRepository accountRepository, UuidGenerator uuidGenerator) {
        this.accountRepository = accountRepository;
        this.uuidGenerator = uuidGenerator;
    }

    @Override
    @Transactional
    public void execute(UUID customerId) {
        accountRepository.findByCustomerId(CustomerRef.of(customerId)).ifPresent(existing -> {
            throw new ResourceConflictException(Account.class, customerId.toString());
        });

        var wallet = SubLedger.builder()
                .id(uuidGenerator.generate())
                .ledgerType(LedgerType.CUSTOMER_WALLET)
                .balance(BigDecimal.ZERO)
                .build();

        var hold = SubLedger.builder()
                .id(uuidGenerator.generate())
                .ledgerType(LedgerType.CUSTOMER_HOLD)
                .balance(BigDecimal.ZERO)
                .build();

        var account = Account.builder()
                .id(uuidGenerator.generate())
                .accountType(AccountType.CUSTOMER)
                .customerRef(CustomerRef.of(customerId))
                .subLedgers(List.of(wallet, hold))
                .build();

        accountRepository.save(account);
    }
}
```

**Key points:**

- `ResourceConflictException` is the existing shared exception mapped to HTTP 409 by `CoreExceptionHandlerAdvice`.
- `CustomerRef.of(customerId)` handles the `customerId`-to-`CustomerRef` wrapping; it is null-safe but `customerId`
  here is always non-null because it came directly from the `CustomerRegistered` event.
- `@Transactional` on `execute` participates in the enclosing transaction propagated by
  `FinanceCustomerEventListener`.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
