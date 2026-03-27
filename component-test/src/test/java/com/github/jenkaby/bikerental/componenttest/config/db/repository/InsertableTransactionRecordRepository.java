package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableTransactionRecordRepository extends InsertableRepositoryImpl<TransactionRecordJpaEntity, Long> {

    public InsertableTransactionRecordRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
