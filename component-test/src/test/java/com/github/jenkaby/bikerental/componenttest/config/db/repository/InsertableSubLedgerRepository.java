package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableSubLedgerRepository extends InsertableRepositoryImpl<SubLedgerJpaEntity, Long> {

    public InsertableSubLedgerRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
