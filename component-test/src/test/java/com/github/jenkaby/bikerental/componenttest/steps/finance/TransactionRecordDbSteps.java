package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.TransactionRecordJpaRepository;
// ...existing code...
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TransactionRecordDbSteps {

    private final TransactionRecordJpaRepository transactionRepository;


    @Then("there are/is only {int} transaction records in db")
    public void thereAreOnlyNumberOfTransactionRecordsInDb(int expected) {
        assertThat(transactionRepository.findAllInitialized()).hasSize(expected);
    }

    @Then("the following transaction record(s) were/was persisted in db")
    public void transactionRecordsWerePersistedInDb(List<TransactionRecordJpaEntity> expected) {
        var all = transactionRepository.findAllInitialized();
        log.info("All {}", all);
        var comparator = Comparator
                .comparing((TransactionRecordJpaEntity e) -> e.getLedgerType() != null ? e.getLedgerType().name() : "")
                .thenComparing(e -> e.getDirection() != null ? e.getDirection().name() : "")
                .thenComparing(e -> e.getAmount() != null ? e.getAmount().toPlainString() : "")
                .thenComparing(e -> e.getId() != null ? e.getId().toString() : "");

        var actualList = all.stream()
                .filter(actual -> expected.stream().anyMatch(exp -> matches(actual, exp)))
                .sorted(comparator)
                .toList();

        var expectedSorted = expected.stream().sorted(comparator).toList();

        assertThat(actualList).hasSize(expectedSorted.size());
        assertThat(actualList).zipSatisfy(expectedSorted, TransactionRecordDbSteps::compareSingleEntry);
    }

    private static void compareSingleEntry(TransactionRecordJpaEntity actual, TransactionRecordJpaEntity exp) {
        log.info("Actual {} expected {}", actual, exp);
        var softly = new SoftAssertions();

        softly.assertThat(actual.getId()).as("Id").isNotNull();
        if (exp.getId() != null) {
            softly.assertThat(actual.getId()).as("Id equals to expected").isEqualTo(exp.getId());
        }

        if (exp.getTransaction() != null && exp.getTransaction().getId() != null) {
            softly.assertThat(actual.getTransaction().getId())
                    .as("Transaction id")
                    .isEqualTo(exp.getTransaction().getId());
        } else {
            softly.assertThat(actual.getTransaction().getId()).as("Transaction id").isNotNull();
        }

        if (exp.getSubLedgerRef() != null) {
            softly.assertThat(actual.getSubLedgerRef()).as("SubLedgerRef")
                    .isEqualTo(exp.getSubLedgerRef());
        }

        softly.assertThat(actual.getLedgerType()).as("Ledger type")
                .isEqualTo(exp.getLedgerType());
        softly.assertThat(actual.getDirection()).as("Direction")
                .isEqualTo(exp.getDirection());
        softly.assertThat(actual.getAmount()).as("Amount")
                .isEqualByComparingTo(exp.getAmount());

        softly.assertAll();
    }

    private static boolean matches(TransactionRecordJpaEntity actual, TransactionRecordJpaEntity exp) {
        if (exp.getId() != null) {
            return Objects.equals(actual.getId(), exp.getId());
        }
        if (exp.getTransaction() != null && exp.getTransaction().getId() != null) {
            return Objects.equals(actual.getTransaction().getId(), exp.getTransaction().getId())
                    && Objects.equals(actual.getLedgerType(), exp.getLedgerType())
                    && Objects.equals(actual.getSubLedgerRef(), exp.getSubLedgerRef());
        }
        return Objects.equals(actual.getLedgerType(), exp.getLedgerType())
                && Objects.equals(actual.getSubLedgerRef(), exp.getSubLedgerRef());
    }
}
