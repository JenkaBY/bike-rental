package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.domain.model.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

public interface RecordPaymentUseCase {

    RecordPaymentResponse execute(RecordPaymentCommand command);

    record RecordPaymentCommand(
            Long rentalId,
            BigDecimal amount,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
    }

    record RecordPaymentResponse(UUID paymentId, String receiptNumber) {
    }
}
