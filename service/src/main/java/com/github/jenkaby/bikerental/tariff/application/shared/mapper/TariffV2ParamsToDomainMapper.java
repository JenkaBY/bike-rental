package com.github.jenkaby.bikerental.tariff.application.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.domain.model.*;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class TariffV2ParamsToDomainMapper {

    public TariffV2 buildTariff(Long id, String name, String description, String equipmentTypeSlug,
                                PricingType pricingType, String version,
                                LocalDate validFrom, LocalDate validTo,
                                TariffV2Status status, Map<String, Object> params) {
        return switch (pricingType) {
            case DEGRESSIVE_HOURLY -> new DegressiveHourlyTariffV2(
                    id, name, description, equipmentTypeSlug, version, validFrom, validTo, status,
                    toMoney(params, TariffV2FieldNames.FIRST_HOUR_PRICE),
                    toMoney(params, TariffV2FieldNames.HOURLY_DISCOUNT),
                    toMoney(params, TariffV2FieldNames.MINIMUM_HOURLY_PRICE),
                    toInteger(params, TariffV2FieldNames.MINIMUM_DURATION_MINUTES),
                    toMoney(params, TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE));
            case FLAT_HOURLY -> new FlatHourlyTariffV2(
                    id, name, description, equipmentTypeSlug, version, validFrom, validTo, status,
                    toMoney(params, TariffV2FieldNames.HOURLY_PRICE),
                    toInteger(params, TariffV2FieldNames.MINIMUM_DURATION_MINUTES),
                    toMoney(params, TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE));
            case DAILY -> new DailyTariffV2(
                    id, name, description, equipmentTypeSlug, version, validFrom, validTo, status,
                    toMoney(params, TariffV2FieldNames.DAILY_PRICE),
                    toMoney(params, TariffV2FieldNames.OVERTIME_HOURLY_PRICE));
            case FLAT_FEE -> new FlatFeeTariffV2(
                    id, name, description, equipmentTypeSlug, version, validFrom, validTo, status,
                    toMoney(params, TariffV2FieldNames.ISSUANCE_FEE));
            case SPECIAL -> new SpecialTariffV2(
                    id, name, description, equipmentTypeSlug, version, validFrom, validTo, status);
        };
    }

    public static Money toMoney(Map<String, Object> params, String key) {
        Object v = params.get(key);
        return switch (v) {
            case BigDecimal bd -> Money.of(bd);
            case Number n -> Money.of(BigDecimal.valueOf(n.doubleValue()));
            case String s -> Money.of(s);
            case null, default -> null;
        };
    }

    public static Integer toInteger(Map<String, Object> params, String key) {
        Object v = params.get(key);
        return switch (v) {
            case Integer i -> i;
            case Number n -> n.intValue();
            case String s -> Integer.valueOf(s);
            case null, default -> null;
        };
    }
}

