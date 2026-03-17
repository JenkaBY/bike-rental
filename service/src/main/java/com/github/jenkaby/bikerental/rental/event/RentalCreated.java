package com.github.jenkaby.bikerental.rental.event;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RentalCreated(
        Long rentalId,
        UUID customerId,
        List<Long> equipmentIds,
        String status,
        Instant createdAt
) implements BikeRentalEvent {
}
