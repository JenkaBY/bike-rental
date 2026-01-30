package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentTypeJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableEquipmentTypeRepository extends InsertableRepositoryImpl<EquipmentTypeJpaEntity, Long> {

    public InsertableEquipmentTypeRepository(EntityManager entityManager) {
        super(entityManager);
    }
}
