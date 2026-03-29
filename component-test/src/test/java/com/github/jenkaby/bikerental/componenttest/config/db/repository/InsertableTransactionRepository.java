package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableTransactionRepository extends InsertableRepositoryImpl<TransactionJpaEntity, Long> {

    public InsertableTransactionRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
