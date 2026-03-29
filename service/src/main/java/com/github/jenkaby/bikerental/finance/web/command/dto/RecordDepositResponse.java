package com.github.jenkaby.bikerental.finance.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of recording a fund deposit")
public record RecordDepositResponse(
        @Schema(description = "Transaction UUID") UUID transactionId,
        @Schema(description = "Timestamp when the deposit was recorded") Instant recordedAt
) {
}
