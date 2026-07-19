package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

public record TransactionDetails(Transaction transaction, Money walletBalanceAfter, Money holdBalanceAfter) {
}
