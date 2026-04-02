package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CustomerAccount extends Account {

    private final CustomerRef customerRef;

    @Override
    public AccountType getAccountType() {
        return AccountType.CUSTOMER;
    }

    public SubLedger getWallet() {
        return getSubLedger(LedgerType.CUSTOMER_WALLET);
    }

    public SubLedger getOnHold() {
        return getSubLedger(LedgerType.CUSTOMER_HOLD);
    }

    public boolean isBalanceSufficient(Money amount) {
        var available = getWallet().getBalance().subtract(getOnHold().getBalance());
        return !available.isLessThan(amount);
    }

    public final Money availableBalance() {
        return getWallet().getBalance();
    }
}
