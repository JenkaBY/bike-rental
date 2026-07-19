package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionDetailsUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class GetTransactionDetailsService implements GetTransactionDetailsUseCase {

    private final TransactionRepository transactionRepository;
    private final TransactionDetailsAssembler transactionDetailsAssembler;

    @Override
    @Transactional(readOnly = true)
    public TransactionDetails execute(UUID transactionId) {
        log.debug("Fetching transaction details id={}", transactionId);
        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(Transaction.class, transactionId));

        return transactionDetailsAssembler
                .assemble(CustomerRef.of(transaction.getCustomerId()), List.of(transaction))
                .get(transaction.getId());
    }
}
