package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.BikeRentalEvent;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record PaymentReceived(
        UUID paymentId,
        Long rentalId,
        Money amount,
        PaymentType paymentType,
        Instant receivedAt
) implements BikeRentalEvent {
}
