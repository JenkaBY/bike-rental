package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableEquipmentStatusRepository extends InsertableRepositoryImpl<EquipmentStatusJpaEntity, Long> {

    public InsertableEquipmentStatusRepository(EntityManager entityManager) {
        super(entityManager);
    }
}
