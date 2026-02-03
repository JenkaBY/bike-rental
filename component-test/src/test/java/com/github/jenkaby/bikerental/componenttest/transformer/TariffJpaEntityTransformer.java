package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

import static com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper.*;

public class TariffJpaEntityTransformer {

    @DataTableType
    public TariffJpaEntity transform(Map<String, String> entry) {
        TariffJpaEntity entity = new TariffJpaEntity();
        entity.setId(Optional.ofNullable(entry.get("id")).map(Long::valueOf).orElse(null));
        entity.setName(entry.get("name"));
        entity.setDescription(entry.get("description"));
        entity.setEquipmentTypeSlug(entry.get("equipmentType"));

        entity.setBasePrice(toBigDecimal(entry, "basePrice"));
        entity.setHalfHourPrice(toBigDecimal(entry, "halfHourPrice"));
        entity.setHourPrice(toBigDecimal(entry, "hourPrice"));
        entity.setDayPrice(toBigDecimal(entry, "dayPrice"));
        entity.setHourDiscountedPrice(toBigDecimal(entry, "discountedPrice"));

        entity.setValidFrom(toLocalDate(entry, "validFrom"));
        entity.setValidTo(toLocalDate(entry, "validTo"));

        entity.setStatus(entry.get("status"));
        entity.setCreatedAt(toInstant(entry, "createdAt"));
        entity.setCreatedAt(toInstant(entry, "updatedAt"));
        return entity;
    }

}
