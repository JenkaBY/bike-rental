package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.application.usecase.ReleaseHoldUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseHoldService implements ReleaseHoldUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public HoldResult execute(ReleaseHoldCommand command) {
        var idempotencyKey = new IdempotencyKey(
                uuidGenerator.generateNameBased("%s_%s".formatted(TransactionType.RELEASE.name(), command.rentalRef().id()))
        );
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            var t = existing.get();
            return new HoldResult(new TransactionRef(t.getId()), t.getRecordedAt());
        }

        Optional<Transaction> holdOptional = transactionRepository.findByRentalRefAndType(command.rentalRef(), TransactionType.HOLD);
        if (holdOptional.isEmpty()) {
            return new HoldResult(null, clock.instant());
        }
        var holdTxN = holdOptional.get();
        var customerRef = new CustomerRef(holdTxN.getCustomerId());

        var now = clock.instant();
        var customerAccount = accountRepository.findByCustomerId(customerRef)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, customerRef.id()));

        Money releaseHold = holdTxN.getAmount();
        var debitChange = customerAccount.getWallet().credit(releaseHold);
        var creditChange = customerAccount.getOnHold().debit(releaseHold);

        accountRepository.save(customerAccount);
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.RELEASE)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(releaseHold)
                .customerId(customerRef.id())
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
