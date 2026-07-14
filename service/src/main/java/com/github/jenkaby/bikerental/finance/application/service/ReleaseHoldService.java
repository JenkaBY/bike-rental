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
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
    private final RentalHoldBalanceCalculator holdBalanceCalculator;
    private final UuidGenerator uuidGenerator;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public HoldResult execute(ReleaseHoldCommand command) {
        var actualRentalRef = command.rentalRef();
        log.info("Releasing hold for rental {} version {}", actualRentalRef.id(), actualRentalRef.rentalVersion());
        var idempotencyKey = getIdempotencyKey(command);
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            var t = existing.get();
            log.info("Hold already released for rental {} version {}, returning existing transaction {}",
                    actualRentalRef.id(), actualRentalRef.rentalVersion(), t.getId());
            return new HoldResult(new TransactionRef(t.getId()), t.getRecordedAt());
        }

        var rentalRef = actualRentalRef.toRentalRef();
        Money netHold = holdBalanceCalculator.netActiveHold(rentalRef);
        if (netHold.isNegativeOrZero()) {
            log.debug("No active hold for rental {} version {}, nothing to release", actualRentalRef.id(), actualRentalRef.rentalVersion());
            return new HoldResult(null, timeProvider.nowInstant());
        }

        Optional<Transaction> holdOptional = transactionRepository.findByRentalRefAndType(rentalRef, TransactionType.HOLD);
        var customerRef = new CustomerRef(holdOptional.orElseThrow(() ->
                new ResourceNotFoundException(Transaction.class, actualRentalRef.id().toString())).getCustomerId());

        var now = timeProvider.nowInstant();
        var customerAccount = accountRepository.findByCustomerId(customerRef)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, customerRef.id()));

        log.info("Releasing net hold amount {} for rental {} version {}", netHold, actualRentalRef.id(), actualRentalRef.rentalVersion());
        var debitChange = customerAccount.getWallet().credit(netHold);
        var creditChange = customerAccount.getOnHold().debit(netHold);

        accountRepository.save(customerAccount);
        UUID transactionId = uuidGenerator.generate();

        var transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.RELEASE)
                .paymentMethod(PaymentMethod.INTERNAL_TRANSFER)
                .amount(netHold)
                .customerId(customerRef.id())
                .operatorId(command.operatorId())
                .sourceType(TransactionSourceType.RENTAL)
                .sourceId(String.valueOf(actualRentalRef.id()))
                .recordedAt(now)
                .idempotencyKey(idempotencyKey)
                .reason(null)
                .records(List.of(
                        debitChange.toTransaction(uuidGenerator.generate()),
                        creditChange.toTransaction(uuidGenerator.generate())
                ))
                .build();

        transactionRepository.save(transaction);
        log.info("Released net hold amount {} in transaction {} for rental {} version {}",
                netHold, transaction.getId(), actualRentalRef.id(), actualRentalRef.rentalVersion());
        return new HoldResult(new TransactionRef(transactionId), now);
    }

    private @NonNull IdempotencyKey getIdempotencyKey(ReleaseHoldCommand command) {
        return new IdempotencyKey(
                uuidGenerator.generateNameBased("%s_%s_%s".formatted(
                        TransactionType.RELEASE.name(),
                        command.rentalRef().id(),
                        command.rentalRef().rentalVersion()))
        );
    }
}
