package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.GetCustomerAccountBalancesUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.repository.AccountRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class GetCustomerAccountBalancesService implements GetCustomerAccountBalancesUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerAccountBalances execute(UUID customerId) {
        log.debug("Fetching account balances for customer={}", customerId);
        var account = accountRepository.findByCustomerId(CustomerRef.of(customerId))
                .orElseThrow(() -> new ResourceNotFoundException(CustomerAccount.class, customerId));

        var wallet = account.getWallet();
        var hold = account.getOnHold();
        log.debug("Balances for customer={}: wallet={} hold={}",
                customerId, wallet.getBalance().amount(), hold.getBalance().amount());

        return new CustomerAccountBalances(
                wallet.getBalance().amount(),
                hold.getBalance().amount(),
                wallet.getUpdatedAt()
        );
    }
}
