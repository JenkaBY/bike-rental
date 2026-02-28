package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Tariff details")
public record TariffResponse(
        @Schema(description = "Tariff ID", example = "1") Long id,
        @Schema(description = "Tariff name", example = "Standard Bike Rental") String name,
        @Schema(description = "Description") String description,
        @Schema(description = "Equipment type slug", example = "bike") String equipmentTypeSlug,
        @Schema(description = "Base price", example = "50.00") BigDecimal basePrice,
        @Schema(description = "Price per 30 minutes", example = "100.00") BigDecimal halfHourPrice,
        @Schema(description = "Price per hour", example = "180.00") BigDecimal hourPrice,
        @Schema(description = "Price per day", example = "900.00") BigDecimal dayPrice,
        @Schema(description = "Discounted price per hour", example = "150.00") BigDecimal hourDiscountedPrice,
        @Schema(description = "Valid from date", example = "2026-01-01") LocalDate validFrom,
        @Schema(description = "Valid to date (null = indefinite)", example = "2026-12-31") LocalDate validTo,
        @Schema(description = "Tariff status") TariffStatus status
) {
}
