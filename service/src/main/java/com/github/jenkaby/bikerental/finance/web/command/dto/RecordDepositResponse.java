package com.github.jenkaby.bikerental.finance.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of recording a fund deposit")
public record RecordDepositResponse(
        @Schema(description = "Transaction UUID") @NotNull UUID transactionId,
        @Schema(description = "Timestamp when the deposit was recorded") @NotNull Instant recordedAt
) {
}
