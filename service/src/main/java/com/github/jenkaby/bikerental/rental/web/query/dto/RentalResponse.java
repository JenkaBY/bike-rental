package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Full rental details")
public record RentalResponse(
        @Schema(description = "Rental ID", example = "1") Long id,
        @Schema(description = "Customer UUID") UUID customerId,
        @Schema(description = "Equipment ID", example = "1") Long equipmentId,
        @Schema(description = "Tariff ID", example = "3") Long tariffId,
        @Schema(description = "Rental status", example = "ACTIVE") String status,
        @Schema(description = "Rental start time") LocalDateTime startedAt,
        @Schema(description = "Expected return time") LocalDateTime expectedReturnAt,
        @Schema(description = "Actual return time (null if not returned)") LocalDateTime actualReturnAt,
        @Schema(description = "Planned duration in minutes", example = "120") Integer plannedDurationMinutes,
        @Schema(description = "Actual duration in minutes (null until returned)", example = "130") Integer actualDurationMinutes,
        @Schema(description = "Estimated rental cost", example = "200.00") BigDecimal estimatedCost,
        @Schema(description = "Final rental cost (null until returned)", example = "250.00") BigDecimal finalCost
) {
}
