package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.util.UUID;

public record TransactionRecordWithoutId(
        SubLedgerRef subLedgerRef,
        LedgerType ledgerType,
        EntryDirection direction,
        Money amount
) {

    public TransactionRecord toTransaction(UUID id) {
        return TransactionRecord.builder()
                .id(id)
                .subLedgerRef(subLedgerRef())
                .ledgerType(ledgerType())
                .direction(direction())
                .amount(amount())
                .build();
    }
}
