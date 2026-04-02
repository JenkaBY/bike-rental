package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.finance.application.mapper.PaymentToInfoMapper;
import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentsByRentalIdUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.RecordPaymentUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
class FinanceFacadeImpl implements FinanceFacade {

    private static final Predicate<Payment> IS_PREPAYMENT = p -> p.getPaymentType() == PaymentType.PREPAYMENT;

    private final RecordPaymentUseCase recordPaymentUseCase;
    private final GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase;
    private final PaymentToInfoMapper paymentToInfoMapper;
    private final RentalHoldUseCase rentalHoldUseCase;

    FinanceFacadeImpl(
            RecordPaymentUseCase recordPaymentUseCase,
            GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase,
            PaymentToInfoMapper paymentToInfoMapper,
            RentalHoldUseCase rentalHoldUseCase) {
        this.recordPaymentUseCase = recordPaymentUseCase;
        this.getPaymentsByRentalIdUseCase = getPaymentsByRentalIdUseCase;
        this.paymentToInfoMapper = paymentToInfoMapper;
        this.rentalHoldUseCase = rentalHoldUseCase;
    }

    @Override
    public PaymentInfo recordPrepayment(Long rentalId, Money amount, PaymentMethod method, String operatorId) {
        var command = new RecordPaymentUseCase.RecordPaymentCommand(
                rentalId,
                amount,
                PaymentType.PREPAYMENT,
                method,
                operatorId
        );
        Payment payment = recordPaymentUseCase.execute(command);
        return paymentToInfoMapper.toPaymentInfo(payment);
    }

    @Override
    public PaymentInfo recordAdditionalPayment(Long rentalId, Money amount, PaymentMethod method, String operatorId) {
        var command = new RecordPaymentUseCase.RecordPaymentCommand(
                rentalId,
                amount,
                PaymentType.ADDITIONAL_PAYMENT,
                method,
                operatorId
        );
        Payment payment = recordPaymentUseCase.execute(command);
        return paymentToInfoMapper.toPaymentInfo(payment);
    }

    @Override
    public boolean hasPrepayment(Long rentalId) {
        return getPaymentsByRentalIdUseCase.execute(rentalId).stream()
                .anyMatch(IS_PREPAYMENT);
    }

    @Override
    public Optional<PaymentInfo> getPrepayment(Long rentalId) {
        return getPaymentsByRentalIdUseCase.execute(rentalId).stream()
                .filter(IS_PREPAYMENT)
                .findFirst()
                .map(paymentToInfoMapper::toPaymentInfo);
    }

    @Override
    public List<PaymentInfo> getPayments(Long rentalId) {
        return getPaymentsByRentalIdUseCase.execute(rentalId).stream()
                .map(paymentToInfoMapper::toPaymentInfo)
                .toList();
    }

    @Override
    public HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost) {
        var command = new RentalHoldUseCase.RentalHoldCommand(customerRef, rentalRef, plannedCost);
        var result = rentalHoldUseCase.execute(command);
        return new HoldInfo(result.transactionRef(), result.recordedAt());
    }
}
