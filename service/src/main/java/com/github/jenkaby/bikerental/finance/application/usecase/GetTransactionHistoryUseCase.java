package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionDetails;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.UUID;

public interface GetTransactionHistoryUseCase {

    Page<TransactionDetails> execute(UUID customerId, TransactionHistoryFilter filter, PageRequest pageRequest);
}
