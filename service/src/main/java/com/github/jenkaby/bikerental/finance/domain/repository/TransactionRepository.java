package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.RentalId;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdempotencyKeyAndCustomerId(IdempotencyKey idempotencyKey, CustomerRef customerId);

    Optional<Transaction> findByIdempotencyKey(IdempotencyKey idempotencyKey);

    Optional<Transaction> findByRentalRefAndType(RentalId rentalRef, TransactionType type);

    List<Transaction> findAllByRentalRefAndTypes(RentalId rentalRef, Set<TransactionType> types);

    Page<Transaction> findTransactionHistory(CustomerRef customerId,
                                             TransactionHistoryFilter filter,
                                             PageRequest pageRequest);
}
