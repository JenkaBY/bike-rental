package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

public interface RecordPaymentUseCase {

    Payment execute(RecordPaymentCommand command);

    record RecordPaymentCommand(
            Long rentalId,
            Money amount,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
    }
}
