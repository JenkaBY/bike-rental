package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.mapper.TransactionMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class GetTransactionHistoryService implements GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Page<TransactionDto> execute(UUID customerId, TransactionHistoryFilter filter, PageRequest pageRequest) {
        var customerRef = CustomerRef.of(customerId);
        accountRepository.findByCustomerId(customerRef)
                .orElseThrow(() -> new ResourceNotFoundException(CustomerAccount.class, customerId));

        Page<Transaction> page = transactionRepository.findTransactionHistory(customerRef, filter, pageRequest);

        List<TransactionDto> entries = page.items().stream()
                .map(transactionMapper::toEntry)
                .toList();

        return new Page<>(entries, page.totalItems(), pageRequest);
    }
}
