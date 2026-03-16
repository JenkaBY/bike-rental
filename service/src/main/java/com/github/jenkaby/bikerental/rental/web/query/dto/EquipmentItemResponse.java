package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Equipment item within rental")
public record EquipmentItemResponse(
        @Schema(description = "Equipment ID", example = "1") Long equipmentId,
        @Schema(description = "Equipment UID", example = "BIKE-001") String equipmentUid,
        @Schema(description = "Estimated cost for this equipment (optional)") BigDecimal estimatedCost,
        @Schema(description = "Final cost for this equipment (optional)") BigDecimal finalCost,
        @Schema(description = "Tariff ID", example = "3") Long tariffId,
        @Schema(description = "Status rental equipment", example = "ASSIGNED") String status
) {
}

