package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableRentalEquipmentRepository extends InsertableRepositoryImpl<RentalEquipmentJpaEntity, Long> {

    public InsertableRentalEquipmentRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
