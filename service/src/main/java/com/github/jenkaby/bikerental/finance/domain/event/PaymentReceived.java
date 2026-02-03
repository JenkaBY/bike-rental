package com.github.jenkaby.bikerental.finance.domain.event;

import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public record PaymentReceived(
        UUID paymentId,
        Long rentalId,
        Money amount,
        PaymentType paymentType,
        Instant receivedAt
) {
}
