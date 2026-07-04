package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class AgreementTemplateNotActiveException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.not_active";

    private static final String MESSAGE_TEMPLATE = "Template %d is not the current active template. Active template is %d";

    public AgreementTemplateNotActiveException(Long requestedTemplateId, Long activeTemplateId) {
        super(MESSAGE_TEMPLATE.formatted(requestedTemplateId, activeTemplateId), ERROR_CODE,
                new Details(requestedTemplateId, activeTemplateId));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long requestedTemplateId, Long activeTemplateId) {
    }
}
