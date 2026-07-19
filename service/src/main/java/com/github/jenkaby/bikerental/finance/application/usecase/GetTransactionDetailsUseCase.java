package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;

import java.util.UUID;

public interface GetTransactionDetailsUseCase {

    TransactionDetails execute(UUID transactionId);
}
