package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public interface ReleaseHoldUseCase {

    HoldResult execute(ReleaseHoldCommand command);

    record ReleaseHoldCommand(RentalRef rentalRef, String operatorId) {
    }

    record HoldResult(@Nullable TransactionRef transactionRef, @NonNull Instant recordedAt) {
    }
}
