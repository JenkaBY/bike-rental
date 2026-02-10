package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

/**
 * Use case for recording a prepayment for a rental.
 */
public interface RecordPrepaymentUseCase {

    PaymentInfo execute(RecordPrepaymentCommand command);

    record RecordPrepaymentCommand(
            Long rentalId,
            Money amount,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
    }
}
