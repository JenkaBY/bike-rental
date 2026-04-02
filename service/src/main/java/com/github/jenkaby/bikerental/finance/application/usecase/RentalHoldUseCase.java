package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Instant;

public interface RentalHoldUseCase {

    HoldResult execute(RentalHoldCommand command);

    record RentalHoldCommand(CustomerRef customerRef, RentalRef rentalRef, Money amount) {
    }

    record HoldResult(TransactionRef transactionRef, Instant recordedAt) {
    }
}
