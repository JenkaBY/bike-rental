package com.github.jenkaby.bikerental.finance.web.command.mapper;

import com.github.jenkaby.bikerental.finance.application.usecase.RecordPaymentUseCase.RecordPaymentCommand;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentResponse;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class})
public interface PaymentCommandMapper {

    RecordPaymentCommand toCommand(RecordPaymentRequest request);

    @Mapping(target = "paymentId", source = "id")
    RecordPaymentResponse toRecordPaymentResponse(Payment payment);
}
