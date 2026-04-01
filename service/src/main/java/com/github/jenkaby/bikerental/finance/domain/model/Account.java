package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@EqualsAndHashCode(of = "id")
public abstract class Account {

    private final UUID id;
    private final List<SubLedger> subLedgers;

    public abstract AccountType getAccountType();

    public SubLedger getSubLedger(LedgerType type) {
        return subLedgers.stream()
                .filter(sl -> sl.getLedgerType() == type)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(SubLedger.class, type.name()));
    }
}
