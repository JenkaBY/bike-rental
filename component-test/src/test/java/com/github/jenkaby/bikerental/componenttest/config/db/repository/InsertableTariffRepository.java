package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableTariffRepository extends InsertableRepositoryImpl<TariffJpaEntity, Long> {

    public InsertableTariffRepository(EntityManager entityManager) {
        super(entityManager);
    }
}
