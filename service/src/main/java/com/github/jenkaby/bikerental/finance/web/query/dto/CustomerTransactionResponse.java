package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A single business transaction in the customer's history, with per-bucket money movement")
public record CustomerTransactionResponse(
        @Schema(description = "Customer id")
        @NotNull UUID customerId,
        @Schema(description = "Absolute transaction amount (always positive); use direction/deltas for the sign")
        @NotNull BigDecimal amount,
        @Schema(description = "Business transaction type",
                allowableValues = {"DEPOSIT", "WITHDRAWAL", "HOLD", "CAPTURE", "RELEASE", "ADJUSTMENT"})
        @NotNull String type,
        @Schema(description = "Overall direction from the customer's perspective", allowableValues = {"CREDIT", "DEBIT"})
        @NotNull String direction,
        @Schema(description = "When the entry was recorded (UTC ISO-8601)")
        @NotNull Instant recordedAt,
        @Schema(description = "Payment method, present for deposits and withdrawals")
        @NotNull String paymentMethod,
        @Schema(description = "Free-text reason, present for adjustments") String reason,
        @Schema(description = "Source type, e.g. RENTAL") String sourceType,
        @Schema(description = "Source identifier") String sourceId,
        @Schema(description = "Signed change per bucket caused by this transaction")
        @NotNull Deltas deltas,
        @Schema(description = "Customer bucket balances after this transaction was applied")
        @NotNull Balances balances
) {

    @Schema(description = "Signed per-bucket deltas; external = wallet + hold (money crossing the customer boundary)")
    public record Deltas(
            @Schema(description = "Change to the available (wallet) balance") @NotNull BigDecimal wallet,
            @Schema(description = "Change to the reserved (hold) balance") @NotNull BigDecimal hold,
            @Schema(description = "Net change of the customer's total money (to/from the external world)")
            @NotNull BigDecimal external
    ) {
    }

    @Schema(description = "Customer bucket balances after this transaction")
    public record Balances(
            @Schema(description = "Available (wallet) balance after this transaction") @NotNull BigDecimal wallet,
            @Schema(description = "Reserved (hold) balance after this transaction") @NotNull BigDecimal hold
    ) {
    }
}
