package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public interface ApplyAdjustmentUseCase {

    AdjustmentResult execute(ApplyAdjustmentCommand command);

    record ApplyAdjustmentCommand(
            UUID customerId,
            Money amount,
            String reason,
            String operatorId
    ) {
    }

    record AdjustmentResult(
            UUID transactionId,
            Money newWalletBalance,
            Instant recordedAt
    ) {
    }
}
