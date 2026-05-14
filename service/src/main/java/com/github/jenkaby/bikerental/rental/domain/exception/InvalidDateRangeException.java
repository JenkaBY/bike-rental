package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

import java.time.LocalDate;

public class InvalidDateRangeException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.date_range.invalid";

    private static final String MESSAGE_TEMPLATE = "'from' date (%s) must not be after 'to' date (%s)";

    public InvalidDateRangeException(LocalDate from, LocalDate to) {
        super(MESSAGE_TEMPLATE.formatted(from, to), ERROR_CODE);
    }
}
