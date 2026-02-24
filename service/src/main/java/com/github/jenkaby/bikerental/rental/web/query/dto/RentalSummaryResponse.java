package com.github.jenkaby.bikerental.rental.web.query.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RentalSummaryResponse(
        Long id,
        UUID customerId,
        Long equipmentId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt,
        Integer overdueMinutes
) {
}
