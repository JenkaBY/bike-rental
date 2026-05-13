package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.MessageStore;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class RentalCancelledEventSteps {

    private static final Function<RentalCancelled, Object> RENTAL_ID_EXTRACTOR = RentalCancelled::rentalId;

    private final ScenarioContext scenarioContext;
    private final MessageStore messageStore;

    @Then("the following rental cancelled event was published")
    public void theFollowingRentalCancelledEventWasPublished(RentalCancelled expected) {
        Set<Object> ids = Set.of(Long.parseLong(scenarioContext.getRequestedObjectId()));

        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<RentalCancelled> actualEvents = messageStore
                            .getEventsFor(RentalCancelled.class, ids, RENTAL_ID_EXTRACTOR);

                    assertThat(actualEvents)
                            .as("Number of published RentalCancelled events")
                            .hasSize(1);

                    assertRentalCancelledEvent(actualEvents.getFirst(), expected);
                });
    }

    private void assertRentalCancelledEvent(RentalCancelled actual, RentalCancelled expected) {
        log.info("Comparing RentalCancelled event actual: {} with expected: {}", actual, expected);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.rentalId()).as("rentalId").isEqualTo(expected.rentalId());
        softly.assertThat(actual.customerId()).as("customerId").isEqualTo(expected.customerId());
        softly.assertThat(actual.equipmentIds()).as("equipmentIds")
                .containsExactlyInAnyOrderElementsOf(expected.equipmentIds());
        softly.assertAll();
    }
}