package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.SystemAccount;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.AccountJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountJpaMapper mapper;

    AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Account save(Account account) {
        AccountJpaEntity entity = switch (account) {
            case CustomerAccount ca -> mapper.toEntity(ca);
            case SystemAccount sa -> mapper.toEntity(sa);
            default -> throw new IllegalStateException("Unknown account type: " + account.getClass().getSimpleName());
        };
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public SystemAccount getSystemAccount() {
        return jpaRepository.findByAccountType(AccountType.SYSTEM)
                .map(mapper::toSystemAccountDomain)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, AccountType.SYSTEM.name()));
    }

    @Override
    public Optional<CustomerAccount> findByCustomerId(CustomerRef customerRef) {
        return jpaRepository.findByCustomerId(customerRef.id())
                .map(mapper::toCustomerAccountDomain);
    }

    private Account toDomain(AccountJpaEntity entity) {
        return entity.getAccountType() == AccountType.CUSTOMER
                ? mapper.toCustomerAccountDomain(entity)
                : mapper.toSystemAccountDomain(entity);
    }
}
