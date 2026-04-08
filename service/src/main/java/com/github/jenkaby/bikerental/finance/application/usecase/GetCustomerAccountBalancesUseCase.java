package com.github.jenkaby.bikerental.finance.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface GetCustomerAccountBalancesUseCase {

    CustomerAccountBalances execute(UUID customerId);

    record CustomerAccountBalances(
            BigDecimal walletBalance,
            BigDecimal holdBalance,
            Instant lastUpdatedAt
    ) {
    }
}
