package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementSignatureJpaEntity;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.projection.AgreementSignatureSummaryProjection;
import org.mapstruct.Mapper;

@Mapper
public interface AgreementSignatureJpaMapper {

    AgreementSignatureJpaEntity toEntity(AgreementSignature signature);

    AgreementSignature toDomain(AgreementSignatureJpaEntity entity);

    AgreementSignatureSummary toSummary(AgreementSignatureSummaryProjection projection);
}
