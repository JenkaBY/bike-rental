package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class WrapperTransactionJpaRepository {

    private final TransactionJpaRepository delegate;

    @Transactional(readOnly = true)
    public List<TransactionJpaEntity> findAllInitialized() {
        return delegate.findAll().stream()
                .peek(e -> e.getRecords().forEach(TransactionRecordJpaEntity::getId))
                .toList();
    }

}
