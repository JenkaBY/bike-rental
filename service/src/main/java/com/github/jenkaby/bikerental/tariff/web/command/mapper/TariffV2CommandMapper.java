package com.github.jenkaby.bikerental.tariff.web.command.mapper;

import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffV2Request;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.Map;

@Mapper
public interface TariffV2CommandMapper {

    @Mapping(target = "params", source = "params", qualifiedByName = "paramsToMap")
    CreateTariffV2UseCase.CreateTariffV2Command toCreateCommand(TariffV2Request request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "params", source = "request.params", qualifiedByName = "paramsToMap")
    UpdateTariffV2UseCase.UpdateTariffV2Command toUpdateCommand(Long id, TariffV2Request request);

    @Named("paramsToMap")
    default Map<String, Object> paramsToMap(PricingParams dto) {
        if (dto == null) {
            return Map.of();
        }
        Map<String, Object> m = new HashMap<>();
        if (dto.firstHourPrice() != null) m.put(TariffV2FieldNames.FIRST_HOUR_PRICE, dto.firstHourPrice());
        if (dto.hourlyDiscount() != null) m.put(TariffV2FieldNames.HOURLY_DISCOUNT, dto.hourlyDiscount());
        if (dto.minimumHourlyPrice() != null) m.put(TariffV2FieldNames.MINIMUM_HOURLY_PRICE, dto.minimumHourlyPrice());
        if (dto.hourlyPrice() != null) m.put(TariffV2FieldNames.HOURLY_PRICE, dto.hourlyPrice());
        if (dto.dailyPrice() != null) m.put(TariffV2FieldNames.DAILY_PRICE, dto.dailyPrice());
        if (dto.overtimeHourlyPrice() != null)
            m.put(TariffV2FieldNames.OVERTIME_HOURLY_PRICE, dto.overtimeHourlyPrice());
        if (dto.issuanceFee() != null) m.put(TariffV2FieldNames.ISSUANCE_FEE, dto.issuanceFee());
        if (dto.minimumDurationMinutes() != null)
            m.put(TariffV2FieldNames.MINIMUM_DURATION_MINUTES, dto.minimumDurationMinutes());
        if (dto.minimumDurationSurcharge() != null)
            m.put(TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, dto.minimumDurationSurcharge());
        if (dto.price() != null) m.put(TariffV2FieldNames.PRICE, dto.price());
        return m;
    }
}
