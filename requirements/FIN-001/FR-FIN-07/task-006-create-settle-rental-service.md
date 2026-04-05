# Task 006: Create `SettleRentalService`

> **Applied Skill:** `java.instructions.md` — `var` for local inference; no inline comments.
> `springboot.instructions.md` — `@Service`, `@Transactional`, constructor injection.
> `spring-boot-data-ddd` — one aggregate per transaction; `@Transactional` on public methods.
> **Depends on:** task-001 (`CAPTURE`, `RELEASE` enum values), task-002 (`OverBudgetSettlementException`),
> task-003 (`findByRentalRefAndType`), task-005 (`SettleRentalUseCase`).

## 1. Objective

Implement `SettleRentalService` — the application service that performs the full settlement atomically
inside a single `@Transactional` boundary. Operators review the customer's balance and final rental cost
before initiating capture; they may ask the customer to fund their wallet if the hold is insufficient.

Key behaviours:

- **Idempotency:** query all existing `CAPTURE` transactions for `rentalRef`. If any exist, return them as
  `captureTransactionRefs` (plus the `RELEASE` ref if present) without any balance mutation.
- **Hold-sufficient path** (`finalCost <= holdBalance`): debit `CUSTOMER_HOLD` by `finalCost`, credit
  `REVENUE` by `finalCost`. Save both accounts, persist one `CAPTURE` transaction. If `excess =
  remainingHoldBalance > 0`, call `commitReleaseTransaction` to produce a `RELEASE` transaction:
  debit `CUSTOMER_HOLD` by `excess`, credit `CUSTOMER_WALLET` by `excess`. Zero-amount entries are never
  created. Return `SettlementResult(List.of(captureRef), releaseRef, now)`.
- **Hold-insufficient path** (`finalCost > holdBalance`): compute `shortfall = finalCost − holdBalance`.
  If `walletBalance < shortfall`, throw `OverBudgetSettlementException(finalCost, holdBalance + walletBalance)`.
  Otherwise produce up to two `CAPTURE` transactions — no `RELEASE`:
  - **HOLD CAPTURE**: debit `CUSTOMER_HOLD` by `holdBalance`, credit `REVENUE` by `holdBalance`.
  - **WALLET CAPTURE** (skipped when `shortfall == 0`): debit `CUSTOMER_WALLET` by `shortfall`, credit `REVENUE` by
    `shortfall`.
    Save both accounts once (after all ledger mutations), then persist the transaction(s). Return
    `SettlementResult(captureRefs, null, now)`.
- **`SettlementResult`** uses `List<TransactionRef> captureTransactionRefs` instead of a single ref.
- **Atomicity:** all account saves and transaction saves are within the same `@Transactional` scope.
  `TransactionRepositoryAdapter` uses `Propagation.MANDATORY`, so it participates in this transaction.

## 2. Files to Modify / Create

### 2a. `SettleRentalUseCase.java` — modify `SettlementResult`

**File Path:**
`service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/SettleRentalUseCase.java`

Replace the existing `SettlementResult` record:

```java
// Add to imports:

import java.util.List;

// Replace:
record SettlementResult(
        TransactionRef captureTransactionRef,
        @Nullable TransactionRef releaseTransactionRef,
        Instant recordedAt) {
}

// With:
record SettlementResult(
        List<TransactionRef> captureTransactionRefs,
        @Nullable TransactionRef releaseTransactionRef,
        Instant recordedAt) {
}
```

> **Note:** All call sites that read `captureTransactionRef` must be updated to `captureTransactionRefs`
> (e.g. `FinanceFacadeImpl`).

---

### 2b. `TransactionRepository.java` — add list query

**File Path:**
`service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/TransactionRepository.java`

Add below the existing `findByRentalRefAndType` declaration:

```java
// Add to imports:

import java.util.List;

// Add method:

List<Transaction> findAllByRentalRefAndType(RentalRef rentalRef, TransactionType type);
```

> The infrastructure adapter (`TransactionRepositoryAdapter`) must implement this method.

---

### 2c. `SettleRentalService.java` — create new file

**File Path:**
`service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/SettleRentalService.java`
**Action:** Create New File

## 3. Code Implementation

**Snippet:**

