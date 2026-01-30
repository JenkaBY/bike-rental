
package com.github.jenkaby.bikerental.componenttest.steps.equipment;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyType;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyUnit;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Then("the '{vocabularyType}' response only contains")
    public void theVocabResponseContains(VocabularyType vocabularyType, List<VocabularyUnit> expectedUnits) {
        var actual = Stream.of(scenarioContext.getResponseBody(VocabularyUnit.class))
                .toList();

        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedUnits.size());

        var expectedAndSorted = expectedUnits.stream().sorted(DEFAULT_BY_SLUG).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing '{}' actual: {} with expected: {}", vocabularyType.getHumanReadableName(), act, exp);
            assertThat(act.slug()).isEqualTo(exp.slug());
            assertThat(act.name()).isEqualTo(exp.name());
            assertThat(act.description()).isEqualTo(exp.description());
        });
    }

    @Then("the '{vocabularyType}' response only contains list of")
    public void theVocabResponseContainsListOf(VocabularyType vocabularyType, List<VocabularyUnit> expectedUnits) {
        var actual = scenarioContext.getResponseAsList(VocabularyUnit.class).stream()
                .sorted(DEFAULT_BY_SLUG)
                .toList();

        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedUnits.size());

        var expectedAndSorted = expectedUnits.stream().sorted(DEFAULT_BY_SLUG).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing '{}' actual: {} with expected: {}", vocabularyType.getHumanReadableName(), act, exp);
            assertThat(act.slug()).isEqualTo(exp.slug());
            assertThat(act.name()).isEqualTo(exp.name());
            assertThat(act.description()).isEqualTo(exp.description());
        });
    }

    @Given("the '{vocabularyType}' request is prepared with the following data")
    public void theVocabResponseContains(VocabularyType type, VocabularyUnit request) {
        log.info("Preparing '{}' request with data: {}", type.getHumanReadableName(), request);

        scenarioContext.setRequestBody(request);
    }

    @Then("the equipment response only contains")
    public void theEquipmentResponseContains(List<EquipmentResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsList(EquipmentResponse.class).stream()
                .sorted(BY_SERIAL_NUMBER)
                .toList();

        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedResponses.size());

        var expectedAndSorted = expectedResponses.stream().sorted(BY_SERIAL_NUMBER).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing equipment actual: {} with expected: {}", act, exp);
            assertSoftly(softly -> {
                softly.assertThat(act.id()).as("Equipment ID").isEqualTo(exp.id());
                softly.assertThat(act.serialNumber()).as("Serial number").isEqualTo(exp.serialNumber());
                softly.assertThat(act.uid()).as("UID").isEqualTo(exp.uid());
                softly.assertThat(act.equipmentTypeSlug()).as("Equipment type slug").isEqualTo(exp.equipmentTypeSlug());
                softly.assertThat(act.model()).as("Model").isEqualTo(exp.model());
                softly.assertThat(act.statusSlug()).as("Status slug").isEqualTo(exp.statusSlug());
                softly.assertThat(act.commissionedAt()).as("Commissioned date").isEqualTo(exp.commissionedAt());
                softly.assertThat(act.condition()).as("Condition").isEqualTo(exp.condition());
            });
        });
    }
}
