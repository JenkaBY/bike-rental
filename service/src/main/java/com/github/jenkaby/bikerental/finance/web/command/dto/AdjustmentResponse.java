package com.github.jenkaby.bikerental.finance.web.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdjustmentResponse(
        UUID transactionId,
        BigDecimal newWalletBalance,
        Instant recordedAt
) {}
