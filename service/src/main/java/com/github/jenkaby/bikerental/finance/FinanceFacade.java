package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientHoldException;
import com.github.jenkaby.bikerental.finance.domain.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public interface FinanceFacade {

    @Deprecated(forRemoval = true)
    PaymentInfo recordPrepayment(Long rentalId, Money amount, PaymentMethod method, String operatorId);

    @Deprecated(forRemoval = true)
    PaymentInfo recordAdditionalPayment(Long rentalId, Money amount, PaymentMethod method, String operatorId);

    @Deprecated(forRemoval = true)
    boolean hasPrepayment(Long rentalId);

    @Deprecated(forRemoval = true)
    Optional<PaymentInfo> getPrepayment(Long rentalId);

    @Deprecated(forRemoval = true)
    List<PaymentInfo> getPayments(Long rentalId);

    HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost);

    SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                                @NonNull Money finalCost, @NonNull String operatorId) throws InsufficientHoldException, OverBudgetSettlementException;
}
