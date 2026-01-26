package com.github.jenkaby.bikerental.componenttest.steps.common.hook;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ScenarioHooks {

    private final ScenarioContext scenarioContext;

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("===============================================");
        log.info("Starting scenario: {}", scenario.getName());
        log.info("Tags: {}", scenario.getSourceTagNames());
        log.info("===============================================");
    }

    @After(order = 100) // Run after DbSteps cleanup
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("===============================================");
            log.error("Scenario FAILED: {}", scenario.getName());
        } else {
            log.info("Scenario PASSED: {}", scenario.getName());
        }
        log.info("===============================================");
    }
}
