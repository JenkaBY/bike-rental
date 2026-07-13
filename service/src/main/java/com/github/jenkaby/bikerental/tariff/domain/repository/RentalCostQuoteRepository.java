package com.github.jenkaby.bikerental.tariff.domain.repository;

import com.github.jenkaby.bikerental.tariff.RentalCostQuote;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RentalCostQuoteRepository {

    RentalCostQuote save(RentalCostQuote quote);

    Optional<RentalCostQuote> findById(UUID quoteId);

    boolean markConsumed(UUID quoteId, Instant consumedAt);
}
