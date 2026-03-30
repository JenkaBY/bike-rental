package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase.DepositResult;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordDepositUseCase.RecordDepositCommand;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.shared.mapper.IdempotencyKeyMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class, IdempotencyKeyMapper.class})
public interface DepositCommandMapper {

    RecordDepositCommand toCommand(RecordDepositRequest request);

    TransactionResponse toResponse(DepositResult result);
}
