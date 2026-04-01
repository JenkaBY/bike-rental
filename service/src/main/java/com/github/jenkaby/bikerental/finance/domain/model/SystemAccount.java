package com.github.jenkaby.bikerental.finance.domain.model;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SystemAccount extends Account {

    @Override
    public AccountType getAccountType() {
        return AccountType.SYSTEM;
    }

    public SubLedger getCash() {
        return getSubLedger(LedgerType.CASH);
    }

    public SubLedger getCardTerminal() {
        return getSubLedger(LedgerType.CARD_TERMINAL);
    }

    public SubLedger getBankTransfer() {
        return getSubLedger(LedgerType.BANK_TRANSFER);
    }

    public SubLedger getRevenue() {
        return getSubLedger(LedgerType.REVENUE);
    }

    public SubLedger getAdjustment() {
        return getSubLedger(LedgerType.ADJUSTMENT);
    }
}
