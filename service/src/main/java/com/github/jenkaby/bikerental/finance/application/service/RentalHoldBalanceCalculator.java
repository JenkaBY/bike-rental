package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RentalHoldBalanceCalculator {

    private final TransactionRepository transactionRepository;

    public Money netActiveHold(RentalRef rentalRef) {
        var sumByTxnType = getSumByTxnType(rentalRef);
        var held = sumByTxnType.getOrDefault(TransactionType.HOLD, Money.zero());
        var released = sumByTxnType.getOrDefault(TransactionType.RELEASE, Money.zero());
        var captured = sumByTxnType.getOrDefault(TransactionType.CAPTURE, Money.zero());
        return held.subtract(released)
                .subtract(captured);
    }

    private @NonNull Map<TransactionType, Money> getSumByTxnType(RentalRef rentalRef) {
        return transactionRepository.findAllByRentalRefAndTypes(rentalRef,
                        Set.of(TransactionType.HOLD, TransactionType.RELEASE, TransactionType.CAPTURE)).stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        Collectors.reducing(
                                Money.zero(),
                                Transaction::getAmount,
                                Money::add
                        ))
                );
    }
}