package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffV2JpaEntity;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper.toInstant;
import static com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper.toLocalDate;

public class TariffV2JpaEntityTransformer {

    @DataTableType
    public TariffV2JpaEntity transform(Map<String, String> entry) {
        TariffV2JpaEntity entity = new TariffV2JpaEntity();
        entity.setId(Optional.ofNullable(entry.get("id")).map(Long::valueOf).orElse(null));
        entity.setName(entry.get("name"));
        entity.setDescription(entry.get("description"));
        entity.setEquipmentType(entry.get("equipmentType"));
        entity.setPricingType(entry.get("pricingType"));

        entity.setValidFrom(toLocalDate(entry, "validFrom"));
        entity.setValidTo(toLocalDate(entry, "validTo"));

        entity.setStatus(entry.get("status"));
        entity.setVersion(entry.getOrDefault("version", "v2"));
        entity.setCreatedAt(Optional.ofNullable(toInstant(entry, "createdAt")).orElse(Instant.now()));
        entity.setUpdatedAt(toInstant(entry, "updatedAt"));

        return entity;
    }


}


