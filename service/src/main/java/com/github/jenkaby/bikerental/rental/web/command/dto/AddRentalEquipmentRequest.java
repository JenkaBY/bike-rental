package com.github.jenkaby.bikerental.rental.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Request body for adding equipment to an active rental")
public record AddRentalEquipmentRequest(

        @Schema(description = "List of Equipment IDs to add to the rental")
        @NotEmpty
        List<@Positive @NotNull Long> equipmentIds,

        @Schema(description = "Operator identifier", example = "operator-1")
        @NotBlank
        String operatorId
) {
}
