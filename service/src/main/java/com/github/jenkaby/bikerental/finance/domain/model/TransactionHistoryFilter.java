package com.github.jenkaby.bikerental.finance.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

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
}
