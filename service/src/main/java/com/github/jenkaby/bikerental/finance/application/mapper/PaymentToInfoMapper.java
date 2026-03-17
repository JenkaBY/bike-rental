package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class})
public interface PaymentToInfoMapper {

    PaymentInfo toPaymentInfo(Payment payment);
}
