package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableRentalRepository extends InsertableRepositoryImpl<RentalJpaEntity, Long> {

    public InsertableRentalRepository(EntityManager entityManager) {
        super(entityManager);
    }
}
