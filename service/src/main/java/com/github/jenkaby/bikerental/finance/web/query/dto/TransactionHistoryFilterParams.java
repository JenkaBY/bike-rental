package com.github.jenkaby.bikerental.finance.web.query.dto;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TransactionHistoryFilterParams(
        @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @Nullable String sourceId,
        @Nullable TransactionSourceType sourceType) {
}
