package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRecordJpaRepository extends JpaRepository<TransactionRecordJpaEntity, UUID> {

    @Transactional(readOnly = true)
    default List<TransactionRecordJpaEntity> findAllInitialized() {
        return findAll().stream()
                .peek(e -> e.getTransaction().getId())
                .toList();
    }
}
