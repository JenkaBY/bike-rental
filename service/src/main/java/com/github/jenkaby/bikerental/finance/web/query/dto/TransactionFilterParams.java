package com.github.jenkaby.bikerental.finance.web.query.dto;

import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record TransactionFilterParams(
        @Nullable @Size(max = 100, message = "customerIds must contain at most 100 elements") Set<UUID> customerIds,
        @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @Nullable String sourceId,
        @Nullable TransactionSourceType sourceType,
        @Nullable Set<LedgerType> ledgerTypes) {
}
