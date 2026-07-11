package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import java.math.BigDecimal;

public interface LedgerBalanceProjection {

    String getLedgerType();

    BigDecimal getRunningBalance();
}
