package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.GetTransactionHistoryUseCase.TransactionDto;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import com.github.jenkaby.bikerental.shared.mapper.CustomerRefMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {CustomerRefMapper.class})
public interface TransactionHistoryQueryMapper {

    CustomerTransactionResponse toResponse(TransactionDto dto);
}
