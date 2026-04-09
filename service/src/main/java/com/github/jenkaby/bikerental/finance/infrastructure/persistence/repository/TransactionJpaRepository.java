package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>, JpaSpecificationExecutor<TransactionJpaEntity> {

    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, UUID customerId);

    List<TransactionJpaEntity> findAllBySourceTypeAndSourceIdAndTransactionType(
            TransactionSourceType sourceType, String sourceId, TransactionType transactionType);
}
