package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Result of equipment return operation")
public record RentalReturnResponse(
        @Schema(description = "Updated rental details") RentalResponse rental,
        @Schema(description = "Cost breakdown for the rental") List<CostBreakdown> costs,
        @Schema(description = "Additional payment charged at return (0 if prepayment covered all)", example = "50.00") BigDecimal additionalPayment,
        @Schema(description = "Payment info for the additional charge") PaymentInfoResponse paymentInfo
) {
    @Schema(description = "Detailed cost breakdown")
    public record CostBreakdown(
            @Schema(description = "Equipment ID", example = "1") Long equipmentId,
            @Schema(description = "Base cost for planned duration", example = "200.00") BigDecimal baseCost,
            @Schema(description = "Overtime cost", example = "50.00") BigDecimal overtimeCost,
            @Schema(description = "Total cost", example = "250.00") BigDecimal totalCost,
            @Schema(description = "Actual rental duration in minutes", example = "130") int actualMinutes,
            @Schema(description = "Billable minutes (after forgiveness applied)", example = "125") int billableMinutes,
            @Schema(description = "Planned duration in minutes", example = "120") int plannedMinutes,
            @Schema(description = "Overtime minutes", example = "10") int overtimeMinutes,
            @Schema(description = "Whether forgiveness rule was applied") boolean forgivenessApplied,
            @Schema(description = "Human-readable calculation message") String calculationMessage
    ) {
    }
}
