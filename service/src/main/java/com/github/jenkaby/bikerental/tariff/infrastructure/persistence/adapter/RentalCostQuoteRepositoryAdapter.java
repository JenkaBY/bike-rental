package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import com.github.jenkaby.bikerental.tariff.domain.repository.RentalCostQuoteRepository;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.mapper.RentalCostQuoteJpaMapper;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository.RentalCostQuoteJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
class RentalCostQuoteRepositoryAdapter implements RentalCostQuoteRepository {

    private final RentalCostQuoteJpaRepository jpaRepository;
    private final RentalCostQuoteJpaMapper mapper;

    RentalCostQuoteRepositoryAdapter(RentalCostQuoteJpaRepository jpaRepository, RentalCostQuoteJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RentalCostQuote save(RentalCostQuote quote) {
        var entity = mapper.toEntity(quote);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<RentalCostQuote> findById(QuoteRef quoteId) {
        return jpaRepository.findById(quoteId.id()).map(mapper::toDomain);
    }

    @Override
    public boolean markConsumed(QuoteRef quoteId, Instant consumedAt) {
        return jpaRepository.markConsumed(quoteId.id(), consumedAt) > 0;
    }

    @Override
    public void deleteById(QuoteRef quoteId) {
        jpaRepository.deleteById(quoteId.id());
    }
}
