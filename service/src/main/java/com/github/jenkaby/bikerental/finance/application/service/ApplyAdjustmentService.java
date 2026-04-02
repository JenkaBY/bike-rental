package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase;
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecordWithoutId;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
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
public class ApplyAdjustmentService implements ApplyAdjustmentUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public AdjustmentResult execute(ApplyAdjustmentCommand command) {
        Optional<Transaction> existing = transactionRepository
                .findByIdempotencyKeyAndCustomerId(command.idempotencyKey(), new CustomerRef(command.customerId()));
        if (existing.isPresent()) {
            Transaction t = existing.get();
            return new AdjustmentResult(t.getId(), t.getRecordedAt());
        }
        var customerAccount = accountRepository
                .findByCustomerId(new CustomerRef(command.customerId()))
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, command.customerId().toString()));

        var systemAccount = accountRepository.getSystemAccount();

        var customerWallet = customerAccount.getWallet();
        var adjustmentSubLedger = systemAccount.getAdjustment();

        boolean isDeduction = command.amount().isNegative();
        Money absAmount = command.amount().abs();

        if (isDeduction && !customerAccount.isBalanceSufficient(absAmount)) {
            throw new InsufficientBalanceException(customerAccount.availableBalance(), absAmount);
        }

        TransactionRecordWithoutId debitChange;
        TransactionRecordWithoutId creditChange;

        if (isDeduction) {
            debitChange = customerWallet.debit(absAmount);
            creditChange = adjustmentSubLedger.credit(absAmount);
        } else {
            debitChange = adjustmentSubLedger.debit(absAmount);
            creditChange = customerWallet.credit(absAmount);
        }

        accountRepository.save(systemAccount);
        accountRepository.save(customerAccount);

        Instant recordedAt = clock.instant();
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.ADJUSTMENT)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(absAmount)
                .customerId(command.customerId())
                .operatorId(command.operatorId())
                .sourceType(null)
                .sourceId(null)
                .recordedAt(recordedAt)
                .idempotencyKey(command.idempotencyKey())
                .reason(command.reason())
                .records(List.of(
                        debitChange.toTransaction(uuidGenerator.generate()),
                        creditChange.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(transaction);

        return new AdjustmentResult(transactionId, recordedAt);
    }
}
