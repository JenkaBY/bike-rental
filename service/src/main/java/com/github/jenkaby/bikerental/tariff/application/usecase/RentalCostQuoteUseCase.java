package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;

import java.util.UUID;

public interface RentalCostQuoteUseCase {

    RentalCostQuote createQuote(RentalCostCalculationV2Command command);

    RentalCostQuote getQuote(UUID quoteId);

    RentalCostQuote consumeQuote(UUID quoteId);
}
