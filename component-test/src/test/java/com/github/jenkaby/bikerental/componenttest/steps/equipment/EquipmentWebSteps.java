
package com.github.jenkaby.bikerental.componenttest.steps.equipment;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyType;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyUnit;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentRequest;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class EquipmentWebSteps {

    public static final Comparator<EquipmentResponse> BY_SERIAL_NUMBER = Comparator.comparing(EquipmentResponse::serialNumber);
    public static final Comparator<VocabularyUnit> DEFAULT_BY_SLUG = Comparator.comparing(VocabularyUnit::slug)
            .thenComparing(VocabularyUnit::name);
    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;

    @Then("the '{vocabularyType}' response only contains")
    public void theVocabResponseContains(VocabularyType vocabularyType, List<VocabularyUnit> expectedUnits) {
        var actual = Stream.of(scenarioContext.getResponseBody(VocabularyUnit.class))
                .toList();

        assertResult(vocabularyType, expectedUnits, actual);
    }

    @Then("the '{vocabularyType}' response only contains list of")
    public void theVocabResponseContainsListOf(VocabularyType vocabularyType, List<VocabularyUnit> expectedUnits) {
        var actual = scenarioContext.getResponseAsList(VocabularyUnit.class).stream()
                .sorted(DEFAULT_BY_SLUG)
                .toList();

        assertResult(vocabularyType, expectedUnits, actual);
    }

    @Given("the '{vocabularyType}' request is prepared with the following data")
    public void theVocabRequestContains(VocabularyType type, VocabularyUnit request) {
        log.info("Preparing '{}' request with data: {}", type.getHumanReadableName(), request);

        scenarioContext.setRequestBody(request);
    }

    @Given("the equipment request is prepared with the following data")
    public void theEquipmentRequestIsPreparedWithData(EquipmentRequest request) {
        log.info("Preparing equipment request with data: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the equipment response contains")
    public void theEquipmentResponseContains(EquipmentResponse expectedResponse) {
        var actual = scenarioContext.getResponseBody(EquipmentResponse.class);

        log.info("Comparing equipment actual: {} with expected: {}", actual, expectedResponse);
        assertSoftly(softly -> {
            if (expectedResponse.id() != null) {
                softly.assertThat(actual.id()).as("Equipment ID").isEqualTo(expectedResponse.id());
            }
            softly.assertThat(actual.serialNumber()).as("Serial number").isEqualTo(expectedResponse.serialNumber());
            softly.assertThat(actual.uid()).as("UID").isEqualTo(expectedResponse.uid());
            softly.assertThat(actual.type()).as("Equipment type slug").isEqualTo(expectedResponse.type());
            softly.assertThat(actual.model()).as("Model").isEqualTo(expectedResponse.model());
            softly.assertThat(actual.status()).as("Status slug").isEqualTo(expectedResponse.status());
            softly.assertThat(actual.commissionedAt()).as("Commissioned date").isEqualTo(expectedResponse.commissionedAt());
            softly.assertThat(actual.condition()).as("Condition").isEqualTo(expectedResponse.condition());
        });
    }

    @Then("the equipment response only contains")
    public void theEquipmentResponseContains(List<EquipmentResponse> expectedResponses) {
        var actual = Stream.of(scenarioContext.getResponseBody(EquipmentResponse.class))
                .toList();

        assertResult(expectedResponses, actual);
    }

    @Then("the equipment response only contains list of")
    public void theEquipmentResponseContainsListOf(List<EquipmentResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsPage(EquipmentResponse.class).items().stream()
                .sorted(BY_SERIAL_NUMBER)
                .toList();

        assertResult(expectedResponses, actual);
    }

    private static void assertResult(List<EquipmentResponse> expectedResponses, List<EquipmentResponse> actual) {
        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedResponses.size());

        var expectedAndSorted = expectedResponses.stream().sorted(BY_SERIAL_NUMBER).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing equipment response actual: {} with expected: {}", act, exp);

            assertSoftly(softly -> {
                softly.assertThat(act.id()).isNotNull();
                softly.assertThat(act.serialNumber()).as("Serial number").isEqualTo(exp.serialNumber());
                softly.assertThat(act.uid()).as("UID").isEqualTo(exp.uid());
                softly.assertThat(act.type()).as("Equipment type slug").isEqualTo(exp.type());
                softly.assertThat(act.model()).as("Model").isEqualTo(exp.model());
                softly.assertThat(act.status()).as("Status slug").isEqualTo(exp.status());
                softly.assertThat(act.commissionedAt()).as("Commissioned date").isEqualTo(exp.commissionedAt());
                softly.assertThat(act.condition()).as("Condition").isEqualTo(exp.condition());
            });
        });
    }

    private static void assertResult(VocabularyType vocabularyType, List<VocabularyUnit> expectedUnits, List<VocabularyUnit> actual) {
        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedUnits.size());

        var expectedAndSorted = expectedUnits.stream().sorted(DEFAULT_BY_SLUG).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing '{}' actual: {} with expected: {}", vocabularyType.getHumanReadableName(), act, exp);
            assertThat(act.slug()).isEqualTo(exp.slug());
            assertThat(act.name()).isEqualTo(exp.name());
            assertThat(act.description()).isEqualTo(exp.description());
        });
    }

    @Given("the equipment being updated is")
    public void theEquipmentBeingUpdatedIs(EquipmentRequest request) {
        scenarioContext.setRequestBody(request);
        webRequestSteps.requestHasBeenMadeToEndpoint(HttpMethod.POST, "/api/equipments");
        var response = scenarioContext.getResponseBody(EquipmentResponse.class);
        scenarioContext.setRequestedObjectId(response.id().toString());
    }
}
