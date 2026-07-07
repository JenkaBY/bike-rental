package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.finance.application.service.RentalHoldBalanceCalculator;
import com.github.jenkaby.bikerental.finance.application.usecase.ReleaseHoldUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.shared.domain.ActualRentalRef;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
class FinanceFacadeImpl implements FinanceFacade {

    private final RentalHoldUseCase rentalHoldUseCase;
    private final ReleaseHoldUseCase releaseHoldUseCase;
    private final SettleRentalUseCase settleRentalUseCase;
    private final RentalHoldBalanceCalculator holdBalanceCalculator;

    FinanceFacadeImpl(
            RentalHoldUseCase rentalHoldUseCase, ReleaseHoldUseCase releaseHoldUseCase,
            SettleRentalUseCase settleRentalUseCase,
            RentalHoldBalanceCalculator holdBalanceCalculator) {
        this.rentalHoldUseCase = rentalHoldUseCase;
        this.releaseHoldUseCase = releaseHoldUseCase;
        this.settleRentalUseCase = settleRentalUseCase;
        this.holdBalanceCalculator = holdBalanceCalculator;
    }

    @Override
    public HoldInfo holdFunds(@NonNull CustomerRef customerRef, @NonNull ActualRentalRef rentalRef, @NonNull Money plannedCost, @NonNull String operatorId) {
        var command = new RentalHoldUseCase.RentalHoldCommand(customerRef, rentalRef, plannedCost, operatorId);
        var result = rentalHoldUseCase.execute(command);
        return new HoldInfo(result.transactionRef(), result.recordedAt());
    }

    @Override
    public SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                                       @NonNull Money finalCost, @NonNull String operatorId) {
        var command = new SettleRentalUseCase.SettleRentalCommand(customerRef, rentalRef, finalCost, operatorId);
        var result = settleRentalUseCase.execute(command);
        return new SettlementInfo(result.captureTransactionRefs(), result.releaseTransactionRef(), result.recordedAt());
    }

    @Override
    public boolean hasHold(RentalRef rentalRef) {
        return holdBalanceCalculator.netActiveHold(rentalRef).isPositive();
    }

    @Override
    public ReleaseHoldInfo releaseHold(@NonNull ActualRentalRef rentalRef, @NonNull String operatorId) {
        var result = this.releaseHoldUseCase.execute(new ReleaseHoldUseCase.ReleaseHoldCommand(rentalRef, operatorId));
        return new ReleaseHoldInfo(result.transactionRef(), result.recordedAt());
    }
}
