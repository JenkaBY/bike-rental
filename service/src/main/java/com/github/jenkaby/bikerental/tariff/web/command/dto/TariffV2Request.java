package com.github.jenkaby.bikerental.tariff.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request body for creating or updating a V2 tariff")
public record TariffV2Request(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Slug String equipmentTypeSlug,
        @NotNull PricingType pricingType,
        @Valid @NotNull PricingParams params,
        @NotNull LocalDate validFrom,
        LocalDate validTo
) {
}
