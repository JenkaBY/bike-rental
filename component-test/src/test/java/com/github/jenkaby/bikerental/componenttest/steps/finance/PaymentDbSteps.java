package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertablePaymentRepository;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.PaymentJpaRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@RequiredArgsConstructor
@Slf4j
public class PaymentDbSteps {

    private static final Comparator<PaymentJpaEntity> DEFAULT_COMPARATOR = Comparator.comparing(PaymentJpaEntity::getId);
    private final ScenarioContext context;
    private final InsertablePaymentRepository insertable;
    private final PaymentJpaRepository jpaRepository;

    @Given("the following payment record(s) exist(s) in db")
    public void theFollowingTariffsExist(List<PaymentJpaEntity> entities) {
        log.debug("Inserting tariffs: {}", entities);
        insertable.insertAll(entities);
    }

    @Then("the following payment record(s) was/were persisted in db")
    public void theFollowingPaymentRecordWasPersistedInDb(List<PaymentJpaEntity> expected) {
        // find by name (or id if provided) in the database and compare
        Set<UUID> expectedIds = context.getPersistedIds();

        var actualList = jpaRepository.findAll().stream()
                .filter(e -> expectedIds.contains(e.getId()))
                .sorted(DEFAULT_COMPARATOR)
                .toList();

        var expectedSorted = expected.stream()
                .sorted(DEFAULT_COMPARATOR)
                .toList();

        assertThat(actualList).zipSatisfy(expectedSorted, (actual, exp) -> {
            log.info("Comparing actual payment: {} with expected: {}", actual, exp);
            assertSoftly(softly -> {
                softly.assertThat(actual.getId()).as("Id").isNotNull();
                if (exp.getId() != null) {
                    softly.assertThat(actual.getId()).as("Id equals to expected").isEqualTo(exp.getId());
                }
                softly.assertThat(actual.getRentalId()).as("Rental ID").isEqualTo(exp.getRentalId());
                softly.assertThat(actual.getAmount()).as("Amount").isEqualByComparingTo(exp.getAmount());
                softly.assertThat(actual.getPaymentType()).as("Payment type").isEqualTo(exp.getPaymentType());
                softly.assertThat(actual.getPaymentMethod()).as("Payment method").isEqualTo(exp.getPaymentMethod());
                softly.assertThat(actual.getCreatedAt()).as("Created at")
                        .isCloseTo(exp.getCreatedAt(), within(1, ChronoUnit.SECONDS));
                softly.assertThat(actual.getOperatorId()).as("Operator ID").isEqualTo(exp.getOperatorId());
//                This field is generated automatically upon creation. Cannot be predicted in advance.
                softly.assertThat(actual.getReceiptNumber()).as("Receipt number").isNotBlank();
            });
        });
    }

    @Given("total payment records in db is {int}")
    public void theFollowingPaymentsExist(int expectedSize) {
        assertThat(jpaRepository.count()).as("Total payments records in db").isEqualTo(expectedSize);
    }
}
