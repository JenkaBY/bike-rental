package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Full details of a single business transaction: its double-entry legs with per-ledger movement "
        + "and running balances, plus the customer-side deltas and resulting bucket balances")
public record TransactionDetailsResponse(
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
        @Schema(description = "Signed per-bucket change caused by this transaction")
        @NotNull TransactionDeltasResponse deltas,
        @Schema(description = "Customer bucket balances after this transaction")
        @NotNull TransactionBalancesResponse balances,
        @Schema(description = "All double-entry legs of this transaction with their movement and running balance")
        @NotNull List<TransactionDetailEntryResponse> entries
) {

    @Schema(description = "A single ledger leg of the double-entry transaction")
    public record TransactionDetailEntryResponse(
            @Schema(description = "Affected ledger",
                    allowableValues = {"CASH", "CARD_TERMINAL", "BANK_TRANSFER", "REVENUE", "ADJUSTMENT",
                            "CUSTOMER_WALLET", "CUSTOMER_HOLD"})
            @NotNull String ledgerType,
            @Schema(description = "Entry direction on that ledger", allowableValues = {"CREDIT", "DEBIT"})
            @NotNull String direction,
            @Schema(description = "Absolute amount of this leg (always positive)")
            @NotNull BigDecimal amount,
            @Schema(description = "Signed change to this ledger's balance (asset ledgers increase on debit)")
            @NotNull BigDecimal signedDelta,
            @Schema(description = "This ledger's running balance after the leg; null when not tracked for the ledger",
                    nullable = true)
            BigDecimal balanceAfter,
            @Schema(description = "Whether this is a system (house) ledger rather than a customer ledger")
            boolean systemLedger
    ) {
    }
}
