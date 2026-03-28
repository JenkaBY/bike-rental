package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, UUID customerId);
}
