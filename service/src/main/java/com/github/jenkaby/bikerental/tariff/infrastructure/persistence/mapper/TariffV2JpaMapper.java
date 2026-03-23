package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.shared.mapper.TariffV2ParamsToDomainMapper;
import com.github.jenkaby.bikerental.tariff.domain.model.*;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffV2JpaEntity;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TariffV2JpaMapper {

    private static final String VERSION_DEFAULT = "v2";

    private final TariffV2ParamsToDomainMapper paramsToDomainMapper;

    public TariffV2JpaMapper(TariffV2ParamsToDomainMapper paramsToDomainMapper) {
        this.paramsToDomainMapper = paramsToDomainMapper;
    }

    public TariffV2 toDomain(TariffV2JpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> params = entity.getParams() != null ? entity.getParams() : Map.of();
        return paramsToDomainMapper.buildTariff(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getEquipmentType(),
                PricingType.valueOf(entity.getPricingType()),
                entity.getVersion() != null ? entity.getVersion() : VERSION_DEFAULT,
                entity.getValidFrom(),
                entity.getValidTo(),
                TariffV2Status.valueOf(entity.getStatus()),
                params
        );
    }

    public TariffV2JpaEntity toEntity(TariffV2 tariff) {
        if (tariff == null) {
            return null;
        }
        var entity = new TariffV2JpaEntity();
        entity.setId(tariff.getId());
        entity.setName(tariff.getName());
        entity.setDescription(tariff.getDescription());
        entity.setEquipmentType(tariff.getEquipmentType());
        entity.setPricingType(tariff.getPricingType().name());
        entity.setVersion(tariff.getVersion() != null ? tariff.getVersion() : VERSION_DEFAULT);
        entity.setValidFrom(tariff.getValidFrom());
        entity.setValidTo(tariff.getValidTo());
        entity.setStatus(tariff.getStatus().name());
        entity.setParams(toParamsMap(tariff));
        return entity;
    }

    private Map<String, Object> toParamsMap(TariffV2 tariff) {
        return switch (tariff) {
            case DegressiveHourlyTariffV2 t -> {
                Map<String, Object> m = new HashMap<>();
                putMoney(m, TariffV2FieldNames.FIRST_HOUR_PRICE, t.getFirstHourPrice());
                putMoney(m, TariffV2FieldNames.HOURLY_DISCOUNT, t.getHourlyDiscount());
                putMoney(m, TariffV2FieldNames.MINIMUM_HOURLY_PRICE, t.getMinimumHourlyPrice());
                putInt(m, TariffV2FieldNames.MINIMUM_DURATION_MINUTES, t.getMinimumDuration().toMinutesPart());
                putMoney(m, TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, t.getMinimumDurationSurcharge());
                yield m;
            }
            case FlatHourlyTariffV2 t -> {
                Map<String, Object> m = new HashMap<>();
                putMoney(m, TariffV2FieldNames.HOURLY_PRICE, t.getHourlyPrice());
                putInt(m, TariffV2FieldNames.MINIMUM_DURATION_MINUTES, t.getMinimumDuration().toMinutesPart());
                putMoney(m, TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, t.getMinimumDurationSurcharge());
                yield m;
            }
            case DailyTariffV2 t -> {
                Map<String, Object> m = new HashMap<>();
                putMoney(m, TariffV2FieldNames.DAILY_PRICE, t.getDailyPrice());
                putMoney(m, TariffV2FieldNames.OVERTIME_HOURLY_PRICE, t.getOvertimeHourlyPrice());
                yield m;
            }
            case FlatFeeTariffV2 t -> {
                Map<String, Object> m = new HashMap<>();
                putMoney(m, TariffV2FieldNames.ISSUANCE_FEE, t.getIssuanceFee());
                yield m;
            }
            case SpecialTariffV2 t -> {
                Map<String, Object> m = new HashMap<>();
                putMoney(m, TariffV2FieldNames.PRICE, t.getPrice());
                yield m;
            }
        };
    }

    private static void putMoney(Map<String, Object> m, String key, Money value) {
        if (value != null) {
            m.put(key, value.amount());
        }
    }

    private static void putInt(Map<String, Object> m, String key, Integer value) {
        if (value != null) {
            m.put(key, value);
        }
    }
}
