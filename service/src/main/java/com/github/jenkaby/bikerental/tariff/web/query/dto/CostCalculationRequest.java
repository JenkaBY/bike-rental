package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.shared.web.support.PercentValue;
import com.github.jenkaby.bikerental.tariff.web.query.validation.SpecialTariffConsistency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request for rental cost calculation")
@SpecialTariffConsistency
public record CostCalculationRequest(
        @NotEmpty List<@NotNull @Valid EquipmentItemRequest> equipments,
        @NotNull @Positive Integer plannedDurationMinutes,
        @Min(0) Integer actualDurationMinutes,
        @PercentValue Integer discountPercent,
        @Positive Long specialTariffId,
        @DecimalMin("0") BigDecimal specialPrice,
        LocalDate rentalDate
) {
    @Schema(description = "Single equipment item for cost calculation")
    public record EquipmentItemRequest(
            @NotBlank String equipmentType
    ) {
    }
}
