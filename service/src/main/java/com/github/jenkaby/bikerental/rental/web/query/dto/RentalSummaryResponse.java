package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Compact rental summary for list views")
public record RentalSummaryResponse(
        @Schema(description = "Rental ID", example = "1") @NotNull Long id,
        @Schema(description = "Customer UUID") @NotNull UUID customerId,
        @Schema(description = "Rented equipment items") List<RentalSummaryEquipmentResponse> equipments,
        @Schema(description = "Rental status", example = "ACTIVE") @NotNull String status,
        @Schema(description = "Rental start time") Instant startedAt,
        @Schema(description = "Expected return time") Instant expectedReturnAt,
        @Schema(description = "Overdue minutes (null if not overdue)", example = "10") Integer overdueMinutes,
        @Schema(description = "Planned duration in minutes", example = "120") @NotNull Integer plannedDurationMinutes,
        @Schema(description = "Actual duration in minutes (null until returned)", example = "130") Integer actualDurationMinutes,
        @Schema(description = "Estimated rental cost", example = "200.00") @NotNull BigDecimal estimatedCost,
        @Schema(description = "Final rental cost (null until returned)", example = "250.00") BigDecimal finalCost,
        @Schema(description = "Rental creation time") @NotNull Instant createdAt
) {
}
