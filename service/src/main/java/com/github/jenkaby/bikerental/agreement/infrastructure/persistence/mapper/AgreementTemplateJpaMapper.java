package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import org.mapstruct.Mapper;

@Mapper
public interface AgreementTemplateJpaMapper {

    AgreementTemplateJpaEntity toEntity(AgreementTemplate template);

    AgreementTemplate toDomain(AgreementTemplateJpaEntity entity);
}
