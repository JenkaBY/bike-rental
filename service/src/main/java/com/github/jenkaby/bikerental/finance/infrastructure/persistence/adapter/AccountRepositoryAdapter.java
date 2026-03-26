package com.github.jenkaby.bikerental.finance.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper.AccountJpaMapper;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        var entity = mapper.toEntity(account);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Account getSystemAccount() {
        return jpaRepository.findByAccountType(AccountType.SYSTEM.name())
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, AccountType.SYSTEM.name()));
    }
}
