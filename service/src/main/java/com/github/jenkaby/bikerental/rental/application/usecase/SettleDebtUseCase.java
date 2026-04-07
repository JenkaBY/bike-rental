package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;

public interface SettleDebtUseCase {

    SettleDebtResult execute(SettleDebtCommand command);

    record SettleDebtCommand(
            CustomerRef customerRef,
            RentalRef rentalRef,
            String operatorId
    ) {
    }

    record SettleDebtResult(boolean settled) {

        public static SettleDebtResult success() {
            return new SettleDebtResult(true);
        }

        public static SettleDebtResult failure() {
            return new SettleDebtResult(false);
        }
    }
}
