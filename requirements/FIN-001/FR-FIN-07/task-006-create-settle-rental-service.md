# Task 006: Create `SettleRentalService`

> **Applied Skill:** `java.instructions.md` — `var` for local inference; no inline comments; `@Nullable`.
> `springboot.instructions.md` — `@Service`, `@Transactional`, constructor injection.
> `spring-boot-data-ddd` — one aggregate per transaction; `@Transactional` on public methods.
> **Depends on:** task-001 (`CAPTURE`, `RELEASE` enum values), task-002 (exception classes),
> task-003 (`findByRentalRefAndType`), task-005 (`SettleRentalUseCase`).

## 1. Objective

Implement `SettleRentalService` — the application service that performs the full settlement (capture +
conditional release) atomically inside a single `@Transactional` boundary. Key behaviours:

- **Idempotency:** if a `CAPTURE` transaction already exists for this `rentalRef`, return the stored result
  without any balance mutation.
- **Over-budget guard:** if `finalCost > heldAmount`, throw `OverBudgetSettlementException` immediately.
- **Capture:** debit `CUSTOMER_HOLD` by `finalCost`, credit `REVENUE` by `finalCost`.
- **Release (conditional):** only when `excess > 0` — debit `CUSTOMER_HOLD` by `excess`, credit
  `CUSTOMER_WALLET` by `excess`. Zero-amount release entries are never created.
- **Atomicity:** both account saves and both transaction saves are within the same `@Transactional` scope.
  `TransactionRepositoryAdapter` uses `Propagation.MANDATORY`, so it participates in this transaction.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/SettleRentalService.java`
* **Action:** Create New File

## 3. Code Implementation

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientHoldException;
import com.github.jenkaby.bikerental.finance.domain.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecordWithoutId;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SettleRentalService implements SettleRentalUseCase {

    private static final String SYSTEM_OPERATOR = "SYSTEM";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public SettlementResult execute(SettleRentalCommand command) {
        var operatorId = command.operatorId() != null ? command.operatorId() : SYSTEM_OPERATOR;

        var existingCapture = transactionRepository.findByRentalRefAndType(
                command.rentalRef(), TransactionType.CAPTURE);
        if (existingCapture.isPresent()) {
            var capture = existingCapture.get();
            TransactionRef releaseRef = transactionRepository
                    .findByRentalRefAndType(command.rentalRef(), TransactionType.RELEASE)
                    .map(t -> new TransactionRef(t.getId()))
                    .orElse(null);
            return new SettlementResult(new TransactionRef(capture.getId()), releaseRef, capture.getRecordedAt());
        }

        var holdTransaction = transactionRepository
                .findByRentalRefAndType(command.rentalRef(), TransactionType.HOLD)
                .orElseThrow(() -> new InsufficientHoldException(command.rentalRef().id()));

        var heldAmount = holdTransaction.getAmount();
        if (command.finalCost().isMoreThan(heldAmount)) {
            throw new OverBudgetSettlementException(command.finalCost(), heldAmount);
        }

        var customerAccount = accountRepository
                .findByCustomerId(command.customerRef())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Account.class, command.customerRef().id().toString()));
        var systemAccount = accountRepository.getSystemAccount();

        var captureHoldDebit = customerAccount.getOnHold().debit(command.finalCost());
        var captureRevenueCredit = systemAccount.getRevenue().credit(command.finalCost());

        var excess = heldAmount.subtract(command.finalCost());
        TransactionRecordWithoutId releaseHoldDebit = null;
        TransactionRecordWithoutId releaseWalletCredit = null;
        if (excess.isPositive()) {
            releaseHoldDebit = customerAccount.getOnHold().debit(excess);
            releaseWalletCredit = customerAccount.getWallet().credit(excess);
        }

        accountRepository.save(customerAccount);
        accountRepository.save(systemAccount);

        Instant now = clock.instant();
        UUID captureId = uuidGenerator.generate();

        var captureTransaction = Transaction.builder()
                .id(captureId)
                .type(TransactionType.CAPTURE)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(command.finalCost())
                .customerId(command.customerRef().id())
                .operatorId(operatorId)
                .sourceType(TransactionSourceType.RENTAL)
                .sourceId(String.valueOf(command.rentalRef().id()))
                .recordedAt(now)
                .idempotencyKey(new IdempotencyKey(uuidGenerator.generate()))
                .reason(null)
                .records(List.of(
                        captureHoldDebit.toTransaction(uuidGenerator.generate()),
                        captureRevenueCredit.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(captureTransaction);

        TransactionRef releaseTransactionRef = null;
        if (releaseHoldDebit != null && releaseWalletCredit != null) {
            UUID releaseId = uuidGenerator.generate();
            var releaseTransaction = Transaction.builder()
                    .id(releaseId)
                    .type(TransactionType.RELEASE)
                    .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                    .amount(excess)
                    .customerId(command.customerRef().id())
                    .operatorId(operatorId)
                    .sourceType(TransactionSourceType.RENTAL)
                    .sourceId(String.valueOf(command.rentalRef().id()))
                    .recordedAt(now)
                    .idempotencyKey(new IdempotencyKey(uuidGenerator.generate()))
                    .reason(null)
                    .records(List.of(
                            releaseHoldDebit.toTransaction(uuidGenerator.generate()),
                            releaseWalletCredit.toTransaction(uuidGenerator.generate())
                    ))
                    .build();
            transactionRepository.save(releaseTransaction);
            releaseTransactionRef = new TransactionRef(releaseId);
        }

        return new SettlementResult(new TransactionRef(captureId), releaseTransactionRef, now);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
