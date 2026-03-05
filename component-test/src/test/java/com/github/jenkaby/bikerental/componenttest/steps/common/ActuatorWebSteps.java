package com.github.jenkaby.bikerental.componenttest.steps.common;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ActuatorWebSteps {

    private final BuildProperties buildProperties;
    private final ScenarioContext scenarioContext;

    @Then("the response build version matches git commit")
    public void theResponseBuildVersionMatchesGitCommit() {
        var documentContext = JsonPath.parse(scenarioContext.getStringResponseBody());
        var actualVersion = documentContext.read("$.build.version", String.class);
        var expectedGitCommit = buildProperties.get("git.commit");

        assertThat(actualVersion)
                .as("$.build.version should match BuildProperties git.commit")
                .isEqualTo(expectedGitCommit);
    }
}
