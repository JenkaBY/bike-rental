package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class WrapperAccountJpaRepository {

    private final AccountJpaRepository delegate;

    @Transactional(readOnly = true)
    public List<AccountJpaEntity> findAllInitialized() {
        return delegate.findAll().stream()
                .peek(e -> e.getSubLedgers().forEach(SubLedgerJpaEntity::getId))
                .toList();
    }
}
