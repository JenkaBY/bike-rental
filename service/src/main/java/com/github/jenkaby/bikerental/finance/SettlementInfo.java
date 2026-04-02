package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record SettlementInfo(
        TransactionRef captureTransactionRef,
        @Nullable TransactionRef releaseTransactionRef,
        Instant recordedAt) {
}
