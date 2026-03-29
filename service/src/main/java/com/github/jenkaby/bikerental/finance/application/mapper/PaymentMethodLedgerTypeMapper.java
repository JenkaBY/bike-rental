package com.github.jenkaby.bikerental.finance.application.mapper;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper
public interface PaymentMethodLedgerTypeMapper {

    @ValueMapping(source = "CASH", target = "CASH")
    @ValueMapping(source = "CARD_TERMINAL", target = "CARD_TERMINAL")
    @ValueMapping(source = "BANK_TRANSFER", target = "BANK_TRANSFER")
    LedgerType toLedgerType(PaymentMethod paymentMethod);
}
