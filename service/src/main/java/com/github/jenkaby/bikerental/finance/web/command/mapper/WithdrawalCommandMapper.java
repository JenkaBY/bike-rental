package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.RecordWithdrawalCommand;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordWithdrawalUseCase.WithdrawalResult;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.shared.mapper.IdempotencyKeyMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class, IdempotencyKeyMapper.class})
public interface WithdrawalCommandMapper {

    RecordWithdrawalCommand toCommand(RecordWithdrawalRequest request);

    TransactionResponse toResponse(WithdrawalResult result);
}
