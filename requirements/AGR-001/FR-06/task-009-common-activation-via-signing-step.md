<task_file_template>

# Task 009: Create the shared "rental is activated via signing" common step

> **Applied Skill:** `spring-boot-java-cucumber` / `component-tests` — new reusable step lives in `steps/common/`
> (design.md §2: "Prefer ONE shared common step ... placed in `steps/common/`"); it is stateless (constructor
> injection only, all state via `ScenarioContext`); it does NOT make HTTP calls itself — it delegates to
> `WebRequestSteps.requestHasBeenMadeToEndpointWithContext` (PATCH lifecycles) and a direct `TestRestTemplate` call is
> avoided by reusing that same delegated method for the sign POST too, since HTTP-calling steps must live only in
> `WebRequestSteps` (`.claude/rules/component-tests.md`). No DataTable/`@DataTableType` needed — the step takes no
> table. Depends on Task 008 (Background fixture `id=5` must exist for scenarios that will use this step) and the
> existing `RentalLifecycleWebSteps.theLifecycleRequestIs` / `WebRequestSteps` / `AgreementSigningSteps` classes
> (read, not modified, by this task).

## 1. Objective

Create a new stateless step class `RentalActivationSteps` in `steps/common/` exposing a single Given step
`the rental is activated via signing` that composes, for the rental identified by
`scenarioContext.getRequestedObjectId()`:

1. `PATCH /api/rentals/{id}/lifecycles` with `{"status": "AWAITING_SIGNATURE", "operatorId": "OP1"}`.
2. `POST /api/rentals/{id}/signatures` with a `SignAgreementRequest` using a hardcoded `templateId=5` (matching the
   Background fixture added in Task 008) and a hardcoded `rentalVersion=1` (a freshly-seeded DRAFT rental with no
   explicit `version` column starts at version 0 and becomes version 1 after the `AWAITING_SIGNATURE` transition —
   see `rental-signing-lifecycle.feature` scenario "Cancel signing returns to draft..." and
   `agreement-signing.feature`'s happy-path scenario which signs a rental pre-seeded with `version=1`).

This lets feature-file scenarios replace the two-step "set ACTIVE lifecycle request, PATCH lifecycles" sequence with
this single Given step. The step reuses `WebRequestSteps.requestHasBeenMadeToEndpointWithContext` for the first call
(same as every other lifecycle scenario in this codebase) and a direct call into the same class's generic
`{httpMethod} request has been made to {string} endpoint` step for the sign POST, so no HTTP client code is
duplicated outside `WebRequestSteps`.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/common/RentalActivationSteps.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.componenttest.steps.common;

import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.transformer.SignAgreementRequestTransformer;
import com.github.jenkaby.bikerental.rental.web.command.dto.LifecycleStatus;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
```

**Code to Add/Replace:**

* **Location:** New file, full content below.
* **Snippet:**

```java
@Slf4j
@RequiredArgsConstructor
public class RentalActivationSteps {

    private static final long DEFAULT_TEMPLATE_ID = 5L;
    private static final long DEFAULT_RENTAL_VERSION = 1L;
    private static final String DEFAULT_OPERATOR_ID = "OP1";

    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;

    @Given("the rental is activated via signing")
    public void theRentalIsActivatedViaSigning() {
        log.info("Activating rental {} via prepare-signing + signing", scenarioContext.getRequestedObjectId());

        scenarioContext.setRequestBody(new RentalLifecycleRequest(LifecycleStatus.AWAITING_SIGNATURE, DEFAULT_OPERATOR_ID));
        webRequestSteps.requestHasBeenMadeToEndpointWithContext(HttpMethod.PATCH, "/api/rentals/{requestedObjectId}/lifecycles");

        var rentalId = scenarioContext.getRequestedObjectId();
        var signRequest = new SignAgreementRequest(
                SignAgreementRequestTransformer.VALID_SIGNATURE_PNG_BASE64,
                DEFAULT_RENTAL_VERSION,
                DEFAULT_TEMPLATE_ID,
                DEFAULT_OPERATOR_ID);
        scenarioContext.setRequestBody(signRequest);
        webRequestSteps.requestHasBeenMadeToEndpoint(HttpMethod.POST, "/api/rentals/" + rentalId + "/signatures");
    }
}
```

Notes for the dev agent:

- `WebRequestSteps.requestHasBeenMadeToEndpointWithContext(HttpMethod, String)` and
  `WebRequestSteps.requestHasBeenMadeToEndpoint(HttpMethod, String)` are both already `public` methods on that class
  (see `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/common/WebRequestSteps.java`)
  — do not re-implement HTTP calls here, only delegate.
- After this step runs, `scenarioContext.getResponse()` holds the response of the LAST HTTP call, i.e. the
  `SignatureCreatedResponse` from `POST /api/rentals/{id}/signatures` — NOT a `RentalResponse`. Callers of this step
  must use DB-read assertions (`rental was persisted in database`, `rental equipments were persisted in database`)
  rather than `the rental response only contains ...` after calling this step.
- `scenarioContext.getRequestedObjectId()` is unchanged by this step (both calls target the same rental id already
  set by the scenario's `a single rental exists in the database with the following data` step).
- Do not add a `@DataTableType` transformer — this step takes no table, matching the "no new step needed" instruction
  for reused steps in `.claude/rules/component-tests.md`.
- Keep this class package-visible/public but with NO instance fields beyond the two constructor-injected
  dependencies (`.claude/rules/component-tests.md` — step classes are stateless).

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. Assume the DB is already up. This step is not
yet referenced by any feature file at this point in the checklist, so this task only confirms the project compiles
with the new class present.

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

</task_file_template>
