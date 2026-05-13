package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId);

    Optional<Transaction> findByIdempotencyKey(IdempotencyKey idempotencyKey);

    Optional<Transaction> findByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    List<Transaction> findAllByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    boolean existsByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    Page<Transaction> findTransactionHistory(CustomerRef customerId,
                                             TransactionHistoryFilter filter,
                                             PageRequest pageRequest);
}
