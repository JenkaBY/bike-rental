# Task 005: Create component test — time-travel feature (Cucumber)

> **Applied Skill:** `spring-boot-java-cucumber` skill — Cucumber feature file + Steps class, `@DataTableType`
> transformers, `WebRequestSteps` reuse, `ScenarioContext`.
> **Applied Skill:** `java.instructions.md` — no-comments, expressive naming.

## 1. Objective

Create four files that verify the full round-trip of the time-travel endpoints:

1. `TimeTravelRequestTransformer` — `@DataTableType` converting a DataTable row to
   `TimeTravelController.SetTimeRequest`.
2. `TimeTravelResponseTransformer` — `@DataTableType` converting a DataTable row to `TimeTravelController.TimeResponse`.
3. `TimeTravelSteps` — individual field-setting steps that build the request body and store it via `ScenarioContext`;
   HTTP calls are delegated to the existing `WebRequestSteps` steps; a custom assertion step checks the clock state via
   the injected `SettableClock` bean after a reset.
4. `time-travel.feature` — three scenarios: set, reset, SSE snapshot.

> **SSE approach:** `TestRestTemplate` cannot stream SSE. Instead, the SSE step uses a plain `HttpURLConnection` with a
> 2-second read timeout to read the first `data:` line emitted at `initialDelay = 0`. The JSON is stripped of the `data:`
> prefix and stored in `ScenarioContext` so the existing `"the response contains"` assertion step works unchanged.

## 2. Files to Modify / Create

### File A — Request transformer

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/TimeTravelRequestTransformer.java`
* **Action:** Create New File

### File B — Response transformer

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/TimeTravelResponseTransformer.java`
* **Action:** Create New File

### File C — Steps class

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/dev/TimeTravelSteps.java`
* **Action:** Create New File

### File D — Feature file

* **File Path:** `component-test/src/test/resources/features/time-travel.feature`
* **Action:** Create New File

## 3. Code Implementation

### File A — TimeTravelRequestTransformer

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.shared.web.TimeTravelController;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;

public class TimeTravelRequestTransformer {

    @DataTableType
    public TimeTravelController.SetTimeRequest setTimeRequest(Map<String, String> entry) {
        return new TimeTravelController.SetTimeRequest(Instant.parse(entry.get("instant")));
    }
}
```

### File B — TimeTravelResponseTransformer

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.shared.web.TimeTravelController;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;

public class TimeTravelResponseTransformer {

    @DataTableType
    public TimeTravelController.TimeResponse timeResponse(Map<String, String> entry) {
        return new TimeTravelController.TimeResponse(Instant.parse(entry.get("instant")));
    }
}
```

### File C — TimeTravelSteps

```java
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
```

### File D — time-travel.feature

```gherkin
Feature: Time travel virtual clock

  Background:
    Given the request header "Content-Type" is "application/json"

  Scenario: Set virtual clock to a fixed instant
    Given the time travel request instant is "2026-01-01T00:00:00Z"
    When a PUT request has been made to "/api/dev/time" endpoint
    Then the response status is 200
    And the response contains
      | path      | value                |
      | $.instant | 2026-01-01T00:00:00Z |

  Scenario: Reset virtual clock restores system time
    Given the time travel request instant is "2020-06-01T12:00:00Z"
    And a PUT request has been made to "/api/dev/time" endpoint
    Then the response status is 200
    And the response contains
      | path      | value                |
      | $.instant | 2020-06-01T12:00:00Z |
    When a DELETE request has been made to "/api/dev/time" endpoint
    Then the response status is 204
    And the virtual clock instant is approximately the current time

  Scenario: SSE stream reports the pinned time and fixed flag
    Given the time travel request instant is "2026-03-15T08:00:00Z"
    And a PUT request has been made to "/api/dev/time" endpoint
    When the time travel SSE snapshot is read
    Then the response contains
      | path      | value                |
      | $.instant | 2026-03-15T08:00:00Z |
      | $.fixed   | true                 |
```

## 4. Key Implementation Notes

1. **`ScenarioContext.setResponse(ResponseEntity<String>)`** is the correct setter (field
   `private ResponseEntity<String> response` + Lombok `@Setter`). Do NOT use `setLastResponse`.
2. **`theTimeTravelRequestInstantIs`** sets `requestBody` to a typed `SetTimeRequest` record. Spring's `RestTemplate` (
   inside `WebRequestSteps`) will serialize it as `{"instant":"2026-01-01T00:00:00Z"}` via
   `MappingJackson2HttpMessageConverter`. The `Content-Type: application/json` header set in the `Background` ensures
   the correct converter is selected.
3. **SSE `HttpURLConnection` step** — `TimeTravelController.streamTime()` schedules the first emission at
   `initialDelay = 0`, so the `data:` line arrives almost immediately. `readLine()` returns the first line (
   `data:{...}`); `replaceFirst("^data:\\s*", "")` strips the prefix, leaving plain JSON. The 2-second read timeout acts
   as a safety net only.
4. **`SettableClock` injection** — `DevClockConfig` is active under `test` profile and registers the `SettableClock`
   bean. `@SpringBootTest` in `RunComponentTests` runs in the same JVM, so `SettableClock` is injectable into step
   classes directly via constructor.
5. **`@After` hook** — resets the clock after every scenario to prevent state leaking into subsequent tests. Import
   `io.cucumber.java.After`, not JUnit's `@AfterEach`.
6. **`"And a PUT request..."` in the reset scenario** — Cucumber `And` inherits the preceding keyword; here `Given` is
   the context keyword. `WebRequestSteps` registers the step as `@When`, but Cucumber matches steps regardless of
   keyword annotation, so this works correctly.
7. The `test` profile satisfies both `DevClockConfig`'s and `TimeTravelController`'s `@Profile({"dev","test"})`
   conditions without any additional annotation in the test.

## 5. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
