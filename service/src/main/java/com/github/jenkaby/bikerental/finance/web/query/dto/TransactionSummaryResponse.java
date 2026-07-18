package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A summary of a business transaction with its double-entry legs; full details are served by the transaction details endpoint")
public record TransactionSummaryResponse(
        @Schema(description = "Transaction id")
        @NotNull UUID id,
        @Schema(description = "Owning customer id")
        @NotNull UUID customerId,
        @Schema(description = "Absolute transaction amount (always positive)")
        @NotNull BigDecimal amount,
        @Schema(description = "Business transaction type",
                allowableValues = {"DEPOSIT", "WITHDRAWAL", "HOLD", "CAPTURE", "RELEASE", "ADJUSTMENT"})
        @NotNull String type,
        @Schema(description = "When the transaction was recorded (UTC ISO-8601)")
        @NotNull Instant recordedAt,
        @Schema(description = "Payment method")
        @NotNull String paymentMethod,
        @Schema(description = "Free-text reason, present for adjustments") String reason,
        @Schema(description = "Source type, e.g. RENTAL") String sourceType,
        @Schema(description = "Source identifier") String sourceId,
        @Schema(description = "Operator that recorded the transaction")
        @NotNull String operatorId,
        @Schema(description = "All double-entry legs of this transaction")
        @NotNull List<TransactionEntryResponse> entries
) {

    @Schema(description = "A single ledger leg of the double-entry transaction")
    public record TransactionEntryResponse(
            @Schema(description = "Affected ledger",
                    allowableValues = {"CASH", "CARD_TERMINAL", "BANK_TRANSFER", "REVENUE", "ADJUSTMENT",
                            "CUSTOMER_WALLET", "CUSTOMER_HOLD"})
            @NotNull String ledgerType,
            @Schema(description = "Entry direction on that ledger", allowableValues = {"CREDIT", "DEBIT"})
            @NotNull String direction,
            @Schema(description = "Absolute amount of this leg (always positive)")
            @NotNull BigDecimal amount
    ) {
    }
}
