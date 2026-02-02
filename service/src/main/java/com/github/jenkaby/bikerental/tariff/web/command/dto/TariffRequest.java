package com.github.jenkaby.bikerental.tariff.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TariffRequest(

        @NotBlank(message = "Tariff name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Slug
        String equipmentTypeSlug,

        @NotNull(message = "Period is required")
        TariffPeriod period,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Base price must have max 2 decimal places")
        BigDecimal basePrice,

        @NotNull(message = "Half hour price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Half hour price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Half hour price must have max 2 decimal places")
        BigDecimal halfHourPrice,

        @NotNull(message = "Hour price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hour price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Hour price must have max 2 decimal places")
        BigDecimal hourPrice,

        @NotNull(message = "Day price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Day price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Day price must have max 2 decimal places")
        BigDecimal dayPrice,

        @NotNull(message = "Hour discounted price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hour discounted price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Hour discounted price must have max 2 decimal places")
        BigDecimal hourDiscountedPrice,

        @NotNull(message = "Valid from date is required")
        LocalDate validFrom,

        LocalDate validTo,

        @NotNull(message = "Status is required")
        TariffStatus status
) {
}
