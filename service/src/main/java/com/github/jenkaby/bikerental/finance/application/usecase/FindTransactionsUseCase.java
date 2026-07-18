package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FindTransactionsUseCase {

    Page<TransactionListItemDto> execute(TransactionFilter filter, PageRequest pageRequest);

    record TransactionListItemDto(
            UUID id,
            UUID customerId,
            BigDecimal amount,
            TransactionType type,
            Instant recordedAt,
            PaymentMethod paymentMethod,
            @Nullable String reason,
            @Nullable TransactionSourceType sourceType,
            @Nullable String sourceId,
            String operatorId,
            List<Entry> entries
    ) {

        public record Entry(LedgerType ledgerType, EntryDirection direction, BigDecimal amount) {
        }
    }
}
