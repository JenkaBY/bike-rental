package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
class TransactionDetailsAssembler {

    private final TransactionRepository transactionRepository;

    Map<UUID, TransactionDetails> assemble(CustomerRef customerRef, List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return Map.of();
        }
        List<Transaction> ascending = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getRecordedAt))
                .toList();
        Instant oldest = ascending.getFirst().getRecordedAt();

        Map<LedgerType, Money> seeds = transactionRepository.findLatestLedgerBalancesBefore(customerRef, oldest);
        Money wallet = seeds.getOrDefault(LedgerType.CUSTOMER_WALLET, Money.zero());
        Money hold = seeds.getOrDefault(LedgerType.CUSTOMER_HOLD, Money.zero());

        Map<UUID, TransactionDetails> detailsByTransaction = new HashMap<>();
        for (Transaction transaction : ascending) {
            wallet = transaction.runningBalanceFor(LedgerType.CUSTOMER_WALLET).orElse(wallet);
            hold = transaction.runningBalanceFor(LedgerType.CUSTOMER_HOLD).orElse(hold);
            detailsByTransaction.put(transaction.getId(), new TransactionDetails(transaction, wallet, hold));
        }
        return detailsByTransaction;
    }
}
