package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.repository.PaymentRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.PaymentJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.PaymentJpaRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentJpaMapper mapper;

    PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository, PaymentJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toEntity(payment);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Payment> getForRental(Long rentalId) {
        return jpaRepository.findByRentalId(rentalId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByReceiptNumber(String receiptNumber) {
        return jpaRepository.existsByReceiptNumber(receiptNumber);
    }

    @Override
    public Payment get(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(Payment.class, id.toString()));
    }
}
