package com.github.jenkaby.bikerental.componenttest.steps.equipment;


import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class EquipmentStatusWebSteps {

    private static final Comparator<EquipmentStatusResponse> STATUS_BY_SLUG = Comparator.comparing(EquipmentStatusResponse::slug);
    private final ScenarioContext scenarioContext;

    @Given("the equipment status request is prepared with the following data")
    public void theEquipmentRequestContains(EquipmentStatusResponse request) {
        log.info("Preparing equipment status request with data: {}", request);

        scenarioContext.setRequestBody(request);
    }

    @Then("the equipment status response only contains list of")
    public void theEquipmentResponseContainsListOf(List<EquipmentStatusResponse> expected) {
        var actual = scenarioContext.getResponseAsList(EquipmentStatusResponse.class).stream()
                .sorted(STATUS_BY_SLUG)
                .toList();

        assertResult(expected, actual);
    }

    @Then("the equipment status response only contains")
    public void theEquipmentStatusResponseContains(List<EquipmentStatusResponse> expected) {
        var actual = Stream.of(scenarioContext.getResponseBody(EquipmentStatusResponse.class))
                .toList();

        assertResult(expected, actual);
    }

    private static void assertResult(List<EquipmentStatusResponse> expectedUnits, List<EquipmentStatusResponse> actual) {
        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedUnits.size());

        var expectedAndSorted = expectedUnits.stream().sorted(STATUS_BY_SLUG).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing actual: {} with expected: {}", act, exp);
            assertThat(act.slug()).isEqualTo(exp.slug());
            assertThat(act.name()).isEqualTo(exp.name());
            assertThat(act.description()).isEqualTo(exp.description());
            assertThat(act.allowedTransitions()).isEqualTo(exp.allowedTransitions());
        });
    }

}
