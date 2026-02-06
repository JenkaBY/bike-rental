package com.github.jenkaby.bikerental.tariff.application.mapper;

import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffUseCase.CreateTariffCommand;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase.UpdateTariffCommand;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TariffCommandToDomainMapper {

    @Mapping(target = "id", ignore = true)
    Tariff toTariff(CreateTariffCommand command);

    Tariff toTariff(UpdateTariffCommand command);
}
