package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableRentalEquipmentRepository;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableRentalRepository;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.RentalEquipmentTestJpaRepository;
import com.github.jenkaby.bikerental.componenttest.config.db.repository.RentalJpaRepositoryWrapper;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class RentalDbSteps {

    private static final Comparator<RentalEquipmentJpaEntity> BY_EQUIPMENT_UID = Comparator.comparing(RentalEquipmentJpaEntity::getEquipmentUid);
    private static final Comparator<RentalJpaEntity> RENTAL_COMPARATOR = Comparator.comparing(RentalJpaEntity::getId);

    private final InsertableRentalRepository rentalRepository;
    private final InsertableRentalEquipmentRepository rentalEquipmentsRepository;
    private final RentalEquipmentTestJpaRepository rentalEquipmentsJpaRepository;
    private final RentalJpaRepositoryWrapper rentalJpaRepository;
    private final ScenarioContext scenarioContext;

    @Given("(a )rental(s) exist(s) in the database with the following data")
    public void aRentalExistsInTheDatabaseWithTheFollowingData(List<RentalJpaEntity> rentals) {
        log.debug("Creating rentals in database: {}", rentals);

        rentalRepository.insertAll(rentals);
    }

    @Given("a single rental exists in the database with the following data")
    public void aSingleRentalExistsInTheDatabaseWithTheFollowingData(RentalJpaEntity rental) {
        log.debug("Creating rental in database: {}", rental);

        var insert = rentalRepository.insert(rental);
        scenarioContext.setRequestedObjectId(insert.getId().toString());
    }

    @Given("(a )rental equipment(s) exist(s) in the database with the following data")
    public void aRentalEquipmentsExistInTheDatabaseWithTheFollowingData(List<RentalEquipmentJpaEntity> equipments) {
        log.debug("Creating rental equipments in database: {}", equipments);
        rentalEquipmentsRepository.insertAll(equipments);
    }

    @Then("(a )rental(s) was/were persisted in database")
    public void aRentalsWerePersistedInDatabase(List<RentalJpaEntity> expected) {
        await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertRentalsPersisted(expected));
    }

    private void assertRentalsPersisted(List<RentalJpaEntity> expected) {
        List<RentalJpaEntity> actualList = rentalJpaRepository.findAll();

        var sortedActual = actualList.stream()
                .sorted(RENTAL_COMPARATOR)
                .toList();

        var sortedExpected = expected.stream()
                .sorted(RENTAL_COMPARATOR)
                .toList();

        assertThat(sortedActual)
                .as("Sizes are matched")
                .hasSize(sortedExpected.size());

        assertThat(sortedActual).zipSatisfy(sortedExpected, (actual, exp) -> {
            log.info("Comparing actual rental: {}", actual);
            log.info("Comparing expected rental: {}", exp);

            var softly = new SoftAssertions();

            if (exp.getId() != null) {
                softly.assertThat(actual.getId()).as("Id").isEqualTo(exp.getId());
            } else {
                softly.assertThat(actual.getId()).as("Id").isNotNull();
            }

            if (exp.getCustomerId() != null) {
                softly.assertThat(actual.getCustomerId()).as("Customer id")
                        .isEqualTo(exp.getCustomerId());
            }

            if (exp.getStatus() != null) {
                softly.assertThat(actual.getStatus()).as("Status")
                        .isEqualTo(exp.getStatus());
            }

            if (exp.getStartedAt() != null) {
                softly.assertThat(actual.getStartedAt()).as("Started at")
                        .isEqualTo(exp.getStartedAt());
            }
            if (exp.getExpectedReturnAt() != null) {
                softly.assertThat(actual.getExpectedReturnAt()).as("Expected return at")
                        .isEqualTo(exp.getExpectedReturnAt());
            }
            if (exp.getActualReturnAt() != null) {
                softly.assertThat(actual.getActualReturnAt()).as("Actual return at")
                        .isEqualTo(exp.getActualReturnAt());
            }

            if (exp.getPlannedDurationMinutes() != null) {
                softly.assertThat(actual.getPlannedDurationMinutes()).as("Planned duration")
                        .isEqualTo(exp.getPlannedDurationMinutes());
            }
            if (exp.getActualDurationMinutes() != null) {
                softly.assertThat(actual.getActualDurationMinutes()).as("Actual duration")
                        .isEqualTo(exp.getActualDurationMinutes());
            }

            if (exp.getCreatedAt() != null) {
                softly.assertThat(actual.getCreatedAt()).as("Created at")
                        .isCloseTo(exp.getCreatedAt(), within(1, ChronoUnit.SECONDS));
            }
            if (exp.getUpdatedAt() != null) {
                softly.assertThat(actual.getUpdatedAt()).as("Updated at")
                        .isCloseTo(exp.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
            }
            if (exp.getSpecialTariffId() != null) {
                softly.assertThat(actual.getSpecialTariffId()).as("Special tariff ID")
                        .isEqualTo(exp.getSpecialTariffId());
            }
            if (exp.getSpecialPrice() != null) {
                softly.assertThat(actual.getSpecialPrice()).as("Special price")
                        .isEqualByComparingTo(exp.getSpecialPrice());
            }
            if (exp.getDiscountPercent() != null) {
                softly.assertThat(actual.getDiscountPercent()).as("Discount percent")
                        .isEqualTo(exp.getDiscountPercent());
            }
            softly.assertAll();
        });
    }

    @Then("(a )rental equipment(s) were/was persisted in database")
    public void aRentalEquipmentsWerePersistedInDatabase(List<RentalEquipmentJpaEntity> expected) {
        expected.forEach(e ->
                {
                    if (e.getRental().getId() == null) {
                        e.setRental(RentalJpaEntity.builder()
                                .id(Long.valueOf(scenarioContext.getRequestedObjectId()))
                                .build());
                    }
                }
        );
        await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertRentalEquipmentsPersisted(expected));
    }

    @Then("there's/there're {int} rental equipment(s) in database")
    public void aRentalEquipmentsWerePersistedInDatabase(int expectedSize) {
        assertThat(rentalEquipmentsJpaRepository.findAll().size()).isEqualTo(expectedSize);
    }

    private void assertRentalEquipmentsPersisted(List<RentalEquipmentJpaEntity> expected) {
//        log.info("Verifying rental equipments persisted in database. Expected: {}", expected);
        Set<String> uids = expected.stream()
                .map(RentalEquipmentJpaEntity::getEquipmentUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var sortedActual = rentalEquipmentsJpaRepository.findAll().stream()
                .filter(a -> uids.contains(a.getEquipmentUid()))
                .sorted(BY_EQUIPMENT_UID)
                .toList();

        var sortedExpected = expected.stream()
                .sorted(BY_EQUIPMENT_UID)
                .toList();

        assertThat(sortedActual)
                .as("Sizes are matched")
                .hasSize(sortedExpected.size());

        assertThat(sortedActual).zipSatisfy(sortedExpected, (actual, exp) -> {
            log.info("Comparing actual rental equipment: {}", actual);
            log.info("Comparing expected rental equipment: {}", exp);

            var softly = new SoftAssertions();
            if (exp.getId() != null) {
                softly.assertThat(actual.getId()).as("Id").isEqualTo(exp.getId());
            } else {
                softly.assertThat(actual.getId()).as("Id").isNotNull();
            }

            if (exp.getRental() != null && exp.getRental().getId() != null) {
                softly.assertThat(actual.getRental()).as("Rental reference").isNotNull();
                softly.assertThat(actual.getRental().getId()).as("Rental id")
                        .isEqualTo(exp.getRental().getId());
            }

            softly.assertThat(actual.getEquipmentId()).as("Equipment ID").isEqualTo(exp.getEquipmentId());
            softly.assertThat(actual.getEquipmentUid()).as("Equipment UID").isEqualTo(exp.getEquipmentUid());
            if (exp.getTariffId() != null) {
                softly.assertThat(actual.getTariffId()).as("Tariff ID").isEqualTo(exp.getTariffId());
            }
            softly.assertThat(actual.getStatus()).as("Status").isEqualTo(exp.getStatus());

            if (exp.getStartedAt() != null) {
                softly.assertThat(actual.getStartedAt()).as("Started at")
                        .isEqualTo(exp.getStartedAt());
            }
            if (exp.getExpectedReturnAt() != null) {
                softly.assertThat(actual.getExpectedReturnAt()).as("Expected return at")
                        .isEqualTo(exp.getExpectedReturnAt());
            }
            if (exp.getActualReturnAt() != null) {
                softly.assertThat(actual.getActualReturnAt()).as("Actual return at")
                        .isEqualTo(exp.getActualReturnAt());
            }

            if (exp.getEstimatedCost() != null) {
                softly.assertThat(actual.getEstimatedCost()).as("Estimated cost")
                        .isEqualByComparingTo(exp.getEstimatedCost());
            }
            if (exp.getFinalCost() != null) {
                softly.assertThat(actual.getFinalCost()).as("Final cost")
                        .isEqualByComparingTo(exp.getFinalCost());
            }

            if (exp.getCreatedAt() != null) {
                softly.assertThat(actual.getCreatedAt()).as("Created at")
                        .isCloseTo(exp.getCreatedAt(), within(1, ChronoUnit.SECONDS));
            }
            if (exp.getUpdatedAt() != null) {
                softly.assertThat(actual.getUpdatedAt()).as("Updated at")
                        .isCloseTo(exp.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
            }

            softly.assertAll();
        });
    }
}
