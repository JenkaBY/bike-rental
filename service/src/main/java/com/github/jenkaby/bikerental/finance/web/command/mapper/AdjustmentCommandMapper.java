package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.AdjustmentResult;
import com.github.jenkaby.bikerental.finance.application.usecase.ApplyAdjustmentUseCase.ApplyAdjustmentCommand;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentResponse;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class})
public interface AdjustmentCommandMapper {

    ApplyAdjustmentCommand toCommand(AdjustmentRequest request);

    AdjustmentResponse toResponse(AdjustmentResult result);
}
