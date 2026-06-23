package com.github.jenkaby.bikerental.componenttest.steps.identity;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableRepositoryEntityManagerDelegate;
import com.github.jenkaby.bikerental.identity.infrastructure.persistence.entity.UserJpaEntity;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class IdentityDbSteps {

    private final InsertableRepositoryEntityManagerDelegate<UserJpaEntity, UUID> userInserter;

    @Given("(a )user account(s) exist(s) in the database with the following data")
    public void userAccountsExist(List<UserJpaEntity> accounts) {
        log.debug("Seeding user accounts: {}", accounts);
        userInserter.insertAll(accounts);
    }
}
