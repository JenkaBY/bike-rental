package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public interface ApplyAdjustmentUseCase {

    AdjustmentResult execute(ApplyAdjustmentCommand command);

    record ApplyAdjustmentCommand(
            UUID customerId,
            Money amount,
            String reason,
            String operatorId,
            IdempotencyKey idempotencyKey
    ) {
    }

    record AdjustmentResult(
            UUID transactionId,
            Instant recordedAt
    ) {
    }
}
