package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
import com.github.jenkaby.bikerental.finance.domain.model.SystemAccount;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    SystemAccount getSystemAccount();

    Optional<CustomerAccount> findByCustomerId(CustomerRef customerRef);
}
