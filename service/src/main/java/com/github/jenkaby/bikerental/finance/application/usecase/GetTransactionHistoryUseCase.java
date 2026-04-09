package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface GetTransactionHistoryUseCase {

    Page<TransactionDto> execute(UUID customerId, TransactionHistoryFilter filter, PageRequest pageRequest);

    record TransactionDto(
            UUID customerId,
            BigDecimal amount,
            TransactionType type,
            Instant recordedAt,
            @Nullable PaymentMethod paymentMethod,
            @Nullable String reason,
            @Nullable TransactionSourceType sourceType,
            @Nullable String sourceId
    ) {
    }
}
