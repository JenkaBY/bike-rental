package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordWithdrawalService implements RecordWithdrawalUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public WithdrawalResult execute(RecordWithdrawalCommand command) {
        log.info("Recording withdrawal for customer={} amount={} via {}",
                command.customerId(), command.amount(), command.paymentMethod());
        Optional<Transaction> existing = transactionRepository
                .findByIdempotencyKeyAndCustomerId(command.idempotencyKey(), new CustomerRef(command.customerId()));
        if (existing.isPresent()) {
            Transaction t = existing.get();
            log.info("Withdrawal already recorded for customer={} idempotencyKey={}, returning existing transaction={}",
                    command.customerId(), command.idempotencyKey(), t.getId());
            return new WithdrawalResult(t.getId(), t.getRecordedAt());
        }

        var customerAccount = accountRepository
                .findByCustomerId(new CustomerRef(command.customerId()))
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerId().toString()));

        var systemAccount = accountRepository.getSystemAccount();

        if (!customerAccount.isBalanceSufficient(command.amount())) {
            throw new InsufficientBalanceException(customerAccount.availableBalance(), command.amount());
        }

        var creditSubLedger = systemAccount.getSubLedger(command.paymentMethod());

        var debitChange = customerAccount.getWallet().debit(command.amount());
        var creditChange = creditSubLedger.credit(command.amount());

        accountRepository.save(systemAccount);
        accountRepository.save(customerAccount);

        Instant now = timeProvider.nowInstant();
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.WITHDRAWAL)
                .paymentMethod(command.paymentMethod())
                .amount(command.amount())
                .customerId(command.customerId())
                .operatorId(command.operatorId())
                .sourceType(command.source())
                .sourceId(command.sourceId())
                .recordedAt(now)
                .idempotencyKey(command.idempotencyKey())
                .records(List.of(
                        debitChange.toTransaction(uuidGenerator.generate()),
                        creditChange.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(transaction);
        log.info("Withdrawal {} recorded for customer={} amount={}", transactionId, command.customerId(), command.amount());

        return new WithdrawalResult(transactionId, now);
    }
}
