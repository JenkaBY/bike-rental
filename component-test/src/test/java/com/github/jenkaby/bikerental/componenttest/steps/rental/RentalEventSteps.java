package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.MessageStore;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class RentalEventSteps {

    private static final Function<RentalCreated, Object> RENTAL_ID_EXTRACTOR = RentalCreated::rentalId;
    private static final Function<RentalStarted, Object> RENTAL_STARTED_ID_EXTRACTOR = RentalStarted::rentalId;
    private static final Comparator<RentalCreated> RENTAL_CREATED_COMPARATOR = Comparator.comparing(RentalCreated::rentalId);
    private static final Comparator<RentalStarted> RENTAL_STARTED_COMPARATOR = Comparator.comparing(RentalStarted::rentalId);
    private final ScenarioContext scenarioContext;
    private final MessageStore messageStore;

    @Then("the following rental created event was published")
    public void theFollowingRentalCreatedEventWasPublished(RentalCreated expected) {
        Set<Object> ids = Set.of(Long.parseLong(scenarioContext.getRequestedObjectId()));

        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<RentalCreated> actualEvents = messageStore.getEventsFor(RentalCreated.class, ids, RENTAL_ID_EXTRACTOR)
                            .stream()
                            .sorted(RENTAL_CREATED_COMPARATOR)
                            .toList();

                    assertThat(actualEvents.size())
                            .as("Number of published RentalCreated events")
                            .isEqualTo(1);


                    assertRentalCreatedEvent(actualEvents.getFirst(), expected);
                });
    }

    @Then("the following rental started event was published")
    public void theFollowingRentalStartedEventWasPublished(RentalStarted expected) {
        Set<Object> ids = Set.of(Long.parseLong(scenarioContext.getRequestedObjectId()));

        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<RentalStarted> actualEvents = messageStore.getEventsFor(RentalStarted.class, ids, RENTAL_STARTED_ID_EXTRACTOR)
                            .stream()
                            .sorted(RENTAL_STARTED_COMPARATOR)
                            .toList();

                    assertThat(actualEvents.size())
                            .as("Number of published RentalStarted events")
                            .isEqualTo(1);

                    assertRentalStartedEvent(actualEvents.getFirst(), expected);
                });
    }

    private void assertRentalCreatedEvent(RentalCreated actual, RentalCreated expected) {
        log.info("Comparing RentalCreated event actual: {} with expected: {}", actual, expected);
        SoftAssertions softly = new SoftAssertions();

        if (expected.rentalId() != null) {
            softly.assertThat(actual.rentalId())
                    .as("Rental ID")
                    .isEqualTo(expected.rentalId());
        } else {
            softly.assertThat(actual.rentalId())
                    .as("Rental ID should not be null")
                    .isNotNull();
        }

        if (expected.customerId() != null) {
            softly.assertThat(actual.customerId())
                    .as("Customer ID")
                    .isEqualTo(expected.customerId());
        }

        if (expected.status() != null) {
            softly.assertThat(actual.status())
                    .as("Status")
                    .isEqualTo(expected.status());
        }

        if (expected.createdAt() != null) {
            softly.assertThat(actual.createdAt())
                    .as("Created at")
                    .isCloseTo(expected.createdAt(), within(Duration.ofSeconds(5)));
        } else {
            softly.assertThat(actual.createdAt())
                    .as("Created at should not be null")
                    .isNotNull();
        }

        softly.assertAll();
    }

    private void assertRentalStartedEvent(RentalStarted actual, RentalStarted expected) {
        log.info("Comparing RentalStarted event actual: {} with expected: {}", actual, expected);
        SoftAssertions softly = new SoftAssertions();

        if (expected.rentalId() != null) {
            softly.assertThat(actual.rentalId())
                    .as("Rental ID")
                    .isEqualTo(expected.rentalId());
        } else {
            softly.assertThat(actual.rentalId())
                    .as("Rental ID should not be null")
                    .isNotNull();
        }

        if (expected.customerId() != null) {
            softly.assertThat(actual.customerId())
                    .as("Customer ID")
                    .isEqualTo(expected.customerId());
        }

        if (expected.equipmentId() != null) {
            softly.assertThat(actual.equipmentId())
                    .as("Equipment ID")
                    .isEqualTo(expected.equipmentId());
        }

        if (expected.startedAt() != null) {
            // startedAt is always validated as "now()" in feature files, so just check it's recent
            softly.assertThat(actual.startedAt())
                    .as("Started at")
                    .isCloseTo(expected.startedAt(), within(Duration.ofSeconds(5)));
        } else {
            softly.assertThat(actual.startedAt())
                    .as("Started at should not be null")
                    .isNotNull();
        }

        if (expected.expectedReturnAt() != null) {
            softly.assertThat(actual.expectedReturnAt())
                    .as("Expected return at")
                    .isCloseTo(expected.expectedReturnAt(), within(Duration.ofSeconds(5)));
        } else if (actual.startedAt() != null && expected.startedAt() != null) {
            // If we have startedAt, expectedReturnAt should be calculated
            softly.assertThat(actual.expectedReturnAt())
                    .as("Expected return at should not be null")
                    .isNotNull();
        }

        softly.assertAll();
    }
}
