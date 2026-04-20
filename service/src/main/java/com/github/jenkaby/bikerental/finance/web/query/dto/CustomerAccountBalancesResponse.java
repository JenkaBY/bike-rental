package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Customer account balance breakdown")
public record CustomerAccountBalancesResponse(
        @Schema(description = "Available (spendable) wallet balance", example = "120.00")
        @NotNull BigDecimal walletBalance,
        @Schema(description = "Reserved (held) balance for active rentals", example = "30.00")
        @NotNull BigDecimal holdBalance,
        @Schema(description = "Timestamp of the most recent ledger mutation (UTC ISO-8601)")
        @NotNull Instant lastUpdatedAt
) {
}
