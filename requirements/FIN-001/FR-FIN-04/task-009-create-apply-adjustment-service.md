# Task 009: Create `ApplyAdjustmentService`

> **Applied Skill:** `spring-boot-data-ddd` — `@Transactional` single-aggregate-per-transaction rule; service must
> flush both accounts before persisting the journal entry. Follows `RecordDepositService` structure precisely.

## 1. Objective

Implement the adjustment application service that:

1. Resolves both participating sub-ledgers from the domain repositories.
2. For **deductions** (negative amount): validates that `CUSTOMER_WALLET.balance ≥ |amount|`; throws
   `InsufficientBalanceException` with available vs. requested amounts if not.
3. Mutates sub-ledger balances in-memory via the existing `credit()` / `debit()` domain methods.
4. Saves both account aggregates to flush the balance changes.
5. Builds a `Transaction` with `type=ADJUSTMENT`, `paymentMethod=null`, `reason` from command, and two
   `TransactionRecord` children.
6. Saves the transaction journal entry.
7. Returns `AdjustmentResult { transactionId, newWalletBalance, recordedAt }`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/ApplyAdjustmentService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** All listed below are included in the snippet.

**Code to Add/Replace:**

* **Location:** New file — paste the entire snippet as the file content.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase;
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
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
public class ApplyAdjustmentService implements ApplyAdjustmentUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public AdjustmentResult execute(ApplyAdjustmentCommand command) {
        var customerAccount = accountRepository
                .findByCustomerId(new CustomerRef(command.customerId()))
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerId().toString()));

        var systemAccount = accountRepository.getSystemAccount();

        var walletSubLedger = customerAccount.getSubLedger(LedgerType.CUSTOMER_WALLET);
        var adjustmentSubLedger = systemAccount.getSubLedger(LedgerType.ADJUSTMENT);

        boolean isDeduction = command.amount().isNegative();
        Money absAmount = isDeduction
                ? Money.of(command.amount().amount().negate())
                : command.amount();

        if (isDeduction && walletSubLedger.getBalance().compareTo(absAmount) < 0) {
            throw new InsufficientBalanceException(walletSubLedger.getBalance(), absAmount);
        }

        final var debitChange;
        final var creditChange;

        if (isDeduction) {
            debitChange = walletSubLedger.debit(absAmount);
            creditChange = adjustmentSubLedger.credit(absAmount);
        } else {
            debitChange = adjustmentSubLedger.debit(absAmount);
            creditChange = walletSubLedger.credit(absAmount);
        }

        accountRepository.save(systemAccount);
        accountRepository.save(customerAccount);

        Instant now = clock.instant();
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.ADJUSTMENT)
                .paymentMethod(null)
                .amount(absAmount)
                .customerId(command.customerId())
                .operatorId(command.operatorId())
                .sourceType(null)
                .sourceId(null)
                .recordedAt(now)
                .idempotencyKey(IdempotencyKey.of(uuidGenerator.generate()))
                .reason(command.reason())
                .records(List.of(
                        debitChange.toTransaction(uuidGenerator.generate()),
                        creditChange.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(transaction);

        return new AdjustmentResult(transactionId, walletSubLedger.getBalance(), now);
    }
}
```

> **Implementation note on `var` with final:** Java does not allow `final var` together. Remove the `final` keyword
> from the `debitChange` / `creditChange` declarations — use plain `var debitChange;` and `var creditChange;` but
> declare them before the `if/else` block, which requires a type. Use the concrete type
> `TransactionRecordWithoutId` from `com.github.jenkaby.bikerental.finance.domain.model.TransactionRecordWithoutId`
> instead:
>
> ```java
> TransactionRecordWithoutId debitChange;
> TransactionRecordWithoutId creditChange;
>
> if (isDeduction) {
>     debitChange = walletSubLedger.debit(absAmount);
>     creditChange = adjustmentSubLedger.credit(absAmount);
> } else {
>     debitChange = adjustmentSubLedger.debit(absAmount);
>     creditChange = walletSubLedger.credit(absAmount);
> }
> ```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
