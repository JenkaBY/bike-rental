package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>, JpaSpecificationExecutor<TransactionJpaEntity> {

    @EntityGraph(attributePaths = "records")
    Optional<TransactionJpaEntity> findWithRecordsById(UUID id);

    Optional<TransactionJpaEntity> findByIdempotencyKeyAndCustomerId(UUID idempotencyKey, UUID customerId);

    Optional<TransactionJpaEntity> findByIdempotencyKey(UUID idempotencyKey);

    List<TransactionJpaEntity> findAllBySourceTypeAndSourceIdAndTransactionTypeIn(
            TransactionSourceType sourceType, String sourceId, Set<TransactionType> transactionTypes);

    @Query(value = """
            SELECT DISTINCT ON (r.ledger_type)
                            r.ledger_type AS ledgerType,
                            r.running_balance AS runningBalance
            FROM finance_transaction_records r
            JOIN finance_transactions t ON t.id = r.transaction_id
            WHERE t.customer_id = :customerId
              AND r.ledger_type IN ('CUSTOMER_WALLET', 'CUSTOMER_HOLD')
              AND t.recorded_at < :before
              AND r.running_balance IS NOT NULL
            ORDER BY r.ledger_type, t.recorded_at DESC, r.id DESC
            """, nativeQuery = true)
    List<LedgerBalanceProjection> findLatestCustomerBucketBalancesBefore(@Param("customerId") UUID customerId,
                                                                         @Param("before") Instant before);
}
