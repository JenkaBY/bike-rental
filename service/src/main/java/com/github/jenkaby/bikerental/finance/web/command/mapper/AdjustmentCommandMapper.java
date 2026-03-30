package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.AdjustmentResult;
import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.ApplyAdjustmentCommand;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class}, imports = {IdempotencyKey.class})
public interface AdjustmentCommandMapper {

    @Mapping(target = "idempotencyKey", expression = "java(IdempotencyKey.of(request.idempotencyKey()))")
    ApplyAdjustmentCommand toCommand(AdjustmentRequest request);

    TransactionResponse toResponse(AdjustmentResult result);
}
