package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableEquipmentRepository extends InsertableRepositoryImpl<EquipmentJpaEntity, Long> {

    public InsertableEquipmentRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
