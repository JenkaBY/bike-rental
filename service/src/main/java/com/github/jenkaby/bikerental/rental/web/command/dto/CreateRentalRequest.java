package com.github.jenkaby.bikerental.rental.web.command.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for Fast Path: creating rental with all data.
 */
public record CreateRentalRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Equipment ID is required")
        Long equipmentId,

        @NotNull(message = "Duration is required")
        Duration duration,

        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        Long tariffId  // Optional - if not provided, will be auto-selected
) {
}
