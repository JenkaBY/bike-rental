package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;

import java.util.Optional;

public interface TariffV2Facade {

    Optional<TariffV2Info> findById(Long tariffId);

    RentalCostCalculationResult calculateRentalCostV2(RentalCostCalculationV2Command command);

    RentalCostQuote getQuote(QuoteRef quoteId) throws QuoteNotFoundException, QuoteExpiredException;

    RentalCostQuote consumeQuote(QuoteRef quoteId) throws QuoteNotFoundException, QuoteExpiredException, QuoteAlreadyConsumedException;
}
