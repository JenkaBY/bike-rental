package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionDto;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionResponse;
import com.github.jenkaby.bikerental.shared.mapper.CustomerRefMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {CustomerRefMapper.class})
public interface TransactionHistoryQueryMapper {

    TransactionResponse toResponse(TransactionDto dto);
}
