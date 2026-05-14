package com.github.jenkaby.bikerental.rental.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record RentalSearchFilter(
        @Nullable RentalStatus status,
        @Nullable UUID customerId,
        @Nullable String equipmentUid,
        @Nullable LocalDate from,
        @Nullable LocalDate to
) {

    public Map<String, String> toMap() {
        var result = new HashMap<String, String>();
        result.put("status", status != null ? status.name() : null);
        result.put("customerId", customerId != null ? customerId.toString() : null);
        result.put("equipmentUid", equipmentUid);
        result.put("createdFrom", Optional.ofNullable(from).map(this::toStartOfDay).map(RentalSearchFilter::formatInstant).orElse(null));
        result.put("createdTo", Optional.ofNullable(to).map(this::toStartOfNextDay).map(RentalSearchFilter::formatInstant).orElse(null));
        return result;
    }

    private Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant toStartOfNextDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private static String formatInstant(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
