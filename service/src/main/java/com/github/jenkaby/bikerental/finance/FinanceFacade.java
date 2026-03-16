package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.util.List;
import java.util.Optional;


public interface FinanceFacade {

    PaymentInfo recordPrepayment(Long rentalId, Money amount, PaymentMethod method, String operatorId);

    PaymentInfo recordAdditionalPayment(Long rentalId, Money amount, PaymentMethod method, String operatorId);

    boolean hasPrepayment(Long rentalId);

    Optional<PaymentInfo> getPrepayment(Long rentalId);

    List<PaymentInfo> getPayments(Long rentalId);
}
