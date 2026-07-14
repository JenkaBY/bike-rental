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
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordRentalHoldService implements RentalHoldUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public HoldResult execute(RentalHoldCommand command) {
        log.info("Recording rental hold for rental={} version={} customer={} amount={}",
                command.rentalRef().id(), command.rentalRef().rentalVersion(), command.customerRef().id(), command.amount());
        var idempotencyKey = new IdempotencyKey(
                uuidGenerator.generateNameBased(
                        "%s_%s".formatted(command.rentalRef().id(), command.rentalRef().rentalVersion()))
        );

        var existing = transactionRepository
                .findByIdempotencyKeyAndCustomerId(idempotencyKey, command.customerRef());
        if (existing.isPresent()) {
            var t = existing.get();
            log.info("Hold already recorded for rental={} version={} customer={}, returning existing transaction={}",
                    command.rentalRef().id(), command.rentalRef().rentalVersion(), command.customerRef().id(), t.getId());
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

        Instant now = timeProvider.nowInstant();
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
        log.info("Hold {} recorded for rental={} version={} amount={}",
                transactionId, command.rentalRef().id(), command.rentalRef().rentalVersion(), command.amount());

        return new HoldResult(new TransactionRef(transactionId), now);
    }
}
