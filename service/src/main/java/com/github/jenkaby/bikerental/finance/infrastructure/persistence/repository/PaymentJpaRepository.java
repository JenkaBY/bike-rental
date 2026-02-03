package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    @Override
    Optional<PaymentJpaEntity> findById(UUID id);

    List<PaymentJpaEntity> findByRentalId(Long rentalId);

    boolean existsByReceiptNumber(String receiptNumber);
}
