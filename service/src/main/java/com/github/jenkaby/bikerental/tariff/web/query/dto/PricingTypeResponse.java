package com.github.jenkaby.bikerental.tariff.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pricing type with localized title and description")
public record PricingTypeResponse(
        @Schema(description = "Enum slug", example = "DEGRESSIVE_HOURLY") String slug,
        @Schema(description = "Localized title") String title,
        @Schema(description = "Localized description") String description
) {
}
