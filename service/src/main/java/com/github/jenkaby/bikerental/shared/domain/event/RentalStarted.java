package com.github.jenkaby.bikerental.shared.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;


public record RentalStarted(
        Long rentalId,
        UUID customerId,
        Long equipmentId,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt
) implements BikeRentalEvent {
}
