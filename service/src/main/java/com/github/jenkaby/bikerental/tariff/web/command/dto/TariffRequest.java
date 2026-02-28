package com.github.jenkaby.bikerental.tariff.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request body for creating or updating a tariff")
public record TariffRequest(

        @Schema(description = "Tariff name", example = "Standard Bike Rental")
        @NotBlank(message = "Tariff name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @Schema(description = "Description")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Equipment type slug this tariff applies to", example = "bike")
        @Slug
        String equipmentTypeSlug,

        @Schema(description = "Base price (entry fee)", example = "50.00")
        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Base price must have max 2 decimal places")
        BigDecimal basePrice,

        @Schema(description = "Price per 30 minutes", example = "100.00")
        @NotNull(message = "Half hour price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Half hour price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Half hour price must have max 2 decimal places")
        BigDecimal halfHourPrice,

        @Schema(description = "Price per hour", example = "180.00")
        @NotNull(message = "Hour price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hour price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Hour price must have max 2 decimal places")
        BigDecimal hourPrice,

        @Schema(description = "Price per day", example = "900.00")
        @NotNull(message = "Day price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Day price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Day price must have max 2 decimal places")
        BigDecimal dayPrice,

        @Schema(description = "Discounted price per hour (e.g. for bulk hours)", example = "150.00")
        @NotNull(message = "Hour discounted price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hour discounted price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Hour discounted price must have max 2 decimal places")
        BigDecimal hourDiscountedPrice,

        @Schema(description = "Date from which the tariff is valid", example = "2026-01-01")
        @NotNull(message = "Valid from date is required")
        LocalDate validFrom,

        @Schema(description = "Date until which the tariff is valid (null = indefinite)", example = "2026-12-31")
        LocalDate validTo,

        @Schema(description = "Tariff status")
        @NotNull(message = "Status is required")
        TariffStatus status
) {
}
