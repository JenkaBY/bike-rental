package com.github.jenkaby.bikerental.finance.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record TransactionFilter(
        Set<UUID> customerIds,
        @Nullable LocalDate fromDate,
        @Nullable LocalDate toDate,
        @Nullable String sourceId,
        @Nullable TransactionSourceType sourceType,
        Set<LedgerType> ledgerTypes
) {
    private static final TransactionFilter EMPTY =
            new TransactionFilter(Set.of(), null, null, null, null, Set.of());

    public TransactionFilter {
        customerIds = customerIds == null ? Set.of() : Set.copyOf(customerIds);
        ledgerTypes = ledgerTypes == null ? Set.of() : Set.copyOf(ledgerTypes);
    }

    public static TransactionFilter empty() {
        return EMPTY;
    }
}
