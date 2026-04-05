package com.github.jenkaby.bikerental.shared.exception;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

@Getter
public class OverBudgetSettlementException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.OVER_BUDGET_SETTLEMENT;

    private static final String MESSAGE_TEMPLATE = "Settlement rejected: final cost %s exceeds available balance %s";

    public OverBudgetSettlementException(Money finalCost, Money availableAmount) {
        super(
                MESSAGE_TEMPLATE.formatted(finalCost, availableAmount),
                ERROR_CODE,
                new Details(finalCost, availableAmount)
        );
    }

    public Details getDetails() {
        return getParams().map(params -> (Details) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Money finalCost, Money availableAmount) {
    }
}
