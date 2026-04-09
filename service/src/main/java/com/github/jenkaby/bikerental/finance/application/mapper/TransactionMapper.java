package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionDto;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionMapper {

    TransactionDto toEntry(Transaction tx);
}