```java
package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.finance.domain.model.*;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SettleRentalService implements SettleRentalUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public SettlementResult execute(SettleRentalCommand command) {
      var existingCaptures = transactionRepository.findAllByRentalRefAndType(command.rentalRef(), TransactionType.CAPTURE);
      if (!existingCaptures.isEmpty()) {
        var captureRefs = existingCaptures.stream().map(t -> new TransactionRef(t.getId())).toList();
        var releaseRef = transactionRepository
                    .findByRentalRefAndType(command.rentalRef(), TransactionType.RELEASE)
                    .map(t -> new TransactionRef(t.getId()))
                    .orElse(null);
        return new SettlementResult(captureRefs, releaseRef, existingCaptures.getFirst().getRecordedAt());
        }

      var customerAccount = accountRepository.findByCustomerId(command.customerRef())
              .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerRef().id().toString()));
        var systemAccount = accountRepository.getSystemAccount();

      var holdBalance = customerAccount.getOnHold().getBalance();
        Instant now = clock.instant();
      var finalCost = command.finalCost();

      if (!finalCost.isMoreThan(holdBalance)) {
        var captureHoldDebit = customerAccount.getOnHold().debit(finalCost);
        var captureRevenueCredit = systemAccount.getRevenue().credit(finalCost);

        UUID captureId = uuidGenerator.generate();
        var captureTransaction = Transaction.builder()
                .id(captureId)
                .type(TransactionType.CAPTURE)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(finalCost)
                .customerId(command.customerRef().id())
                .operatorId(command.operatorId())
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

        var excess = customerAccount.getOnHold().getBalance();
        var releaseTransactionRef = commitReleaseTransaction(customerAccount, command, excess, now)
                .map(t -> new TransactionRef(t.getId()))
                .orElse(null);

        accountRepository.save(customerAccount);
        accountRepository.save(systemAccount);

        return new SettlementResult(List.of(new TransactionRef(captureId)), releaseTransactionRef, now);
      }

      var shortfall = finalCost.subtract(holdBalance);
      if (customerAccount.getWallet().getBalance().isLessThan(shortfall)) {
        throw new OverBudgetSettlementException(finalCost,
                holdBalance.add(customerAccount.getWallet().getBalance()));
      }

      var holdDebit = customerAccount.getOnHold().debit(holdBalance);
      var holdRevenueCredit = systemAccount.getRevenue().credit(holdBalance);

      var captureRefs = new ArrayList<TransactionRef>();

      UUID holdCaptureId = uuidGenerator.generate();
      transactionRepository.save(Transaction.builder()
              .id(holdCaptureId)
              .type(TransactionType.CAPTURE)
              .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
              .amount(holdBalance)
              .customerId(command.customerRef().id())
              .operatorId(command.operatorId())
              .sourceType(TransactionSourceType.RENTAL)
              .sourceId(String.valueOf(command.rentalRef().id()))
              .recordedAt(now)
              .idempotencyKey(new IdempotencyKey(uuidGenerator.generate()))
              .reason(null)
              .records(List.of(
                      holdDebit.toTransaction(uuidGenerator.generate()),
                      holdRevenueCredit.toTransaction(uuidGenerator.generate())
              ))
              .build());
      captureRefs.add(new TransactionRef(holdCaptureId));

      if (shortfall.isPositive()) {
        var walletDebit = customerAccount.getWallet().debit(shortfall);
        var walletRevenueCredit = systemAccount.getRevenue().credit(shortfall);
        UUID walletCaptureId = uuidGenerator.generate();
        transactionRepository.save(Transaction.builder()
                .id(walletCaptureId)
                .type(TransactionType.CAPTURE)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(shortfall)
                .customerId(command.customerRef().id())
                .operatorId(command.operatorId())
                .sourceType(TransactionSourceType.RENTAL)
                .sourceId(String.valueOf(command.rentalRef().id()))
                .recordedAt(now)
                .idempotencyKey(new IdempotencyKey(uuidGenerator.generate()))
                .reason(null)
                .records(List.of(
                        walletDebit.toTransaction(uuidGenerator.generate()),
                        walletRevenueCredit.toTransaction(uuidGenerator.generate())
                ))
                .build());
        captureRefs.add(new TransactionRef(walletCaptureId));
      }

      accountRepository.save(customerAccount);
      accountRepository.save(systemAccount);

      return new SettlementResult(captureRefs, null, now);
    }

  private Optional<Transaction> commitReleaseTransaction(CustomerAccount account, SettleRentalCommand command, Money excess, Instant now) {
    if (excess.isPositive()) {
      var releaseHoldDebit = account.getOnHold().debit(excess);
      var releaseWalletCredit = account.getWallet().credit(excess);
            var releaseTransaction = Transaction.builder()
                    .id(uuidGenerator.generate())
                    .type(TransactionType.RELEASE)
                    .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                    .amount(excess)
                    .customerId(command.customerRef().id())
                    .operatorId(command.operatorId())
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
      return Optional.of(transactionRepository.save(releaseTransaction));
        }
    return Optional.empty();
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
