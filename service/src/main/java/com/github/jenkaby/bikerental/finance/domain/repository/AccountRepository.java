package com.github.jenkaby.bikerental.finance.domain.repository;

import com.github.jenkaby.bikerental.finance.domain.model.Account;

public interface AccountRepository {

    Account save(Account account);

    Account getSystemAccount();
}
