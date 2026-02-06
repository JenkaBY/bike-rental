package com.github.jenkaby.bikerental.rental.web.query.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией об аренде.
 */
public record RentalResponse(
        Long id,
        UUID customerId,
        Long equipmentId,
        Long tariffId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime expectedReturnAt,
        LocalDateTime actualReturnAt,
        Integer plannedDurationMinutes,
        Integer actualDurationMinutes,
        BigDecimal estimatedCost,
        BigDecimal finalCost
) {
}
