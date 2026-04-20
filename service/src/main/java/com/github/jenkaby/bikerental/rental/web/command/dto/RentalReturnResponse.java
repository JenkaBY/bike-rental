package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Schema(description = "Result of equipment return operation")
public record RentalReturnResponse(
        @Schema(description = "Updated rental details") @NotNull RentalResponse rental,
        @Schema(description = "Settlement transaction related to rental return") @Nullable SettlementResponse settlement
) {
}
