package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public record SettlementInfo(
        @NonNull List<TransactionRef> captureTransactionRefs,
        @Nullable TransactionRef releaseTransactionRef,
        @NonNull Instant recordedAt) {
}
