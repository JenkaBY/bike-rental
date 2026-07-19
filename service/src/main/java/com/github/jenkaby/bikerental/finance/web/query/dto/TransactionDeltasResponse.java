package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Signed per-bucket deltas; external = wallet + hold (money crossing the customer boundary)")
public record TransactionDeltasResponse(
        @Schema(description = "Change to the available (wallet) balance") @NotNull BigDecimal wallet,
        @Schema(description = "Change to the reserved (hold) balance") @NotNull BigDecimal hold,
        @Schema(description = "Net change of the customer's total money (to/from the external world)")
        @NotNull BigDecimal external
) {
}
