package com.github.jenkaby.bikerental.rental.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RentalSearchFilter(
        List<RentalStatus> statuses,
        @Nullable UUID customerId,
        @Nullable String equipmentUid,
        @Nullable LocalDate from,
        @Nullable LocalDate to
) {

    public RentalSearchFilter {
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }

    public Map<String, String[]> toMap() {
        var result = new HashMap<String, String[]>();
        if (!statuses.isEmpty()) {
            result.put("status", statuses.stream().map(RentalStatus::name).toArray(String[]::new));
        }
        putIfPresent(result, "customerId", customerId != null ? customerId.toString() : null);
        putIfPresent(result, "equipmentUid", equipmentUid);
        putIfPresent(result, "createdFrom", from != null ? formatInstant(toStartOfDay(from)) : null);
        putIfPresent(result, "createdTo", to != null ? formatInstant(toStartOfNextDay(to)) : null);
        return result;
    }

    private static void putIfPresent(Map<String, String[]> target, String key, @Nullable String value) {
        if (value != null) {
            target.put(key, new String[]{value});
        }
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
