package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Customer bucket balances after the transaction")
public record TransactionBalancesResponse(
        @Schema(description = "Available (wallet) balance after the transaction") @NotNull BigDecimal wallet,
        @Schema(description = "Reserved (hold) balance after the transaction") @NotNull BigDecimal hold
) {
}
