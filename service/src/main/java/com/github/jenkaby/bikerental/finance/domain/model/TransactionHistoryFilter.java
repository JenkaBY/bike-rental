package com.github.jenkaby.bikerental.finance.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record TransactionHistoryFilter(
        @Nullable LocalDate fromDate,
        @Nullable LocalDate toDate,
        @Nullable String sourceId,
        @Nullable TransactionSourceType sourceType
) {
    private static final TransactionHistoryFilter EMPTY = new TransactionHistoryFilter(null, null, null, null);

    public static TransactionHistoryFilter empty() {
        return EMPTY;
    }

    public Map<String, String> toMap() {
        var result = new HashMap<String, String>();
        result.put("fromDate", Optional.ofNullable(fromDate).map(this::toInstant).map(TransactionHistoryFilter::toString).orElse(null));
        result.put("toDate", Optional.ofNullable(toDate).map(this::nextDay).map(this::toInstant).map(TransactionHistoryFilter::toString).orElse(null));
        result.put("sourceId", sourceId);
        result.put("sourceType", sourceType != null ? sourceType.name() : null);
        return result;
    }

    private LocalDate nextDay(LocalDate date) {
        return date.plusDays(1);
    }

    //
    private Instant toInstant(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private static String toString(Instant date) {
        return DateTimeFormatter.ISO_INSTANT.format(date);
    }
}
