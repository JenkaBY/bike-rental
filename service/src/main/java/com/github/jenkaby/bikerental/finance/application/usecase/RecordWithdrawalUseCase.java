package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;
import java.util.UUID;

public interface RecordWithdrawalUseCase {

    WithdrawalResult execute(RecordWithdrawalCommand command);

    record RecordWithdrawalCommand(
            UUID customerId,
            Money amount,
            PaymentMethod payoutMethod,
            String operatorId,
            IdempotencyKey idempotencyKey
    ) {
    }

    record WithdrawalResult(
            UUID transactionId,
            Instant recordedAt
    ) {
    }
}
