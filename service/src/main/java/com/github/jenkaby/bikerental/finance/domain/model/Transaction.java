package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Transaction {

    private final UUID id;
    private final TransactionType type;
    private final PaymentMethod paymentMethod;
    private final Money amount;
    private final UUID customerId;
    private final String operatorId;
    @Nullable
    private final TransactionSourceType sourceType;
    @Nullable
    private final String sourceId;
    private final Instant recordedAt;
    private final IdempotencyKey idempotencyKey;
    @Nullable
    private final String reason;
    private final List<TransactionRecord> records;
}
