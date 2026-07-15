package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.shared.web.support.PercentValue;
import com.github.jenkaby.bikerental.tariff.web.query.validation.SpecialTariffConsistency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Request for rental cost calculation V2 (per-equipment return timestamps)")
@SpecialTariffConsistency
public record CostCalculationV2Request(
        @NotEmpty List<@NotNull @Valid EquipmentItemRequest> equipments,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
        @NotNull @Positive Integer plannedDurationMinutes,
        @PercentValue Integer discountPercent,
        @Positive Long specialTariffId,
        @DecimalMin("0") BigDecimal specialPrice
) {
    @Schema(description = "Single equipment item for V2 cost calculation")
    public record EquipmentItemRequest(
            @NotNull @Positive Long equipmentId,
            @NotBlank String equipmentType,
            @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant returnAt
    ) {
    }
}
