package com.github.jenkaby.bikerental.componenttest.steps.dev;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.shared.config.DevClockConfig.SettableClock;
import com.github.jenkaby.bikerental.shared.web.TimeTravelController;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    @When("the time travel SSE snapshot is read")
    public void theTimeTravelSseSnapshotIsRead() throws Exception {
        var url = new URL("http://localhost:" + port + "/api/dev/time");
        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setReadTimeout(2000);
        try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            var line = reader.readLine();
            var json = line.replaceFirst("^data:\\s*", "");
            scenarioContext.setResponse(ResponseEntity.ok(json));
        }
    }

    @Then("the virtual clock instant is approximately the current time")
    public void theVirtualClockInstantIsApproximatelyTheCurrentTime() {
        assertThat(settableClock.instant())
                .as("virtual clock instant after reset")
                .isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    }
}
