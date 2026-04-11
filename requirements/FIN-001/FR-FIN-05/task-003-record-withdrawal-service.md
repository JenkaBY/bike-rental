# Task 003: Create RecordWithdrawalService

> **Applied Skill:** `java.instructions.md` — application service with `@Transactional` boundary, available-balance
> guard, double-entry journal construction; mirrors `RecordDepositService` with debit/credit sides reversed.
> Depends on task-008 (`CustomerAccount`, `SystemAccount`) and task-009 (narrowed `AccountRepository` port).

## 1. Objective

Implement `RecordWithdrawalService`, which:

1. Returns the stored result immediately when a duplicate `idempotencyKey` is detected (no balance mutation).
2. Loads and validates the customer `Account` and `System Account`.
3. Guards that `amount ≤ CUSTOMER_WALLET.balance − CUSTOMER_HOLD.balance` (available balance check).
4. Debits `CUSTOMER_WALLET` and credits the payout sub-ledger (`CASH`, `CARD_TERMINAL`, or `BANK_TRANSFER`).
5. Persists both account mutations and a `Transaction(type=WITHDRAWAL)` journal within a single `@Transactional`
   boundary.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordWithdrawalService.java`
* **Action:** Create New File

## 3. Code Implementation

> `isCustomerBalanceSufficient(Money)` is defined on `CustomerAccount` (task-008). `getWallet()` and
> `getOnHold()` are typed accessors on `CustomerAccount`. `getSystemAccount()` returns `SystemAccount`
> (task-009) — dynamic payout sub-ledger lookup via `systemAccount.getSubLedger(creditLedgerType)` still
> uses the inherited `Account.getSubLedger(LedgerType)` for runtime payment-method resolution.

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.application.mapper.PaymentMethodLedgerTypeMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
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
```

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.mapper.PaymentMethodLedgerTypeMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
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
public class RecordWithdrawalService implements RecordWithdrawalUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;
    private final PaymentMethodLedgerTypeMapper paymentMethodMapper;

    @Override
    @Transactional
    public WithdrawalResult execute(RecordWithdrawalCommand command) {
        Optional<Transaction> existing = transactionRepository
                .findByIdempotencyKeyAndCustomerId(command.idempotencyKey(), new CustomerRef(command.customerId()));
        if (existing.isPresent()) {
            Transaction t = existing.get();
            return new WithdrawalResult(t.getId(), t.getRecordedAt());
        }

        var customerAccount = accountRepository
                .findByCustomerId(new CustomerRef(command.customerId()))
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerId().toString()));

        var systemAccount = accountRepository.getSystemAccount();

        if (!customerAccount.isBalanceSufficient(command.amount())) {
            var available = customerAccount.getWallet().getBalance()
                    .subtract(customerAccount.getOnHold().getBalance());
            throw new InsufficientBalanceException(available, command.amount());
        }

        LedgerType creditLedgerType = paymentMethodMapper.toLedgerType(command.payoutMethod());
        var creditSubLedger = systemAccount.getSubLedger(creditLedgerType);

        var debitChange = customerAccount.getWallet().debit(command.amount());
        var creditChange = creditSubLedger.credit(command.amount());

        accountRepository.save(systemAccount);
        accountRepository.save(customerAccount);

        Instant now = clock.instant();
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.WITHDRAWAL)
                .paymentMethod(command.payoutMethod())
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

        return new WithdrawalResult(transactionId, now);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
