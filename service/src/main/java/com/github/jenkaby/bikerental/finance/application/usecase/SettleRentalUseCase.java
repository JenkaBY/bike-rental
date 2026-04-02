package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public interface SettleRentalUseCase {

    SettlementResult execute(SettleRentalCommand command);

    record SettleRentalCommand(CustomerRef customerRef, RentalRef rentalRef, Money finalCost, String operatorId) {
    }

    record SettlementResult(
            TransactionRef captureTransactionRef,
            @Nullable TransactionRef releaseTransactionRef,
            Instant recordedAt) {
    }
}
