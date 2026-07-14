package com.github.jenkaby.bikerental.tariff.domain.repository;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;

import java.time.Instant;
import java.util.Optional;

public interface RentalCostQuoteRepository {

    RentalCostQuote save(RentalCostQuote quote);

    Optional<RentalCostQuote> findById(QuoteRef quoteId);

    boolean markConsumed(QuoteRef quoteId, Instant consumedAt);

    void deleteById(QuoteRef quoteId);
}
