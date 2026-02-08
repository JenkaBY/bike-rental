package com.github.jenkaby.bikerental.rental.event;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

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
