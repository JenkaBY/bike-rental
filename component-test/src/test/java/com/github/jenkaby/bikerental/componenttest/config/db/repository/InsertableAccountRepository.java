package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableAccountRepository extends InsertableRepositoryImpl<AccountJpaEntity, Long> {

    public InsertableAccountRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
