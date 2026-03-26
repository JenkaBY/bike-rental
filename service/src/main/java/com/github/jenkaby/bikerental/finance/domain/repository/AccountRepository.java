package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Account getSystemAccount();

    Optional<Account> findByCustomerId(CustomerRef customerRef);
}
