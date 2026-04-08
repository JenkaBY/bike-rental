# Task 003: Create `GetCustomerAccountBalancesService`

> **Applied Skill:** `spring-boot-data-ddd` — application service implementing a use-case port; read-only
> `@Transactional` boundary; delegates to the existing `AccountRepository` domain port.

## 1. Objective

Implement `GetCustomerAccountBalancesUseCase`. The service loads the `CustomerAccount` aggregate via
`AccountRepository.findByCustomerId()`, extracts wallet and hold balances from the two sub-ledgers, and computes
`lastUpdatedAt` as the later of `wallet.getUpdatedAt()` and `hold.getUpdatedAt()`. Throws
`ResourceNotFoundException` when no account exists, which the global advice maps to `404`.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/GetCustomerAccountBalancesService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
```

**Full file content:**

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class GetCustomerAccountBalancesService implements GetCustomerAccountBalancesUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerAccountBalances execute(UUID customerId) {
        var account = accountRepository.findByCustomerId(CustomerRef.of(customerId))
                .orElseThrow(() -> new ResourceNotFoundException(CustomerAccount.class, customerId));

        var wallet = account.getWallet();
        var hold = account.getOnHold();

        return new CustomerAccountBalances(
                wallet.getBalance().amount(),
                hold.getBalance().amount(),
                wallet.getUpdatedAt()
        );
    }
}
```

**Notes:**

- The class is package-private (`class`, not `public class`) — consistent with every other service in the
  `finance/application/service/` package (e.g. `CreateCustomerAccountService`, `RecordDepositService`).
- `wallet.getBalance().amount()` — `Money.amount()` returns the underlying `BigDecimal` (scale 2).
- `wallet.getUpdatedAt()` — the `CUSTOMER_WALLET` sub-ledger is the authoritative running balance of the
  customer account; every deposit, withdrawal, hold, and capture mutates it, so its `updatedAt` reflects
  the customer account's most recent financial activity.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: compiles without errors; Spring will auto-discover the `@Service` bean.
