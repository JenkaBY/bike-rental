package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public interface RecordDepositUseCase {

    DepositResult execute(RecordDepositCommand command);

    record RecordDepositCommand(
            UUID customerId,
            Money amount,
            PaymentMethod paymentMethod,
            String operatorId,
            IdempotencyKey idempotencyKey
    ) {
    }

    record DepositResult(
            UUID transactionId,
            Instant recordedAt
    ) {
    }
}
