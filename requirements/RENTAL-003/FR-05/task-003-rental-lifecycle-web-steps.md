# Task 003: Create RentalLifecycleRequestTransformer and RentalLifecycleWebSteps

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — `@DataTableType` transformers,
> `ScenarioContext`, step pattern for setting request body

## 1. Objective

Create a `@DataTableType` transformer that converts a datatable row into a
`RentalLifecycleRequest`, and a step definition class that places the request into
`ScenarioContext`. Follows the same pattern as `RentalRequestTransformer` / `RentalWebSteps`.

## 2. Files to Modify / Create

### 2.1 RentalLifecycleRequestTransformer (New)

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalLifecycleRequestTransformer.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleStatus;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalLifecycleRequestTransformer {

    @DataTableType
    public RentalLifecycleRequest transform(Map<String, String> entry) {
        var statusString = DataTableHelper.getStringOrNull(entry, "status");
        var status = statusString != null ? RentalLifecycleStatus.valueOf(statusString) : null;
        var operatorId = DataTableHelper.getStringOrNull(entry, "operatorId");
        return new RentalLifecycleRequest(status, operatorId);
    }
}
```

### 2.2 RentalLifecycleWebSteps (New)

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalLifecycleWebSteps.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RentalLifecycleWebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the lifecycle request is")
    public void theLifecycleRequestIs(RentalLifecycleRequest request) {
        log.info("Preparing lifecycle request: {}", request);
        scenarioContext.setRequestBody(request);
    }
}
```

## 3. Notes

* The `@DataTableType` transformer is registered globally by Cucumber — no extra wiring needed.
* `scenarioContext.setRequestBody(request)` accepts any object; serialization happens in
  `WebRequestSteps` before the HTTP call.
* The HTTP call uses the existing step:
  `When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context`
  from `WebRequestSteps`, so no new HTTP step is needed.
* The corresponding Gherkin step in `rental-lifecycle.feature` uses a DataTable:
  ```gherkin
  Given the lifecycle request is
    | status | operatorId |
    | ACTIVE | OP1        |
  ```

## 4. Validation Steps

skip