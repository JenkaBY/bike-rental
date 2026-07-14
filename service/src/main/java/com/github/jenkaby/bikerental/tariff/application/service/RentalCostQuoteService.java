package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import com.github.jenkaby.bikerental.tariff.QuoteAlreadyConsumedException;
import com.github.jenkaby.bikerental.tariff.QuoteExpiredException;
import com.github.jenkaby.bikerental.tariff.QuoteNotFoundException;
import com.github.jenkaby.bikerental.tariff.QuoteStatus;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import com.github.jenkaby.bikerental.tariff.application.config.QuoteProperties;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostQuoteUseCase;
import com.github.jenkaby.bikerental.tariff.domain.repository.RentalCostQuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class RentalCostQuoteService implements RentalCostQuoteUseCase {

    private final RentalCostCalculationV2UseCase calculationUseCase;
    private final RentalCostQuoteRepository quoteRepository;
    private final QuoteProperties quoteProperties;
    private final UuidGenerator uuidGenerator;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public RentalCostQuote createQuote(RentalCostCalculationV2Command command) {
        var result = calculationUseCase.execute(command);
        var quotedAt = now();
        var quote = new RentalCostQuote(
                uuidGenerator.generate(),
                quotedAt,
                quotedAt.plus(quoteProperties.quoteTtl()),
                QuoteStatus.ACTIVE,
                command,
                result);
        var saved = quoteRepository.save(quote);
        log.info("Created cost quote {} valid until {}", saved.quoteId(), saved.expiresAt());
        return saved;
    }

    @Override
    public RentalCostQuote getQuote(UUID quoteId) {
        var quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));
        if (quote.isExpired(now())) {
            throw new QuoteExpiredException(quoteId, quote.expiresAt());
        }
        return quote;
    }

    @Override
    @Transactional
    public RentalCostQuote consumeQuote(UUID quoteId) {
        var quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));
        var now = now();
        if (quote.isExpired(now)) {
            throw new QuoteExpiredException(quoteId, quote.expiresAt());
        }
        if (quote.isConsumed() || !quoteRepository.markConsumed(quoteId, now)) {
            throw new QuoteAlreadyConsumedException(quoteId);
        }
        log.info("Consumed cost quote {}", quoteId);
        return new RentalCostQuote(
                quote.quoteId(),
                quote.quotedAt(),
                quote.expiresAt(),
                QuoteStatus.CONSUMED,
                quote.inputs(),
                quote.result());
    }

    private Instant now() {
        return timeProvider.nowInstant();
    }
}
