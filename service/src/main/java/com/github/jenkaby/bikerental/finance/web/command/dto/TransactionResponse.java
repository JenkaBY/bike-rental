package com.github.jenkaby.bikerental.finance.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Unified transaction response for deposits and adjustments")
public record TransactionResponse(
        @Schema(description = "Transaction UUID") @NotNull UUID transactionId,
        @Schema(description = "Timestamp when the transaction was recorded") @NotNull Instant recordedAt
) {
}
