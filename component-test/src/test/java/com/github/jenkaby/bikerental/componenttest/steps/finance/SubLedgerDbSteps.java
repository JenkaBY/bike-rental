package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableSubLedgerRepository;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.SubLedgerJpaRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Slf4j
@RequiredArgsConstructor
public class SubLedgerDbSteps {

    private static final Comparator<SubLedgerJpaEntity> COMPARATOR = Comparator
            .comparing((SubLedgerJpaEntity e) -> e.getId() != null ? e.getId().toString() : "")
            .thenComparing(e -> e.getAccount() != null && e.getAccount().getId() != null ? e.getAccount().getId().toString() : "")
            .thenComparing(e -> e.getLedgerType() != null ? e.getLedgerType().name() : "");

    private final InsertableSubLedgerRepository insertRepository;
    private final SubLedgerJpaRepository subLedgerJpaRepository;

    @Given("the following sub-ledger record(s) exist(s) in db")
    public void theFollowingSubLedgersExist(List<SubLedgerJpaEntity> entities) {
        log.debug("Inserting sub-ledgers: {}", entities);
        insertRepository.insertAll(entities);
    }

    @Then("the following sub-ledger record(s) were/was persisted in db")
    public void theFollowingSubLedgerRecordsWerePersistedInDb(List<SubLedgerJpaEntity> expectedEntities) {
        var all = subLedgerJpaRepository.findAllInitialized();

        var actualList = all.stream()
                .filter(actual -> expectedEntities.stream()
                        .anyMatch(exp -> matches(actual, exp)))
                .sorted(COMPARATOR)
                .toList();

        var expectedSorted = expectedEntities.stream()
                .sorted(COMPARATOR)
                .toList();

        assertThat(actualList).hasSize(expectedSorted.size());

        assertThat(actualList).zipSatisfy(expectedSorted, SubLedgerDbSteps::compareSingleElement);
    }

    private static void compareSingleElement(SubLedgerJpaEntity actual, SubLedgerJpaEntity exp) {
        log.info("Actual {} expected {}", actual, exp);
        var softly = new SoftAssertions();
        softly.assertThat(actual.getId()).as("Id").isNotNull();
        if (exp.getId() != null) {
            softly.assertThat(actual.getId()).as("Id equals to expected").isEqualTo(exp.getId());
        }

        softly.assertThat(actual.getAccount().getId()).as("Account id").isEqualTo(exp.getAccount().getId());

        softly.assertThat(actual.getLedgerType()).as("Ledger type")
                .isEqualTo(exp.getLedgerType());
        softly.assertThat(actual.getBalance()).as("Balance")
                .isEqualByComparingTo(exp.getBalance());

        if (exp.getLedgerType().isSystemLedger()) {
            softly.assertThat(actual.getCreatedAt()).as("Created at").isNotNull();
        } else {
            if (exp.getUpdatedAt() == null) {
                softly.assertThat(actual.getUpdatedAt()).as("Updated at")
                        .isNotNull();
            } else {
                softly.assertThat(actual.getUpdatedAt()).as("Updated at")
                        .isCloseTo(exp.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
            }

            if (exp.getCreatedAt() == null) {
                softly.assertThat(actual.getCreatedAt()).as("Created at")
                        .isNotNull();
            } else {
                softly.assertThat(actual.getCreatedAt()).as("Created at")
                        .isCloseTo(exp.getCreatedAt(), within(1, ChronoUnit.SECONDS));
            }

        }
        softly.assertThat(actual.getVersion()).as("Version").isNotNull();
        softly.assertAll();
    }

    private boolean matches(SubLedgerJpaEntity actual, SubLedgerJpaEntity exp) {
        if (exp.getId() != null) {
            return Objects.equals(actual.getId(), exp.getId());
        }
        return Objects.equals(actual.getAccount().getId(), exp.getAccount().getId())
                && Objects.equals(actual.getLedgerType(), exp.getLedgerType());
    }

    @After("@ReinitializeSystemLedgers")
    public void theSystemLedgersHaveAmount() {
        subLedgerJpaRepository.findAllInitialized().stream()
                .filter(l -> l.getLedgerType().isSystemLedger())
                .peek(l -> l.setBalance(BigDecimal.ZERO))
                .forEach(subLedgerJpaRepository::save);
    }
}

