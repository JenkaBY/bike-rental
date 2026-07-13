package com.github.jenkaby.bikerental.rental.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Confirms the final full return of a rental against a previously created cost quote")
public record ConfirmReturnRequest(
        @NotNull UUID quoteId,
        @NotBlank String operatorId
) {
}
