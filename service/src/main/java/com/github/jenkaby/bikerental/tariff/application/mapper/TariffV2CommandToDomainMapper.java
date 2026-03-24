package com.github.jenkaby.bikerental.tariff.application.mapper;

import com.github.jenkaby.bikerental.tariff.application.shared.mapper.TariffV2ParamsToDomainMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2Status;
import org.springframework.stereotype.Component;

import java.util.Map;

// FIXME use mapper here
@Component
public class TariffV2CommandToDomainMapper {

    private static final String VERSION_DEFAULT = "v2";

    private final TariffV2ParamsToDomainMapper paramsToDomainMapper;

    public TariffV2CommandToDomainMapper(TariffV2ParamsToDomainMapper paramsToDomainMapper) {
        this.paramsToDomainMapper = paramsToDomainMapper;
    }

    public TariffV2 toTariffV2(CreateTariffV2UseCase.CreateTariffV2Command command) {
        Map<String, Object> params = command.params() != null ? command.params() : Map.of();
        return paramsToDomainMapper.buildTariff(
                null,
                command.name(),
                command.description(),
                command.equipmentTypeSlug(),
                command.pricingType(),
                VERSION_DEFAULT,
                command.validFrom(),
                command.validTo(),
                TariffV2Status.INACTIVE,
                params
        );
    }

    public TariffV2 toTariffV2(UpdateTariffV2UseCase.UpdateTariffV2Command command, TariffV2Status status) {
        Map<String, Object> params = command.params() != null ? command.params() : Map.of();
        return paramsToDomainMapper.buildTariff(
                command.id(),
                command.name(),
                command.description(),
                command.equipmentTypeSlug(),
                command.pricingType(),
                VERSION_DEFAULT,
                command.validFrom(),
                command.validTo(),
                status,
                params
        );
    }
}
