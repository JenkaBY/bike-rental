package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class GetTransactionHistoryService implements GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionDetailsAssembler transactionDetailsAssembler;

    @Override
    public Page<TransactionDetails> execute(UUID customerId, TransactionHistoryFilter filter, PageRequest pageRequest) {
        log.debug("Fetching transaction history for customer={} filter={} page={}", customerId, filter, pageRequest);
        var customerRef = CustomerRef.of(customerId);
        accountRepository.findByCustomerId(customerRef)
                .orElseThrow(() -> new ResourceNotFoundException(CustomerAccount.class, customerId));

        Page<Transaction> page = transactionRepository.findTransactionHistory(customerRef, filter, pageRequest);
        List<Transaction> items = page.items();

        var detailsByTransaction = transactionDetailsAssembler.assemble(customerRef, items);
        List<TransactionDetails> entries = items.stream()
                .map(transaction -> detailsByTransaction.get(transaction.getId()))
                .toList();
        log.debug("Found {} transactions (total={}) for customer={}", entries.size(), page.totalItems(), customerId);

        return new Page<>(entries, page.totalItems(), pageRequest);
    }
}
