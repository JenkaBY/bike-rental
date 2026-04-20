package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Full rental details")
public record RentalResponse(
        @Schema(description = "Rental ID", example = "1") @NotNull Long id,
        @Schema(description = "Customer UUID") @NotNull UUID customerId,
        @Schema(description = "List of equipment items in this rental") @NotNull List<EquipmentItemResponse> equipmentItems,
        @Schema(description = "Rental status", example = "ACTIVE") @NotNull String status,
        @Schema(description = "Rental start time") @NotNull LocalDateTime startedAt,
        @Schema(description = "Expected return time") LocalDateTime expectedReturnAt,
        @Schema(description = "Actual return time (null if not returned)") LocalDateTime actualReturnAt,
        @Schema(description = "Planned duration in minutes", example = "120") @NotNull Integer plannedDurationMinutes,
        @Schema(description = "Actual duration in minutes (null until returned)", example = "130") Integer actualDurationMinutes,
        @Schema(description = "Estimated rental cost", example = "200.00") @NotNull BigDecimal estimatedCost,
        @Schema(description = "Final rental cost (null until returned)", example = "250.00") BigDecimal finalCost
) {
}
