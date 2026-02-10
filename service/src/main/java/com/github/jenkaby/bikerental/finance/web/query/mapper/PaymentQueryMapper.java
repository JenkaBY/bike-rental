package com.github.jenkaby.bikerental.finance.web.query.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.web.query.dto.PaymentResponse;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class})
public interface PaymentQueryMapper {

    @Mapping(target = "paymentType", expression = "java(domain.getPaymentType().name())")
    @Mapping(target = "paymentMethod", expression = "java(domain.getPaymentMethod().name())")
    @Mapping(target = "amount", source = "amount")
    PaymentResponse toResponse(Payment domain);
}
