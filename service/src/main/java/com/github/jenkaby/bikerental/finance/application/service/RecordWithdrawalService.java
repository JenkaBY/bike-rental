package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.mapper.PaymentMethodLedgerTypeMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase;
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
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
            var available = customerAccount.getWallet().getBalance().subtract(customerAccount.getOnHold().getBalance());
            throw new InsufficientBalanceException(available, command.amount());
        }

        var creditLedgerType = paymentMethodMapper.toLedgerType(command.payoutMethod());
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
