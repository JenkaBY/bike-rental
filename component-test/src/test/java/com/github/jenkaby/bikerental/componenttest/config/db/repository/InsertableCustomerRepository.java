package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class InsertableCustomerRepository extends InsertableRepositoryImpl<CustomerJpaEntity, UUID> {

    public InsertableCustomerRepository(EntityManager entityManager) {
        super(entityManager);
    }
}
