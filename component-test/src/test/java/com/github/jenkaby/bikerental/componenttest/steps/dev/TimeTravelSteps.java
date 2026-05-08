package com.github.jenkaby.bikerental.componenttest.steps.dev;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.shared.config.DevClockConfig.SettableClock;
import com.github.jenkaby.bikerental.shared.web.TimeTravelController;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@RequiredArgsConstructor
public class TimeTravelSteps {

    private final SettableClock settableClock;
    private final ScenarioContext scenarioContext;
    @LocalServerPort
    private final int port;

    @After
    public void resetClockAfterScenario() {
        settableClock.reset();
    }

    @Given("the time travel request instant is {string}")
    public void theTimeTravelRequestInstantIs(String instant) {
        scenarioContext.setRequestBody(new TimeTravelController.SetTimeRequest(Instant.parse(instant)));
    }

    @Then("the virtual clock instant is approximately the current time")
    public void theVirtualClockInstantIsApproximatelyTheCurrentTime() {
        assertThat(settableClock.instant())
                .as("virtual clock instant after reset")
                .isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    }
}
