package com.github.jenkaby.bikerental.finance.domain.model;

public enum LedgerType {
    CASH,
    CARD_TERMINAL,
    BANK_TRANSFER,
    REVENUE,
    ADJUSTMENT,
    CUSTOMER_WALLET,
    CUSTOMER_HOLD;

    public boolean isSystemLedger() {
        return switch (this) {
            case CASH, CARD_TERMINAL, BANK_TRANSFER, REVENUE, ADJUSTMENT -> true;
            case CUSTOMER_WALLET, CUSTOMER_HOLD -> false;
        };
    }
}
