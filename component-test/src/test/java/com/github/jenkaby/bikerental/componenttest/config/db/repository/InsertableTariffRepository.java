package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableTariffRepository extends InsertableRepositoryImpl<TariffJpaEntity, Long> {

    public InsertableTariffRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
