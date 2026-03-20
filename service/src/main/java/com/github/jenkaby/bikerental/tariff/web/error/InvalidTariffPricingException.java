package com.github.jenkaby.bikerental.tariff.web.error;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class InvalidTariffPricingException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.pricing.invalid";

    public InvalidTariffPricingException(String message, String errorCode) {
        super(message, errorCode);
    }
}
