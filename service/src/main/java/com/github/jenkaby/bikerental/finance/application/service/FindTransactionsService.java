package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.FindTransactionsUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class FindTransactionsService implements FindTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> execute(TransactionFilter filter, PageRequest pageRequest) {
        log.debug("Finding transactions filter={} page={}", filter, pageRequest);
        var page = transactionRepository.findTransactions(filter, pageRequest);
        log.debug("Found {} transactions (total={})", page.items().size(), page.totalItems());
        return page;
    }
}
