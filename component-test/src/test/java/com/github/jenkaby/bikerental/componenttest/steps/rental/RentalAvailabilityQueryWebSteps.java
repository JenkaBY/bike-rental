package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class RentalAvailabilityQueryWebSteps {

    private static final Comparator<AvailableEquipmentResponse> BY_ID =
            Comparator.comparing(AvailableEquipmentResponse::id);

    private final ScenarioContext scenarioContext;

    @Then("the available equipment response only contains page of")
    public void theAvailableEquipmentResponseOnlyContainsPageOf(List<AvailableEquipmentResponse> expected) {
        var actual = scenarioContext.getResponseAsPage(AvailableEquipmentResponse.class).items()
                .stream()
                .sorted(BY_ID)
                .toList();
        log.info("Comparing available equipment response actual: {} with expected: {}", actual, expected);
        assertThat(actual)
                .as("Available equipment items list size")
                .hasSize(expected.size());
        var sortedExpected = expected.stream().sorted(BY_ID).toList();
        assertThat(actual).zipSatisfy(sortedExpected, (act, exp) ->
                assertSoftly(softly -> {
                    softly.assertThat(act.id()).as("Equipment ID").isEqualTo(exp.id());
                    softly.assertThat(act.uid()).as("UID").isEqualTo(exp.uid());
                    softly.assertThat(act.serialNumber()).as("Serial number").isEqualTo(exp.serialNumber());
                    softly.assertThat(act.typeSlug()).as("Type slug").isEqualTo(exp.typeSlug());
                    softly.assertThat(act.model()).as("Model").isEqualTo(exp.model());
                })
        );
    }
}