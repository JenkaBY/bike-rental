package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import org.mapstruct.Mapper;

@Mapper
public interface PaymentToInfoMapper {

    PaymentInfo toPaymentInfo(Payment payment);
}
