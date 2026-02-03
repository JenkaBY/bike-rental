package com.github.jenkaby.bikerental.tariff.web.command.mapper;

import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffUseCase.CreateTariffCommand;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase.UpdateTariffCommand;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TariffCommandMapper {

    CreateTariffCommand toCreateCommand(TariffRequest request);

    UpdateTariffCommand toUpdateCommand(Long id, TariffRequest request);
}
