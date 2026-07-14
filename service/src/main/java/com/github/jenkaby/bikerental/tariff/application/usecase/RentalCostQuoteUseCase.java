package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;

public interface RentalCostQuoteUseCase {

    RentalCostQuote createQuote(RentalCostCalculationV2Command command);

    RentalCostQuote getQuote(QuoteRef quoteId);

    RentalCostQuote consumeQuote(QuoteRef quoteId);

    void deleteQuote(QuoteRef quoteId);
}
