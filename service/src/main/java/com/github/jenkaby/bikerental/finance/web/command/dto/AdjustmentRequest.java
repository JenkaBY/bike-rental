package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentRequest(
        @NotNull UUID customerId,
        @MoneyAmount BigDecimal amount,
        @NotBlank String reason,
        @NotBlank String operatorId,
        @NotNull UUID idempotencyKey
) {}
