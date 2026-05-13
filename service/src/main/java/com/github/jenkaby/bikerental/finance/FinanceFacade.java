package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;

public interface FinanceFacade {

    HoldInfo holdFunds(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                       @NonNull Money plannedCost, @NonNull String operatorId);

    boolean hasHold(RentalRef rentalRef);

    ReleaseHoldInfo releaseHold(@NonNull RentalRef rentalRef, @NonNull String operatorId);

    SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                                @NonNull Money finalCost, @NonNull String operatorId);
}
