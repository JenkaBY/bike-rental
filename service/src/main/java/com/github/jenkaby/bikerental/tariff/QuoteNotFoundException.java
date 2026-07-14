package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

import java.util.UUID;

/**
 * Thrown when a cost quote cannot be found. Part of the public API of the tariff module and may be thrown by
 * {@link TariffV2Facade#getQuote(QuoteRef)} / {@link TariffV2Facade#consumeQuote(QuoteRef)}.
 */
public class QuoteNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.quote.not_found";

    private static final String MESSAGE_TEMPLATE = "Cost quote '%s' not found";

    public QuoteNotFoundException(UUID quoteId) {
        super(MESSAGE_TEMPLATE.formatted(quoteId), ERROR_CODE, new Details(quoteId));
    }

    public Details getDetails() {
        return getParams().map(Details.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(UUID quoteId) {
    }
}
