package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

import java.time.Instant;
import java.util.UUID;

/**
 * Thrown when a cost quote has passed its expiry. Part of the public API of the tariff module and may be thrown by
 * {@link TariffV2Facade#getQuote(QuoteRef)} / {@link TariffV2Facade#consumeQuote(QuoteRef)}.
 */
public class QuoteExpiredException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.quote.expired";

    private static final String MESSAGE_TEMPLATE = "Cost quote '%s' expired at %s";

    public QuoteExpiredException(UUID quoteId, Instant expiresAt) {
        super(MESSAGE_TEMPLATE.formatted(quoteId, expiresAt), ERROR_CODE, new Details(quoteId, expiresAt));
    }

    public Details getDetails() {
        return getParams().map(Details.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(UUID quoteId, Instant expiresAt) {
    }
}
