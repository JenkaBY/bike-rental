package com.github.jenkaby.bikerental.tariff.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Pricing type with localized title and description")
public record PricingTypeResponse(
        @Schema(description = "Enum slug", example = "DEGRESSIVE_HOURLY") @NotNull String slug,
        @Schema(description = "Localized title") @NotNull String title,
        @Schema(description = "Localized description") @NotNull String description
) {
}
