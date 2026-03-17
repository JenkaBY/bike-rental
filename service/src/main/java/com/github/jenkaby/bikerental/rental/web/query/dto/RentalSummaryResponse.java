package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Compact rental summary for list views")
public record RentalSummaryResponse(
        @Schema(description = "Rental ID", example = "1") Long id,
        @Schema(description = "Customer UUID") UUID customerId,
        @Schema(description = "List of rented equipment IDs", example = "[1,3]") List<Long> equipmentIds,
        @Schema(description = "Rental status", example = "ACTIVE") String status,
        @Schema(description = "Rental start time") LocalDateTime startedAt,
        @Schema(description = "Expected return time") LocalDateTime expectedReturnAt,
        @Schema(description = "Overdue minutes (null if not overdue)", example = "10") Integer overdueMinutes
) {
}
