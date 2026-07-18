package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.FindTransactionsUseCase.TransactionListItemDto;
import com.github.jenkaby.bikerental.finance.domain.model.Transaction;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionRecord;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class})
public interface TransactionListMapper {

    @Mapping(target = "entries", source = "records")
    TransactionListItemDto toDto(Transaction transaction);

    TransactionListItemDto.Entry toEntry(TransactionRecord record);
}
