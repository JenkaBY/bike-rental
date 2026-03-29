# Task 011: Create RecordDepositUseCase and RecordDepositService

> **Applied Skill:** `java.instructions.md` — use-case interface with nested command record; application service
> implementing it; `@Transactional` boundary; follows exact same pattern as `RecordPaymentUseCase` /
> `RecordPaymentService`.

## 1. Objective

Create the `RecordDepositUseCase` port interface (with its nested `RecordDepositCommand` record) and the
`RecordDepositService` application service that implements the full atomic deposit flow: account lookup,
in-memory balance mutation, journal construction, and persistence of both mutated accounts and the new
transaction — all within a single `@Transactional` boundary.

## 2. Files to Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/RecordDepositUseCase.java` | Create New File |
| 2 | `service/src/main/java/com/github/jenkaby/bikerental/finance/application/mapper/PaymentMethodLedgerTypeMapper.java` | Create New File |
| 3 | `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordDepositService.java` | Create New File |

## 3. Code Implementation

### File 1 — `RecordDepositUseCase.java`

```java
package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public interface RecordDepositUseCase {

    DepositResult execute(RecordDepositCommand command);

    record RecordDepositCommand(
            UUID customerId,
            com.github.jenkaby.bikerental.shared.domain.model.vo.Money amount,
            PaymentMethod paymentMethod,
            String operatorId,
            com.github.jenkaby.bikerental.shared.domain.IdempotencyKey idempotencyKey
    ) {
    }

    record DepositResult(
            UUID transactionId,
            java.time.Instant recordedAt
    ) {
    }
}
```

### File 2 — `PaymentMethodLedgerTypeMapper.java`

```java
package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper
public interface PaymentMethodLedgerTypeMapper {

    @ValueMapping(source = "CASH", target = "CASH")
    @ValueMapping(source = "CARD_TERMINAL", target = "CARD_TERMINAL")
    @ValueMapping(source = "BANK_TRANSFER", target = "BANK_TRANSFER")
    LedgerType toLedgerType(PaymentMethod paymentMethod);
}
```

> **Note:** Explicit `@ValueMapping` entries ensure every `PaymentMethod` constant is intentionally accounted
> for. MapStruct generates an `IllegalArgumentException` at runtime for any unmapped source constant, so a
> new `PaymentMethod` that is not listed here will surface immediately on first call.

### File 3 — `RecordDepositService.java`

**Imports Required:**
```java
import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.*;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
```

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.mapper.PaymentMethodLedgerTypeMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecordDepositService implements RecordDepositUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;
    private final PaymentMethodLedgerTypeMapper paymentMethodMapper;

    @Override
    @Transactional
    public DepositResult execute(RecordDepositCommand command) {
        Optional<Transaction> existing = transactionRepository
                .findByIdempotencyKeyAndCustomerId(command.idempotencyKey(), new CustomerRef(command.customerId()));
        if (existing.isPresent()) {
            Transaction t = existing.get();
            return new DepositResult(t.getId(), t.getRecordedAt());
        }

        var customerAccount = accountRepository
                .findByCustomerId(new CustomerRef(command.customerId()))
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerId().toString()));

        var systemAccount = accountRepository.getSystemAccount();

        LedgerType debitLedgerType = paymentMethodMapper.toLedgerType(command.paymentMethod());

        var debitSubLedger = systemAccount.getSubLedger(debitLedgerType);
        var creditSubLedger = customerAccount.getCustomerWallet();

        var debitChange = debitSubLedger.debit(command.amount());
        var creditChange = creditSubLedger.credit(command.amount());

        // Note: `AccountRepositoryAdapter.save(...)` merges domain sub-ledger balance changes into the
        // existing JPA-managed entities to preserve `@Version` on `SubLedgerJpaEntity`. This avoids
        // overwriting the version field with a freshly-mapped entity, which would trigger optimistic-lock
        // failures during concurrent updates.
        accountRepository.save(systemAccount);
        accountRepository.save(customerAccount);

        Instant now = clock.instant();
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.DEPOSIT)
                .paymentMethod(command.paymentMethod())
                .amount(command.amount())
                .customerId(command.customerId())
                .operatorId(command.operatorId())
                .sourceType(null)
                .sourceId(null)
                .recordedAt(now)
                .idempotencyKey(command.idempotencyKey())
                .records(List.of(
                        debitChange.toTransaction(uuidGenerator.generate()),
                        creditChange.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(transaction);

        return new DepositResult(transactionId, now);
    }

}
```

> **Key points:**
> - `PaymentMethodLedgerTypeMapper` maps `PaymentMethod` to the corresponding System Account `LedgerType`
>   via explicit `@ValueMapping` annotations. MapStruct generates an `IllegalArgumentException` for any
>   unmapped source constant, so new `PaymentMethod` values surface immediately on first call.
> - Both `accountRepository.save()` calls flush the mutated `SubLedger` balances because `AccountJpaEntity`
>   cascades `ALL` to `SubLedgerJpaEntity` and JPA merge propagates the dirty balance value through the
>   cascade chain.
> - `uuidGenerator.generate()` is called three times: once for the transaction ID and once per
>   `TransactionRecord`.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Compilation must succeed. Ensure no unmapped fields are reported by MapStruct (the service does not contain
mappers, but the compile goal runs annotation processing across the module).
