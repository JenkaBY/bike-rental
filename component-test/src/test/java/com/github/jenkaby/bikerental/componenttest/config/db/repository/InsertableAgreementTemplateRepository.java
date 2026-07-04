package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InsertableAgreementTemplateRepository extends InsertableRepositoryImpl<AgreementTemplateJpaEntity, Long> {

    public InsertableAgreementTemplateRepository(JpaEntityInserter entityInserter) {
        super(entityInserter);
    }
}
