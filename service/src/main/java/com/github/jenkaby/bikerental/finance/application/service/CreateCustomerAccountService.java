package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.CreateCustomerAccountUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
class CreateCustomerAccountService implements CreateCustomerAccountUseCase {

    private final AccountRepository accountRepository;
    private final UuidGenerator uuidGenerator;

    CreateCustomerAccountService(AccountRepository accountRepository, UuidGenerator uuidGenerator) {
        this.accountRepository = accountRepository;
        this.uuidGenerator = uuidGenerator;
    }

    @Override
    @Transactional
    public void execute(UUID customerId) {
        accountRepository.findByCustomerId(CustomerRef.of(customerId))
                .ifPresent(existing -> {
                    throw new ResourceConflictException(Account.class, customerId.toString());
                });

        var wallet = SubLedger.builder()
                .id(uuidGenerator.generate())
                .ledgerType(LedgerType.CUSTOMER_WALLET)
                .balance(BigDecimal.ZERO)
                .build();

        var hold = SubLedger.builder()
                .id(uuidGenerator.generate())
                .ledgerType(LedgerType.CUSTOMER_HOLD)
                .balance(BigDecimal.ZERO)
                .build();

        var account = Account.builder()
                .id(uuidGenerator.generate())
                .accountType(AccountType.CUSTOMER)
                .customerRef(CustomerRef.of(customerId))
                .subLedgers(List.of(wallet, hold))
                .build();

        accountRepository.save(account);
    }
}
