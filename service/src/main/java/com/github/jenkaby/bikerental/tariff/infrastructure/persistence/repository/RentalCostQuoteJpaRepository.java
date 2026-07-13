package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.RentalCostQuoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface RentalCostQuoteJpaRepository extends JpaRepository<RentalCostQuoteJpaEntity, UUID> {

    @Modifying
    @Query("UPDATE RentalCostQuoteJpaEntity q SET q.status = 'CONSUMED', q.consumedAt = :consumedAt, " +
            "q.updatedAt = :consumedAt WHERE q.id = :id AND q.status = 'ACTIVE'")
    int markConsumed(@Param("id") UUID id, @Param("consumedAt") Instant consumedAt);
}
