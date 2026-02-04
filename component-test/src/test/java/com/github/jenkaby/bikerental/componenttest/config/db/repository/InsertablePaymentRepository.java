package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InsertablePaymentRepository extends InsertableRepositoryImpl<PaymentJpaEntity, Long> {

    public InsertablePaymentRepository(EntityManager entityManager) {
        super(entityManager);
    }
}
