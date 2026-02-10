package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class InsufficientPrepaymentException extends BikeRentalException {

    private final Long rentalId;

    public InsufficientPrepaymentException(Long rentalId, String message) {
        super(message);
        this.rentalId = rentalId;
    }

    public static InsufficientPrepaymentException forInsufficientPrepayment(Rental rental) {
        return rental.getEstimatedCost() == null
                ? estimatedCostNotSet(rental.getId())
                : amountBelowEstimatedCost(rental.getId());
    }

    public static InsufficientPrepaymentException estimatedCostNotSet(Long rentalId) {
        return new InsufficientPrepaymentException(rentalId,
                "Estimated cost must be calculated before recording prepayment");
    }

    public static InsufficientPrepaymentException amountBelowEstimatedCost(Long rentalId) {
        return new InsufficientPrepaymentException(rentalId,
                "Prepayment amount must be at least the estimated cost of the rental");
    }
}
