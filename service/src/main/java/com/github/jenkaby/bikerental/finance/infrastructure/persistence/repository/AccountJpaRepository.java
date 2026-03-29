package com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByAccountType(AccountType accountType);

    @EntityGraph(attributePaths = {"subLedgers"})
    Optional<AccountJpaEntity> findByCustomerId(UUID customerId);
}
