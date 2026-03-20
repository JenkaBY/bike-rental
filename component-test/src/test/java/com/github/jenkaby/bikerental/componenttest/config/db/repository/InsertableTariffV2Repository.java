package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffV2JpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableTariffV2Repository extends InsertableRepositoryImpl<TariffV2JpaEntity, Long> {

    public InsertableTariffV2Repository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
