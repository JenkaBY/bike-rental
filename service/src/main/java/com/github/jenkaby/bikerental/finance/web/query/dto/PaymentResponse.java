package com.github.jenkaby.bikerental.finance.web.query.dto;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Long rentalId,
        Money amount,
        String paymentType,
        String paymentMethod,
        Instant createdAt,
        String operatorId,
        String receiptNumber
) {
}
