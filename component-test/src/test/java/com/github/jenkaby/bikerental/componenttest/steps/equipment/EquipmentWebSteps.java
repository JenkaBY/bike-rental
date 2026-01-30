
package com.github.jenkaby.bikerental.componenttest.steps.equipment;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyType;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyUnit;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class EquipmentWebSteps {

    private final ScenarioContext scenarioContext;

    @Then("the '{vocabularyType}' response only contains")
    public void theEquipmentStatusResponseContains(VocabularyType vocabularyType, List<VocabularyUnit> expectedUnits) {
        Comparator<VocabularyUnit> comparing = Comparator.comparing(VocabularyUnit::slug)
                .thenComparing(VocabularyUnit::name);
        var actual = scenarioContext.getResponseAsList(VocabularyUnit.class).stream()
                .sorted(comparing)
                .toList();

        assertThat(actual.size()).as("Sizes are matched").isEqualTo(expectedUnits.size());

        var expectedAndSorted = expectedUnits.stream().sorted(comparing).toList();
        assertThat(actual).zipSatisfy(expectedAndSorted, (act, exp) -> {
            log.info("Comparing '{}' actual: {} with expected: {}", vocabularyType.getHumanReadableName(), act, exp);
            assertThat(act.slug()).isEqualTo(exp.slug());
            assertThat(act.name()).isEqualTo(exp.name());
            assertThat(act.description()).isEqualTo(exp.description());
        });
    }

    @Then("the '{vocabularyType}' request is prepared with the following data")
    public void theEquipmentStatusResponseContains(VocabularyType type, VocabularyUnit request) {
        log.info("Preparing '{}' request with data: {}", type.getHumanReadableName(), request);

        scenarioContext.setRequestBody(request);
    }
}
