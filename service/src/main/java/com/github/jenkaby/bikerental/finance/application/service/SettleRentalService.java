package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.*;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettleRentalService implements SettleRentalUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional(noRollbackFor = {OverBudgetSettlementException.class})
    public SettlementResult execute(SettleRentalCommand command) {
        log.info("Settlement for rental id=[{}]", command.rentalRef().id());
        var existingCaptures = transactionRepository.findAllByRentalRefAndType(command.rentalRef(), TransactionType.CAPTURE);
        if (!existingCaptures.isEmpty()) {
            var captureRefs = existingCaptures.stream().map(t -> new TransactionRef(t.getId())).toList();
            log.info("No settlement needed for rental id=[{}] as it exists with ids {}", command.rentalRef().id(), captureRefs);
            var releaseRef = transactionRepository
                    .findByRentalRefAndType(command.rentalRef(), TransactionType.RELEASE)
                    .map(t -> new TransactionRef(t.getId()))
                    .orElse(null);
            return new SettlementResult(captureRefs, releaseRef, existingCaptures.getFirst().getRecordedAt());
        }
        if (command.finalCost().isZero()) {
            log.info("No settlement needed for rental id=[{}] as final cost is zero", command.rentalRef().id());
            return new SettlementResult(List.of(), null, clock.instant());
        }
        var customerAccount = accountRepository.findByCustomerId(command.customerRef())
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerRef().id().toString()));
        var systemAccount = accountRepository.getSystemAccount();

        Optional<Transaction> holdOptional = transactionRepository.findByRentalRefAndType(command.rentalRef(), TransactionType.HOLD);
        if (holdOptional.isEmpty()) {
            log.info("No settlement needed for rental id=[{}] as no hold exists", command.rentalRef().id());
            return new SettlementResult(List.of(), null, clock.instant());
        }
        var holdTxN = holdOptional.get();
        var holdTxNAmount = holdTxN.getAmount();

        Instant now = clock.instant();
        var finalCost = command.finalCost();

        String sourceId = String.valueOf(command.rentalRef().id());
        if (holdTxNAmount.isMoreThan(finalCost)) {
            log.info("Final cost [{}] settlement needed for rental id=[{}] less than actual hold [{}]", finalCost, command.rentalRef().id(), holdTxN.getAmount());
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
                    .sourceId(sourceId)
                    .recordedAt(now)
                    .idempotencyKey(new IdempotencyKey(uuidGenerator.generate()))
                    .reason(null)
                    .records(List.of(
                            captureHoldDebit.toTransaction(uuidGenerator.generate()),
                            captureRevenueCredit.toTransaction(uuidGenerator.generate())
                    ))
                    .build();
            transactionRepository.save(captureTransaction);

            var excess = holdTxNAmount.subtract(finalCost);
            var releaseTransactionRef = commitReleaseTransaction(customerAccount, command, excess, now)
                    .map(t -> new TransactionRef(t.getId()))
                    .orElse(null);

            accountRepository.save(customerAccount);
            accountRepository.save(systemAccount);

            return new SettlementResult(List.of(new TransactionRef(captureId)), releaseTransactionRef, now);
        }

        var shortfall = finalCost.subtract(holdTxNAmount);
        if (customerAccount.getWallet().getBalance().isLessThan(shortfall)) {
            log.warn("Insufficient balance [{}] to cover shortfall [{}] for rental id=[{}]", customerAccount.getWallet().getBalance(),
                    shortfall.amount(), command.rentalRef().id());
            throw new OverBudgetSettlementException(finalCost,
                    holdTxNAmount.add(customerAccount.getWallet().getBalance()));
        }

        var captureRefs = new ArrayList<TransactionRef>();
        if (customerAccount.getOnHold().getBalance().isPositive()) {
            log.info("Capturing customer's on hold [{}] for rental id=[{}]", holdTxNAmount.amount(), command.rentalRef().id());
            var holdDebit = customerAccount.getOnHold().debit(holdTxNAmount);
            var holdRevenueCredit = systemAccount.getRevenue().credit(holdTxNAmount);
            UUID holdCaptureId = uuidGenerator.generate();
            transactionRepository.save(Transaction.builder()
                    .id(holdCaptureId)
                    .type(TransactionType.CAPTURE)
                    .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                    .amount(holdTxNAmount)
                    .customerId(command.customerRef().id())
                    .operatorId(command.operatorId())
                    .sourceType(TransactionSourceType.RENTAL)
                    .sourceId(sourceId)
                    .recordedAt(now)
                    .idempotencyKey(new IdempotencyKey(uuidGenerator.generate()))
                    .reason(null)
                    .records(List.of(
                            holdDebit.toTransaction(uuidGenerator.generate()),
                            holdRevenueCredit.toTransaction(uuidGenerator.generate())
                    ))
                    .build());
            captureRefs.add(new TransactionRef(holdCaptureId));
        }

        if (shortfall.isPositive()) {
            log.info("Capturing shortfall {} for rental {}", shortfall.amount(), command.rentalRef().id());
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
                    .sourceId(sourceId)
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
        if (excess.isNegative()) {
            throw new IllegalArgumentException(
                    "Unreachable state. The excess for rental %s is negative %s".formatted(command.rentalRef().id(), excess));
        }
        return Optional.empty();
    }
}
