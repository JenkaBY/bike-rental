package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
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


    public SubLedger getSubLedger(PaymentMethod paymentMethod) {
        var type = switch (paymentMethod) {
            case CASH -> LedgerType.CASH;
            case CARD_TERMINAL -> LedgerType.CARD_TERMINAL;
            case BANK_TRANSFER -> LedgerType.BANK_TRANSFER;
            case INTERNAL_TRANSFER -> LedgerType.ADJUSTMENT;
            default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        };
        return getSubLedger(type);
    }

    protected SubLedger getSubLedger(LedgerType type) {
        return subLedgers.stream()
                .filter(sl -> sl.getLedgerType() == type)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(SubLedger.class, type.name()));
    }
}
