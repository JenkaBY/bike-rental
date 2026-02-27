package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.MessageStore;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class RentalCompletedEventSteps {

    private static final Function<RentalCompleted, Object> RENTAL_ID_EXTRACTOR = RentalCompleted::rentalId;

    private final ScenarioContext scenarioContext;
    private final MessageStore messageStore;

    @Then("the following rental completed event was published")
    public void theFollowingRentalCompletedEventWasPublished(RentalCompleted expected) {
        Set<Object> ids = Set.of(Long.parseLong(scenarioContext.getRequestedObjectId()));

        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<RentalCompleted> actualEvents = messageStore
                            .getEventsFor(RentalCompleted.class, ids, RENTAL_ID_EXTRACTOR);

                    assertThat(actualEvents)
                            .as("Number of published RentalCompleted events")
                            .hasSize(1);

                    assertRentalCompletedEvent(actualEvents.getFirst(), expected);
                });
    }

    private void assertRentalCompletedEvent(RentalCompleted actual, RentalCompleted expected) {
        log.info("Comparing RentalCompleted event actual: {} with expected: {}", actual, expected);
        var softly = new SoftAssertions();

        softly.assertThat(actual.rentalId())
                .as("Rental ID should not be null")
                .isNotNull();

        if (expected.rentalId() != null) {
            softly.assertThat(actual.rentalId())
                    .as("Rental ID")
                    .isEqualTo(expected.rentalId());
        }

        if (expected.equipmentId() != null) {
            softly.assertThat(actual.equipmentId())
                    .as("Equipment ID")
                    .isEqualTo(expected.equipmentId());
        }

        softly.assertThat(actual.returnTime())
                .as("Return time should not be null")
                .isNotNull();

        if (expected.returnTime() != null) {
            softly.assertThat(actual.returnTime())
                    .as("Return time")
                    .isCloseTo(expected.returnTime(), within(5, ChronoUnit.SECONDS));
        }

        if (expected.finalCost() != null) {
            softly.assertThat(actual.finalCost())
                    .as("Final cost")
                    .isEqualByComparingTo(expected.finalCost());
        } else {
            softly.assertThat(actual.finalCost())
                    .as("Final cost should not be null")
                    .isNotNull();
        }

        softly.assertAll();
    }
}

