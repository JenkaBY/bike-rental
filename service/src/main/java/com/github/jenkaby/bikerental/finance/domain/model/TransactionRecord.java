package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class TransactionRecord {

    private final UUID id;
    private final SubLedgerRef subLedgerRef;
    private final LedgerType ledgerType;
    private final EntryDirection direction;
    private final Money amount;
}
