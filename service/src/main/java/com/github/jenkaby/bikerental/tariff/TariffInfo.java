package com.github.jenkaby.bikerental.tariff;

import java.math.BigDecimal;
import java.time.LocalDate;


public record TariffInfo(
        Long id,
        String name,
        String equipmentTypeSlug,
        BigDecimal basePrice,
        BigDecimal hourPrice,  // Can be zero if not applicable
        BigDecimal dayPrice,   // Can be zero if not applicable
        LocalDate validFrom,
        LocalDate validTo,
        boolean active
) {
}
