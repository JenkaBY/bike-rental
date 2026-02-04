package com.github.jenkaby.bikerental.componenttest.steps.tariff;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableTariffRepository;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.TariffJpaEntity;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.repository.TariffJpaRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TariffDbSteps {

    private static final Comparator<TariffJpaEntity> DEFAULT_COMPARATOR = Comparator.comparing(TariffJpaEntity::getEquipmentTypeSlug)
            .thenComparing(TariffJpaEntity::getValidFrom);
    private final InsertableTariffRepository insertRepository;
    private final TariffJpaRepository jpaRepository;

    @Given("the following tariff record(s) exist(s) in db")
    public void theFollowingTariffsExist(List<TariffJpaEntity> entities) {
        log.debug("Inserting tariffs: {}", entities);
        insertRepository.insertAll(entities);
    }

    @Then("the following tariff record(s) was/were persisted in db")
    public void theFollowingTariffsWerePersisted(List<TariffJpaEntity> expected) {
        // find by name (or id if provided) in the database and compare
        Set<String> expectedNames = expected.stream()
                .map(TariffJpaEntity::getName)
                .collect(Collectors.toSet());

        var actualList = jpaRepository.findAll().stream()
                .filter(e -> expectedNames.contains(e.getName()))
                .sorted(DEFAULT_COMPARATOR)
                .toList();

        var expectedSorted = expected.stream()
                .sorted(DEFAULT_COMPARATOR)
                .toList();

        assertThat(actualList).zipSatisfy(expectedSorted, (actual, exp) -> {
            log.info("Comparing actual tariff: {} with expected: {}", actual, exp);
            var softly = new SoftAssertions();
            softly.assertThat(actual.getId()).as("Id").isNotNull();
            softly.assertThat(actual.getName()).as("Name").isEqualTo(exp.getName());
            softly.assertThat(actual.getDescription()).as("Description").isEqualTo(exp.getDescription());
            softly.assertThat(actual.getEquipmentTypeSlug()).as("Equipment type slug").isEqualTo(exp.getEquipmentTypeSlug());
            softly.assertThat(actual.getBasePrice()).as("Base price").isEqualTo(exp.getBasePrice());
            softly.assertThat(actual.getHalfHourPrice()).as("Half hour price").isEqualTo(exp.getHalfHourPrice());
            softly.assertThat(actual.getHourPrice()).as("Hour price").isEqualTo(exp.getHourPrice());
            softly.assertThat(actual.getDayPrice()).as("Day price").isEqualTo(exp.getDayPrice());
            softly.assertThat(actual.getHourDiscountedPrice()).as("Hour discounted price").isEqualTo(exp.getHourDiscountedPrice());
            softly.assertThat(actual.getValidFrom()).as("Valid from").isEqualTo(exp.getValidFrom());
            softly.assertThat(actual.getValidTo()).as("Valid to").isEqualTo(exp.getValidTo());
            softly.assertThat(actual.getStatus()).as("Status").isEqualTo(exp.getStatus());
            softly.assertAll();
        });
    }

    @Given("total tariff records in db is {int}")
    public void theFollowingTariffsExist(int expectedSize) {
        assertThat(jpaRepository.count()).as("Total tariff records in db").isEqualTo(expectedSize);
    }
}
