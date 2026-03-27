package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;

    TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Transaction save(Transaction transaction) {
        var entity = mapper.toEntity(transaction);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey.id())
                .map(mapper::toDomain);
    }
}
