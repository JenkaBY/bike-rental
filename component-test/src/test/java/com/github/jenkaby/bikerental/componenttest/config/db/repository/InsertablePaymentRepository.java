package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertablePaymentRepository extends InsertableRepositoryImpl<PaymentJpaEntity, Long> {

    public InsertablePaymentRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
