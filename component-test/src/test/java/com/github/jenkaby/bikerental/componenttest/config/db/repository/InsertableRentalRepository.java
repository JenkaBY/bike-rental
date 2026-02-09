package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableRentalRepository extends InsertableRepositoryImpl<RentalJpaEntity, Long> {

    public InsertableRentalRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
