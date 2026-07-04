package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class AgreementTemplateNotActivatableException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.not_activatable";

    private static final String MESSAGE_TEMPLATE = "Cannot activate agreement template in status %s. Only DRAFT templates are activatable";

    public AgreementTemplateNotActivatableException(AgreementTemplateStatus currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(currentStatus), ERROR_CODE, new Details(currentStatus));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(AgreementTemplateStatus currentStatus) {
    }
}
