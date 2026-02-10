package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;


public record PaymentInfo(
        UUID id,
        Money amount,
        PaymentMethod paymentMethod,
        String receiptNumber,
        Instant createdAt
) {
}
