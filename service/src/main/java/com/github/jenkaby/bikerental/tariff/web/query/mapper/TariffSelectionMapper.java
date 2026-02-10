package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.tariff.TariffInfo;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TariffSelectionMapper {

    @Mapping(target = "equipmentType", source = "tariffInfo.equipmentTypeSlug")
    @Mapping(target = "price", expression = "java(getPriceForPeriod(tariffInfo, period))")
    TariffSelectionResponse toSelectionResponse(TariffInfo tariffInfo, TariffPeriod period);

    default java.math.BigDecimal getPriceForPeriod(TariffInfo tariffInfo, TariffPeriod period) {
        return switch (period) {
            case HALF_HOUR -> tariffInfo.halfHourPrice();
            case HOUR -> tariffInfo.hourPrice();
            case DAY -> tariffInfo.dayPrice();
        };
    }
}
