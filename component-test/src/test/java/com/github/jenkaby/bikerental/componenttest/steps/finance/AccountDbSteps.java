package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableAccountRepository;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.WrapperAccountJpaRepository;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


@Slf4j
@RequiredArgsConstructor
public class AccountDbSteps {

    private static final Comparator<AccountJpaEntity> COMPARATOR = Comparator
            .comparing((AccountJpaEntity e) -> e.getId() != null ? e.getId().toString() : "")
            .thenComparing(e -> e.getCustomerId() != null ? e.getCustomerId().toString() : "")
            .thenComparing(e -> e.getAccountType() != null ? e.getAccountType().name() : "");
    private final InsertableAccountRepository insertRepository;
    private final WrapperAccountJpaRepository accountJpaRepository;

    @Given("the following account record(s) exist(s) in db")
    public void theFollowingAccountsExist(List<AccountJpaEntity> entities) {
        log.debug("Inserting accounts: {}", entities);
        insertRepository.insertAll(entities);
    }

    @Then("the following account record(s) was/were persisted in db")
    public void theFollowingAccountRecordWasPersistedInDb(List<AccountJpaEntity> expectedList) {
        var all = accountJpaRepository.findAllInitialized();

        var actualList = all.stream()
                .filter(actual -> expectedList.stream().anyMatch(exp -> matches(actual, exp)))
                .sorted(COMPARATOR)
                .toList();

        var expectedSorted = expectedList.stream().sorted(COMPARATOR).toList();

        assertThat(actualList).hasSize(expectedSorted.size());
        assertThat(actualList).zipSatisfy(expectedSorted, AccountDbSteps::assertSingle);
    }

    private static void assertSingle(AccountJpaEntity actual, AccountJpaEntity exp) {
        var softly = new SoftAssertions();
        softly.assertThat(actual.getId()).as("Id").isNotNull();
        if (exp.getId() != null) {
            softly.assertThat(actual.getId()).as("Id equals to expected")
                    .isEqualTo(exp.getId());
        }
        softly.assertThat(actual.getAccountType()).as("Account type")
                .isEqualTo(exp.getAccountType());

        softly.assertThat(actual.getCustomerId()).as("Customer id")
                .isEqualTo(exp.getCustomerId());
        if (exp.getAccountType() == AccountType.SYSTEM) {
            softly.assertThat(actual.getCreatedAt()).isNotNull();
        } else {
            softly.assertThat(actual.getCreatedAt()).as("Created at")
                    .isCloseTo(exp.getCreatedAt(), within(1, ChronoUnit.SECONDS));
        }

        softly.assertAll();
    }

    private boolean matches(AccountJpaEntity actual, AccountJpaEntity exp) {
        if (exp.getId() != null) {
            return Objects.equals(actual.getId(), exp.getId());
        }
        if (exp.getCustomerId() != null) {
            return Objects.equals(actual.getCustomerId(), exp.getCustomerId());
        }
        return Objects.equals(actual.getAccountType(), exp.getAccountType());
    }
}



