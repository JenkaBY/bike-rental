package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class SubLedger {

    private final UUID id;
    private final LedgerType ledgerType;
    private final BigDecimal balance;
}
