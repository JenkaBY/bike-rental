package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.domain.model.*;
import com.github.jenkaby.bikerental.tariff.domain.model.vo.PricingTypeInfo;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingTypeResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(uses = {MoneyMapper.class})
public interface TariffV2QueryMapper {

    default TariffV2Response toResponse(TariffV2 tariff) {
        return new TariffV2Response(
                tariff.getId(),
                tariff.getName(),
                tariff.getDescription(),
                tariff.getEquipmentType(),
                tariff.getPricingType(),
                toParamsDto(tariff),
                tariff.getValidFrom(),
                tariff.getValidTo(),
                tariff.getVersion(),
                tariff.getStatus()
        );
    }

    private static PricingParams toParamsDto(TariffV2 tariff) {
        return switch (tariff) {
            case DegressiveHourlyTariffV2 t -> new PricingParams(
                    t.getFirstHourPrice().amount(),
                    t.getHourlyDiscount().amount(),
                    t.getMinimumHourlyPrice().amount(),
                    null, null, null, null,
                    t.getMinimumDuration().toMinutesPart(),
                    t.getMinimumDurationSurcharge().amount(),
                    null
            );
            case FlatHourlyTariffV2 t -> new PricingParams(
                    null, null, null,
                    t.getHourlyPrice().amount(),
                    null, null, null,
                    t.getMinimumDuration().toMinutesPart(),
                    t.getMinimumDurationSurcharge().amount(),
                    null
            );
            case DailyTariffV2 t -> new PricingParams(
                    null, null, null, null,
                    t.getDailyPrice().amount(),
                    t.getOvertimeHourlyPrice().amount(),
                    null, null, null, null
            );
            case FlatFeeTariffV2 t -> new PricingParams(
                    null, null, null, null, null, null,
                    t.getIssuanceFee().amount(),
                    null, null, null
            );
            case SpecialTariffV2 t -> new PricingParams(
                    null, null, null, null, null, null, null, null, null,
                    BigDecimal.ZERO
            );
        };
    }

    PricingTypeResponse toResponse(PricingTypeInfo info);
}
