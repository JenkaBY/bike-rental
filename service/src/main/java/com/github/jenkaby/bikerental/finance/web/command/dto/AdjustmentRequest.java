package com.github.jenkaby.bikerental.finance.web.command.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentRequest(
        @NotNull UUID customerId,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank String reason,
        @NotBlank String operatorId
) {}
