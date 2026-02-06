package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

@Slf4j
@RequiredArgsConstructor
public class RentalWebSteps {

    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;

//    @Given("the rental record being updated/created is")
//    public void theTariffBeingUpdatedIs(Rental request) {
//        scenarioContext.setRequestBody(request);
//        webRequestSteps.requestHasBeenMadeToEndpoint(HttpMethod.POST, "/api/rentals");
//        var response = scenarioContext.getResponseBody(TariffResponse.class);
//        scenarioContext.setRequestedObjectId(response.id().toString());
//    }

    @Then("the rental response only contains")
    public void theRentalResponseOnlyContains(RentalResponse expectedRental) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        assertRentalResponse(actualRental, expectedRental);
    }

    private void assertRentalResponse(RentalResponse actual, RentalResponse expected) {
        log.info("Comparing rental response actual: {} with expected: {}", actual, expected);
        var softly = new SoftAssertions();
        if (expected.id() != null) {
            softly.assertThat(actual.id())
                    .as("Rental ID")
                    .isEqualTo(expected.id());
        } else {
            softly.assertThat(actual.id())
                    .as("Rental ID should not be null")
                    .isNotNull();
        }
        softly.assertThat(actual.customerId())
                .as("Customer ID")
                .isEqualTo(expected.customerId());
        softly.assertThat(actual.equipmentId())
                .as("Equipment ID")
                .isEqualTo(expected.equipmentId());
        softly.assertThat(actual.tariffId())
                .as("Tariff ID")
                .isEqualTo(expected.tariffId());
        softly.assertThat(actual.status())
                .as("Status")
                .isEqualTo(expected.status());
        softly.assertThat(actual.startedAt())
                .as("Started at")
                .isEqualTo(expected.startedAt());
        softly.assertThat(actual.expectedReturnAt())
                .as("Expected return at")
                .isEqualTo(expected.expectedReturnAt());
        softly.assertThat(actual.actualReturnAt())
                .as("Actual return at")
                .isEqualTo(expected.actualReturnAt());
        softly.assertThat(actual.plannedDurationMinutes())
                .as("Planned duration minutes")
                .isEqualTo(expected.plannedDurationMinutes());
        softly.assertThat(actual.actualDurationMinutes())
                .as("Actual duration minutes")
                .isEqualTo(expected.actualDurationMinutes());
        softly.assertThat(actual.estimatedCost())
                .as("Estimated cost")
                .isEqualTo(expected.estimatedCost());
        softly.assertThat(actual.finalCost())
                .as("Final cost")
                .isEqualTo(expected.finalCost());
        softly.assertAll();
    }
}
