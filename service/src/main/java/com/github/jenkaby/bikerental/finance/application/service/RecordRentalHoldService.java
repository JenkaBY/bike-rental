package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
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
                .operatorId(command.operatorId())
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
