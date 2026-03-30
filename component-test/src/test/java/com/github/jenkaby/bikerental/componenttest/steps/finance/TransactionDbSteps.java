package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.WrapperTransactionJpaRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
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
public class TransactionDbSteps {

    private static final Comparator<TransactionJpaEntity> COMPARATOR = Comparator
            .comparing((TransactionJpaEntity e) -> e.getId() != null ? e.getId().toString() : "")
            .thenComparing(e -> e.getCustomerId() != null ? e.getCustomerId().toString() : "")
            .thenComparing(e -> e.getTransactionType() != null ? e.getTransactionType().name() : "");

    private final WrapperTransactionJpaRepository transactionJpaRepository;

    @Then("there are/is only {int} transactions in db")
    public void thereAreOnlyNumberOfTransactionRecordsInDb(int expected) {
        assertThat(transactionJpaRepository.findAllInitialized()).hasSize(expected);
    }

    @Then("the following transaction(s) were/was persisted in db")
    public void transactionsWerePersistedInDb(List<TransactionJpaEntity> expected) {
        var all = transactionJpaRepository.findAllInitialized();

        var actualList = all.stream()
                .filter(actual -> expected.stream().anyMatch(exp -> matches(actual, exp)))
                .sorted(COMPARATOR)
                .toList();

        var expectedSorted = expected.stream().sorted(COMPARATOR).toList();

        assertThat(actualList).hasSize(expectedSorted.size());
        assertThat(actualList).zipSatisfy(expectedSorted, TransactionDbSteps::compareSingleElement);
    }

    private static void compareSingleElement(TransactionJpaEntity actual, TransactionJpaEntity exp) {
        log.info("Actual {} expected {}", actual, exp);
        var softly = new SoftAssertions();

        softly.assertThat(actual.getId()).as("Id").isNotNull();
        if (exp.getId() != null) {
            softly.assertThat(actual.getId()).as("Id equals to expected").isEqualTo(exp.getId());
        }

        softly.assertThat(actual.getTransactionType()).as("Transaction type")
                .isEqualTo(exp.getTransactionType());

        softly.assertThat(actual.getPaymentMethod()).as("Payment method")
                .isEqualTo(exp.getPaymentMethod());

        softly.assertThat(actual.getAmount()).as("Amount")
                .isEqualByComparingTo(exp.getAmount());

        softly.assertThat(actual.getCustomerId()).as("Customer id")
                .isEqualTo(exp.getCustomerId());

        softly.assertThat(actual.getOperatorId()).as("Operator id")
                .isEqualTo(exp.getOperatorId());

        if (exp.getRecordedAt() == null) {
            softly.assertThat(actual.getRecordedAt()).as("Recorded at").isNotNull();
        } else {
            softly.assertThat(actual.getRecordedAt()).as("Recorded at")
                    .isCloseTo(exp.getRecordedAt(), within(1, ChronoUnit.SECONDS));
        }
        softly.assertThat(actual.getRecords()).as("Records").isNotEmpty();
        if (exp.getReason() != null) {
            softly.assertThat(actual.getReason()).as("Reason").isEqualTo(exp.getReason());
        }
        // validate records is made in separate step
        softly.assertAll();
    }

    private boolean matches(TransactionJpaEntity actual, TransactionJpaEntity exp) {
        if (exp.getId() != null) {
            return Objects.equals(actual.getId(), exp.getId());
        }
        if (exp.getCustomerId() != null) {
            return Objects.equals(actual.getCustomerId(), exp.getCustomerId())
                    && Objects.equals(actual.getTransactionType(), exp.getTransactionType());
        }
        return Objects.equals(actual.getTransactionType(), exp.getTransactionType());
    }
}
