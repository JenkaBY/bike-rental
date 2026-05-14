# Task 007: Add Batch Endpoint Tests to `CustomerQueryControllerTest`

> **Applied Skill:** `spring-mvc-controller-test` — `@MockitoBean` declarations; `@Nested` class per endpoint;
> happy-path `@Test` methods; `BadRequest` sub-nested class for validation scenarios.

## 1. Objective

Register `GetCustomersByIdsUseCase` as a `@MockitoBean` in the test class and add a
`@Nested class GetCustomersBatch` covering the two happy-path scenarios (found items, empty result) and
two bad-request scenarios (missing `ids`, over-100 list). Malformed UUID input is rejected by Spring's
type converter before Bean Validation runs, so no separate test for it is needed beyond confirming the
400 behaviour; the missing-`ids` and over-100-list tests are the actionable validation paths.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following imports to the existing import block:

```java
import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomersByIdsUseCase;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.mockito.Mockito.mock;
```

(`java.util.List`, `java.util.UUID`, `org.mockito.BDDMockito.given`, and
`org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get` are already imported.)

**Code to Add/Replace:**

### Step A — Add `@MockitoBean` declaration for the new use case

* **Location:** Inside the `CustomerQueryControllerTest` class body, immediately after the existing
  `@MockitoBean private GetCustomerByIdUseCase getCustomerById;` declaration.

* **Current code (context lines):**

```java
    @MockitoBean
    private GetCustomerByIdUseCase getCustomerById;

    @MockitoBean
    private CustomerWebMapper mapper;
```

* **Snippet (replace with):**

```java
    @MockitoBean
    private GetCustomerByIdUseCase getCustomerById;

    @MockitoBean
    private GetCustomersByIdsUseCase getCustomersByIds;

    @MockitoBean
    private CustomerWebMapper mapper;
```

---

### Step B — Add `@Nested class GetCustomersBatch`

* **Location:** Inside the `CustomerQueryControllerTest` class body, immediately after the closing `}` of
  the existing `@Nested class GetCustomerById` block.

* **Snippet:**

```java
    @Nested
    class GetCustomersBatch {

        @Test
        void allFound_returns200WithList() throws Exception {
            var id1 = UUID.randomUUID();
            var id2 = UUID.randomUUID();
            var cust1 = mock(Customer.class);
            var cust2 = mock(Customer.class);
            var resp1 = mock(CustomerResponse.class);
            var resp2 = mock(CustomerResponse.class);

            given(getCustomersByIds.execute(any())).willReturn(List.of(cust1, cust2));
            given(mapper.toResponses(List.of(cust1, cust2))).willReturn(List.of(resp1, resp2));

            mockMvc.perform(get(API_CUSTOMERS + "/batch")
                            .queryParam("ids", id1 + "," + id2))
                    .andExpect(status().isOk());
        }

        @Test
        void noneFound_returns200WithEmptyList() throws Exception {
            given(getCustomersByIds.execute(any())).willReturn(List.of());
            given(mapper.toResponses(List.of())).willReturn(List.of());

            mockMvc.perform(get(API_CUSTOMERS + "/batch")
                            .queryParam("ids", UUID.randomUUID().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Nested
        class BadRequest {

            @Test
            void whenIdsIsMissing() throws Exception {
                mockMvc.perform(get(API_CUSTOMERS + "/batch"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenIdCountExceeds100() throws Exception {
                var ids = IntStream.rangeClosed(1, 101)
                        .mapToObj(i -> UUID.randomUUID().toString())
                        .collect(Collectors.joining(","));

                mockMvc.perform(get(API_CUSTOMERS + "/batch")
                                .queryParam("ids", ids))
                        .andExpect(status().isBadRequest());
            }
        }
    }
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests CustomerQueryControllerTest
```

All tests in `CustomerQueryControllerTest` must pass (pre-existing tests are unaffected; the new nested
class adds 4 tests on top of the existing suite).
