# Task 005: Create `RecordRentalHoldService` and Required Domain Helpers

> **Applied Skill:** `java.instructions.md` — `var` for local inference; no inline comments; null-safe patterns.
> `springboot.instructions.md` — `@Service`, `@Transactional`, `@RequiredArgsConstructor`.
> **Depends on:** `task-001`, `task-002`, `task-003`, `task-004`.

## 1. Objective

This task covers four files:

1. Extend `UuidGenerator` port with a deterministic name-based UUID method and implement it in `UuidCreatorAdapter`.
2. Add `availableBalance()` helper to `CustomerAccount`.
3. Create `RecordRentalHoldService` implementing `RentalHoldUseCase`.

## 2. File to Modify / Create

### File 1 — `UuidGenerator.java` (extend port interface)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/port/uuid/UuidGenerator.java`
* **Action:** Modify Existing File

### File 2 — `UuidCreatorAdapter.java` (implement new method)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/port/uuid/UuidCreatorAdapter.java`
* **Action:** Modify Existing File

### File 3 — `CustomerAccount.java` (add `availableBalance()` helper)

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/CustomerAccount.java`
* **Action:** Modify Existing File

### File 4 — `RecordRentalHoldService.java` (new application service)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordRentalHoldService.java`
* **Action:** Create New File

## 3. Code Implementation

### File 1 — `UuidGenerator.java`

Add a `generateNameBased(String name)` default method. Using `default` keeps all existing implementations
(including test doubles) unbroken without forcing them to override.

**Imports to add:**

```java
import com.github.f4b6a3.uuid.UuidCreator;
```

**Location:** Add `generateNameBased` after the existing `generate()` declaration.

* **Old code:**

```java
public interface UuidGenerator {

    UUID generate();
}
```

* **New code:**

```java
public interface UuidGenerator {

    UUID generate();

    default UUID generateNameBased(String name) {
        return UuidCreator.getNameBasedMd5(name);
    }
}
```

---

### File 2 — `UuidCreatorAdapter.java`

No changes required — the `default` method on the interface is inherited automatically.

---

### File 3 — `CustomerAccount.java`

Add `availableBalance()` below the existing `isBalanceSufficient(Money amount)` method.

**Imports required:** None — `Money` is already in scope.

* **Location:** Insert after the closing brace of `isBalanceSufficient`.

* **Old code:**

```java
    public boolean isBalanceSufficient(Money amount) {
        var available = getWallet().getBalance().subtract(getOnHold().getBalance());
        return !available.isLessThan(amount);
    }
}
```

* **New code:**

```java
    public boolean isBalanceSufficient(Money amount) {
        var available = getWallet().getBalance().subtract(getOnHold().getBalance());
        return !available.isLessThan(amount);
    }

    public Money availableBalance() {
        return getWallet().getBalance().subtract(getOnHold().getBalance());
    }
}
```

---

### File 4 — `RecordRentalHoldService.java`

**Imports Required (File 4):**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
```

**Full file content (File 4):**

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecordRentalHoldService implements RentalHoldUseCase {

    private static final String SYSTEM_OPERATOR = "SYSTEM";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public HoldResult execute(RentalHoldCommand command) {
        var idempotencyKey = new IdempotencyKey(
                uuidGenerator.generateNameBased(String.valueOf(command.rentalRef().id()))
        );

        var existing = transactionRepository
                .findByIdempotencyKeyAndCustomerId(idempotencyKey, command.customerRef());
        if (existing.isPresent()) {
            var t = existing.get();
            return new HoldResult(new TransactionRef(t.getId()), t.getRecordedAt());
        }

        var customerAccount = accountRepository
                .findByCustomerId(command.customerRef())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Account.class, command.customerRef().id().toString()));

        if (!customerAccount.isBalanceSufficient(command.amount())) {
            throw new InsufficientBalanceException(customerAccount.availableBalance(), command.amount());
        }

        var debitChange = customerAccount.getWallet().debit(command.amount());
        var creditChange = customerAccount.getOnHold().credit(command.amount());

        accountRepository.save(customerAccount);

        Instant now = clock.instant();
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.HOLD)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(command.amount())
                .customerId(command.customerRef().id())
                .operatorId(SYSTEM_OPERATOR)
                .sourceType(TransactionSourceType.RENTAL)
                .sourceId(String.valueOf(command.rentalRef().id()))
                .recordedAt(now)
                .idempotencyKey(idempotencyKey)
                .reason(null)
                .records(List.of(
                        debitChange.toTransaction(uuidGenerator.generate()),
                        creditChange.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(transaction);

        return new HoldResult(new TransactionRef(transactionId), now);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
