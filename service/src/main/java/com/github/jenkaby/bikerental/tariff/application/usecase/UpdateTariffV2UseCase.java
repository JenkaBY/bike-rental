package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

import java.time.LocalDate;
import java.util.Map;

public interface UpdateTariffV2UseCase {
    TariffV2 execute(UpdateTariffV2Command command);

    record UpdateTariffV2Command(
            Long id,
            String name,
            String description,
            String equipmentTypeSlug,
            PricingType pricingType,
            Map<String, Object> params,
            LocalDate validFrom,
            LocalDate validTo
    ) {
    }
}
