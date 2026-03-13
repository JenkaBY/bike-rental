package com.github.jenkaby.bikerental.shared.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record RentalStarted(
        Long rentalId,
        UUID customerId,
        List<Long> equipmentIds,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt
) implements BikeRentalEvent {
}
