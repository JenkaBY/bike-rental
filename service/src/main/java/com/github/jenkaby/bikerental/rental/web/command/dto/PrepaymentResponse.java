package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record PrepaymentResponse(
        UUID paymentId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String receiptNumber,
        Instant createdAt
) {
}
