package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
class FinanceFacadeImpl implements FinanceFacade {

    private final RentalHoldUseCase rentalHoldUseCase;
    private final SettleRentalUseCase settleRentalUseCase;
    private final TransactionRepository transactionRepository;

    FinanceFacadeImpl(
            RentalHoldUseCase rentalHoldUseCase,
            SettleRentalUseCase settleRentalUseCase,
            TransactionRepository transactionRepository) {
        this.rentalHoldUseCase = rentalHoldUseCase;
        this.settleRentalUseCase = settleRentalUseCase;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public HoldInfo holdFunds(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef, @NonNull Money plannedCost, @NonNull String operatorId) {
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
        return transactionRepository.existsByRentalRefAndType(rentalRef, TransactionType.HOLD);
    }
}
