package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubLedgerJpaRepository extends JpaRepository<SubLedgerJpaEntity, UUID> {

    @Transactional(readOnly = true)
    default List<SubLedgerJpaEntity> findAllInitialized() {
        return findAll().stream()
                .peek(e -> e.getAccount().getId())
                .toList();
    }
}
