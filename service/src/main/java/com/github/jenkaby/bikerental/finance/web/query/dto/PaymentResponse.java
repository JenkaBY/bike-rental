package com.github.jenkaby.bikerental.finance.web.query.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Long rentalId,
        BigDecimal amount,
        String paymentType,
        String paymentMethod,
        Instant createdAt,
        String operatorId,
        String receiptNumber
) {
}
