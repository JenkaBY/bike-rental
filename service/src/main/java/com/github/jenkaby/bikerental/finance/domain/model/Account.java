package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Account {

    private final UUID id;
    private final AccountType accountType;
    @Nullable
    private final CustomerRef customerRef;
    private final List<SubLedger> subLedgers;

    public SubLedger getCustomerWallet() {
        return getSubLedger(LedgerType.CUSTOMER_WALLET);
    }

    public SubLedger getSubLedger(LedgerType type) {
        return subLedgers.stream()
                .filter(sl -> sl.getLedgerType() == type)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(SubLedger.class, type.name()));
    }
}
