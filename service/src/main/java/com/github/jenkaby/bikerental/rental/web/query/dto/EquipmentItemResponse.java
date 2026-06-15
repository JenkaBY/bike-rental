package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

@Schema(description = "Equipment item within rental")
public record EquipmentItemResponse(
        @Schema(description = "Equipment ID", example = "1") @NotNull Long equipmentId,
        @Schema(description = "Equipment UID", example = "BIKE-001") String equipmentUid,
        @Schema(description = "Estimated cost for this equipment (optional)") @NotNull BigDecimal estimatedCost,
        @Schema(description = "Final cost for this equipment (optional)") BigDecimal finalCost,
        @Schema(description = "Tariff ID", example = "3") Long tariffId,
        @Schema(description = "Status rental equipment", example = "ASSIGNED") @NotNull String status,
        @Schema(description = "Final cost breakdown; populated only when equipment is returned") @Nullable CostBreakdown breakdown
) {

    @Schema(description = "Per-equipment final cost breakdown")
    public record CostBreakdown(
            @NotNull String pricingType,
            @NotNull String tariffName,
            @NotNull Integer billedDurationMinutes,
            @Nullable Integer overtimeMinutes,
            @Nullable Integer forgivenMinutes,
            @NotNull BigDecimal itemCost,
            @NotNull CalculationDetail calculationBreakdown
    ) {

        @Schema(description = "Detailed calculation pattern and parameters")
        public record CalculationDetail(
                @NotNull String breakdownPatternCode,
                @NotNull String message,
                @Nullable Object params
        ) {
        }
    }
}
