package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class AgreementPdfRenderingException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.pdf.rendering_failed";

    private static final String MESSAGE = "Failed to render the agreement PDF document";

    public AgreementPdfRenderingException(Throwable cause) {
        super(MESSAGE, ERROR_CODE);
        initCause(cause);
    }
}
