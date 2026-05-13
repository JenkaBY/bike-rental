package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import com.github.jenkaby.bikerental.shared.web.support.PercentValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request body for creating a rental via Fast Path")
public record RentalRequest(
        @Schema(description = "Customer UUID")
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @Schema(description = "List of Equipment IDs to rent (preferred)")
        @NotNull @Size(min = 1)
        List<@Positive @NotNull Long> equipmentIds,

        @Schema(description = "Planned rental duration in minutes", example = "120")
        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be positive")
        Integer duration,

        @Schema(description = "Operator identifier", example = "operator-1")
        @NotBlank
        String operatorId,

        @Schema(description = "Id of a SPECIAL-type tariff; mutually exclusive with discountPercent", example = "99")
        @Positive
        Long specialTariffId,

        @Schema(description = "Operator-provided fixed total; required when specialTariffId is set", example = "15.00")
        @MoneyAmount
        BigDecimal specialPrice,

        @Schema(description = "Discount percentage applied to the non-special subtotal (0-100); ignored when specialTariffId is set", example = "10")
        @PercentValue
        Integer discountPercent
) {

    @AssertTrue(message = "specialTariffId and discountPercent are mutually exclusive")
    @Schema(hidden = true)
    boolean isSpecialTariffAndDiscountMutuallyExclusive() {
        return specialTariffId == null || discountPercent == null;
    }

    @AssertTrue(message = "specialTariffId and specialPrice must exist or not simultaneously")
    @Schema(hidden = true)
    boolean isSpecialPriceAndTariffConsistent() {
        return (specialTariffId == null && specialPrice == null) || (specialTariffId != null && specialPrice != null);
    }
}
