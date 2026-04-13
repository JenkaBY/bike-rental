package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import com.github.jenkaby.bikerental.shared.web.support.PercentValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request body for creating a rental via Fast Path")
public record CreateRentalRequest(
        @Schema(description = "Customer UUID") @NotNull(message = "Customer ID is required") UUID customerId,
        @Schema(description = "List of Equipment IDs to rent (preferred)") List<@Positive @NotNull Long> equipmentIds,
        @Schema(description = "Planned rental duration (ISO-8601)", example = "PT2H") @NotNull(message = "Duration is required") Duration duration,
        @Schema(description = "Optional tariff ID; auto-selected if not provided", example = "5") Long tariffId,
        @Schema(description = "Operator identifier", example = "operator-1") @NotBlank String operatorId,
        @Schema(description = "ID of a SPECIAL-type V2 tariff; mutually exclusive with discountPercent", example = "99")
        Long specialTariffId,
        @Schema(description = "Operator-provided fixed total; required when specialTariffId is set", example = "15.00")
        @MoneyAmount BigDecimal specialPrice,
        @Schema(description = "Discount percentage applied to the non-special subtotal (0-100); ignored when specialTariffId is set", example = "10")
        @PercentValue Integer discountPercent
) {

    @AssertTrue(message = "specialTariffId and discountPercent are mutually exclusive")
    @Schema(hidden = true)
    boolean isSpecialTariffAndDiscountMutuallyExclusive() {
        return specialTariffId == null || discountPercent == null;
    }

}
