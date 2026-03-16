package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Schema(description = "Request body for returning rented equipment")
public record ReturnEquipmentRequest(
        @Schema(description = "Rental ID", example = "1") Long rentalId,
        @Schema(description = "List of equipment IDs to return") List<@NotNull Long> equipmentIds,
        @Schema(description = "List of equipment UIDs to return") List<@NotEmpty String> equipmentUids,
        @Schema(description = "Payment method for any additional charge") @NotNull PaymentMethod paymentMethod,
        @Schema(description = "Operator identifier", example = "operator-1") @NotEmpty String operatorId
) {
    @AssertTrue(message = "At least one of rentalId, equipmentId, or equipmentUid must be provided")
    public boolean isValidIdentifiers() {
        int count = 0;
        if (rentalId != null) count++;
        if (!CollectionUtils.isEmpty(equipmentIds)) count++;
        if (!CollectionUtils.isEmpty(equipmentUids)) count++;
        return count > 0;
    }
}
