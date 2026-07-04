package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class ActiveAgreementTemplateNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.no_active";

    private static final String MESSAGE = "No active agreement template exists";

    public ActiveAgreementTemplateNotFoundException() {
        super(MESSAGE, ERROR_CODE);
    }
}
