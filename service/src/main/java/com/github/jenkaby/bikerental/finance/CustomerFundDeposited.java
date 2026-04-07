package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

import java.time.Instant;
import java.util.UUID;

public record CustomerFundDeposited(
        UUID customerId,
        UUID transactionId,
        String operatorId,
        Instant depositedAt
) implements BikeRentalEvent {
}
