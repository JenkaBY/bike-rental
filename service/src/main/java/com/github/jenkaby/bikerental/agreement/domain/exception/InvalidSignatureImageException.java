package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class InvalidSignatureImageException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.invalid_signature_image";

    private static final String MESSAGE = "The submitted signature image is not valid base64-encoded PNG data";

    public InvalidSignatureImageException() {
        super(MESSAGE, ERROR_CODE);
    }
}
