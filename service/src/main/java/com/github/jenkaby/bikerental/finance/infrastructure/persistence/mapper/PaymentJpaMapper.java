package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class},
        imports = {PaymentType.class, PaymentMethod.class})
public interface PaymentJpaMapper {

    @Mapping(target = "paymentType", expression = "java(PaymentType.valueOf(entity.getPaymentType()))")
    @Mapping(target = "paymentMethod", expression = "java(PaymentMethod.valueOf(entity.getPaymentMethod()))")
    Payment toDomain(PaymentJpaEntity entity);

    @Mapping(target = "paymentType", expression = "java(domain.getPaymentType().name())")
    @Mapping(target = "paymentMethod", expression = "java(domain.getPaymentMethod().name())")
    PaymentJpaEntity toEntity(Payment domain);
}
