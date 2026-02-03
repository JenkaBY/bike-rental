package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MoneyMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TariffJpaMapper {

    Tariff toDomain(TariffJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TariffJpaEntity toEntity(Tariff tariff);
}
