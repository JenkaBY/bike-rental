package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;

import java.math.BigDecimal;

public interface RecordPaymentUseCase {

    Payment execute(RecordPaymentCommand command);

    record RecordPaymentCommand(
            Long rentalId,
            BigDecimal amount,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
    }
}
