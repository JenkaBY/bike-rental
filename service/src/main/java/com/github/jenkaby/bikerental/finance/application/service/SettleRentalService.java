package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientHoldException;
import com.github.jenkaby.bikerental.finance.domain.exception.OverBudgetSettlementException;
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
        var existingCapture = transactionRepository.findByRentalRefAndType(command.rentalRef(), TransactionType.CAPTURE);
        if (existingCapture.isPresent()) {
            var capture = existingCapture.get();
            var releaseRef = transactionRepository
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

        var customerAccount = accountRepository.findByCustomerId(command.customerRef())
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerRef().id().toString()));
        var systemAccount = accountRepository.getSystemAccount();

        var captureHoldDebit = customerAccount.getOnHold().debit(command.finalCost());
        var captureRevenueCredit = systemAccount.getRevenue().credit(command.finalCost());


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

        var excess = heldAmount.subtract(command.finalCost());
        var releaseTransactionRef = commitReleaseTransaction(customerAccount, command, excess, now)
                .map(t -> new TransactionRef(t.getId()))
                .orElse(null);

        return new SettlementResult(new TransactionRef(captureId), releaseTransactionRef, now);
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
