package com.github.jenkaby.bikerental.tariff.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class InvalidSpecialPriceException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.special.price_invalid";
    private static final String MESSAGE = "specialPrice is required and must be >= 0 when specialTariffId is set";

    public InvalidSpecialPriceException() {
        super(MESSAGE, ERROR_CODE);
    }
}

