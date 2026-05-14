# Task 008: Add Component Tests for Batch Customer Fetch Endpoint

> **Applied Skill:** `spring-boot-java-cucumber` — happy-path-only scenarios; new customer-specific batch request
> step using alias resolution from `Aliases`; new assertion steps in `CustomerWebSteps` for flat-list responses;
> feature file organized under `features/customer/`.

## 1. Objective

Add a Cucumber feature file covering the three happy-path scenarios of `GET /api/customers/batch` and extend
`CustomerWebSteps` with two new assertion step methods:

1. `"the batch customer response contains"` — compares a flat `List<CustomerResponse>` from the response
   against the expected rows in the DataTable.
2. `"the batch customer response is empty"`.

The **request** uses the existing `WebRequestSteps` step
`"a GET request has been made to {string} endpoint with query parameters"`. No new request step is needed.
Because `Aliases.getValueOrDefault` is a plain map lookup, a combined string `"CUS1,CUS2"` is not resolved;
therefore the `ids` query-param value uses the literal UUID strings that are the canonical values behind
the aliases. The expected-response DataTable continues to use `CUS1`/`CUS2` tokens — those are resolved
by `CustomerResponseTransformer`.

---

## 2. Files to Modify / Create

### File A — Extend step definitions

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/customer/CustomerWebSteps.java`
* **Action:** Modify Existing File

### File B — New feature file

* **File Path:**
  `component-test/src/test/resources/features/customer/customer-batch.feature`
* **Action:** Create New File

---

## 3. Code Implementation

### File A — `CustomerWebSteps.java`

#### Step 1 — Add missing imports

**Current import block (lines 1–11):**

```java
package com.github.jenkaby.bikerental.componenttest.steps.customer;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
```

**Replace with (adds three new imports):**

```java
package com.github.jenkaby.bikerental.componenttest.steps.customer;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
```

#### Step 2 — Add new step methods

**Location:** Immediately after the closing brace of the `theResponseMatchesExpectedCustomer` method and before
the closing brace of the class.

**Current end of class:**

```java
        softly.assertAll();
    }
}
```

**Replace with:**

```java
        softly.assertAll();
    }

    @Then("the batch customer response contains")
    public void theBatchCustomerResponseContains(List<CustomerResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsList(CustomerResponse.class).stream()
                .sorted(Comparator.comparing(CustomerResponse::phone))
                .toList();
        var expectedSorted = expectedResponses.stream()
                .sorted(Comparator.comparing(CustomerResponse::phone))
                .toList();

        assertThat(actual).as("batch customer response size").hasSize(expectedSorted.size());

        var softly = new SoftAssertions();
        for (int i = 0; i < expectedSorted.size(); i++) {
            var act = actual.get(i);
            var exp = expectedSorted.get(i);
            softly.assertThat(act.id()).as("Customer[%d].id", i).isNotNull();
            softly.assertThat(act.phone()).as("Customer[%d].phone", i).isEqualTo(exp.phone());
            softly.assertThat(act.firstName()).as("Customer[%d].firstName", i).isEqualTo(exp.firstName());
            softly.assertThat(act.lastName()).as("Customer[%d].lastName", i).isEqualTo(exp.lastName());
            softly.assertThat(act.email()).as("Customer[%d].email", i).isEqualTo(exp.email());
            softly.assertThat(act.birthDate()).as("Customer[%d].birthDate", i).isEqualTo(exp.birthDate());
            softly.assertThat(act.comments()).as("Customer[%d].comments", i).isEqualTo(exp.comments());
        }
        softly.assertAll();
    }

    @Then("the batch customer response is empty")
    public void theBatchCustomerResponseIsEmpty() {
        assertThat(scenarioContext.getResponseAsList(CustomerResponse.class)).isEmpty();
    }
}
```

---

### File B — `customer-batch.feature`

The `ids` query-param value is a comma-separated string of literal UUIDs — the canonical values the CUS
aliases map to. `Aliases.getValueOrDefault` is a plain map lookup: a combined string like
`"CUS1,CUS2"` is not a registered key and is returned unchanged, so the API would receive the alias token
instead of a UUID. Use the explicit UUID strings in the request DataTable.

The `id` column in the **expected**-response DataTable still uses `CUS1`/`CUS2` alias tokens; those are
resolved by `CustomerResponseTransformer` via `Aliases.getCustomerId(alias)`.

| Alias | UUID                                   |
|-------|----------------------------------------|
| CUS1  | `11111111-1111-1111-1111-111111111111` |
| CUS2  | `11111111-1111-1111-1111-111111111112` |
| CUS3  | `11111111-1111-1111-1111-111111111113` |

* **Snippet:**

```gherkin
Feature: Batch customer fetch by UUIDs
  As a frontend client
  I want to fetch multiple customer records in a single request
  So that rendering the rental list does not require N individual API calls

  Background:
    Given customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |
      | CUS3 | +79998883333 | Maria     | Garcia   | maria@test.com   | 1983-03-03 | Sexy     |

  Scenario: Batch fetch returns all matching customers when all UUIDs exist
    When a GET request has been made to "/api/customers/batch" endpoint with query parameters
      | ids                                                                                      |
      | 11111111-1111-1111-1111-111111111111,11111111-1111-1111-1111-111111111112                 |
    Then the response status is 200
    And the batch customer response contains
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |

  Scenario: Batch fetch silently omits non-existent UUIDs
    When a GET request has been made to "/api/customers/batch" endpoint with query parameters
      | ids                                                                                      |
      | 11111111-1111-1111-1111-111111111111,00000000-0000-0000-0000-000000000099                 |
    Then the response status is 200
    And the batch customer response contains
      | id   | phone        | firstName | lastName | email | birthDate | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null  | null      | null     |

  Scenario: Batch fetch returns empty list when no UUIDs match any record
    When a GET request has been made to "/api/customers/batch" endpoint with query parameters
      | ids                                  |
      | 00000000-0000-0000-0000-000000000098  |
    Then the response status is 200
    And the batch customer response is empty
```

---

## 4. Additional Notes

### Why literal UUIDs in the request DataTable

`WebRequestSteps.requestHasBeenMadeToEndpointWithQueryParams` uses `@Transpose DataTable` and calls
`DataTable::asMap`, which takes **column 0 as keys and column 1 as values**. Each value is passed through
`Aliases.getValueOrDefault` — a plain map lookup. A combined string `"CUS1,CUS2"` is not a registered alias
key so it is returned unchanged, and the API would receive literal text instead of UUIDs. Using the actual
UUID strings ensures `getValueOrDefault` returns them as-is and the URL becomes `?ids=uuid1,uuid2`, which
Spring MVC correctly binds to `List<UUID>`.

### Why CUS aliases still work in the expected-response DataTable

`CustomerResponseTransformer` resolves the `id` column via `Aliases.getCustomerId(alias)` before the
assertion step compares the response. This is entirely independent of the request DataTable.

---

## 5. Validation Steps

skip
