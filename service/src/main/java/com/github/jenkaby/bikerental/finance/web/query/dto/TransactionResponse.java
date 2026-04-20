package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A single journal entry line in the transaction history")
public record TransactionResponse(
        @Schema(description = "Customer id") @NotNull UUID customerId,
        @Schema(description = "TxN amount") @NotNull BigDecimal amount,
        @Schema(description = "Business transaction type") @NotNull String type,
        @Schema(description = "When the entry was recorded (UTC ISO-8601)") @NotNull Instant recordedAt,
        @Nullable @Schema(description = "Payment method, present for deposits and withdrawals") String paymentMethod,
        @Nullable @Schema(description = "Free-text reason, present for adjustments") String reason,
        @Nullable @Schema(description = "Source type, e.g. RENTAL") String sourceType,
        @Nullable @Schema(description = "Source identifier") String sourceId
) {
}
