# Task 004: Add WebMvc tests for `GET /api/customers/{id}`

> **Applied Skill:** spring-mvc-controller-test - follow `@ApiTest` conventions, `@MockitoBean` usage and parameterized
> bad-request tests

## 1. Objective

Add controller-slice tests that verify the new `GET /api/customers/{id}` endpoint returns 200 with `CustomerResponse`,
404 when not found, and 400 for invalid UUID formats.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryControllerTest.java`
* **Action:** Modify Existing File (extend the existing test class with new tests)

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
```

**Code to Add/Replace:**

* **Location:** In `CustomerQueryControllerTest`, add a new `@Nested` class `GetCustomerById` within the test class.
* **Snippet:**

```java

@Nested
class GetCustomerById {

    @Test
    void shouldReturn200WhenCustomerExists() throws Exception {
        var id = UUID.randomUUID();
        var customerInfo = new CustomerInfo(id, "+79991234001", "Alex", "Doe", null, null, null);
        var response = new CustomerResponse(customerInfo.id(), customerInfo.phone(), customerInfo.firstName(), customerInfo.lastName(), null, null, null);

        given(customerQueryUseCase.findById(id)).willReturn(Optional.of(customerInfo));
        given(customerQueryMapper.toResponse(customerInfo)).willReturn(response);

        mockMvc.perform(get(API_CUSTOMERS + "/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.phone").value("+79991234001"))
                .andExpect(jsonPath("$.firstName").value("Alex"));
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        var id = UUID.randomUUID();
        given(customerQueryUseCase.findById(id)).willReturn(Optional.empty());

        mockMvc.perform(get(API_CUSTOMERS + "/{id}", id.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("shared.resource.not_found"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-uuid", "", "123"})
    void shouldReturn400ForInvalidUuid(String path) throws Exception {
        mockMvc.perform(get(API_CUSTOMERS + "/{id}", path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }
}
```

Notes:

- Declare `customerQueryUseCase` and `customerQueryMapper` as `@MockitoBean` (already present in class). Use
  `given(...)` stubbing and verify `status()` and JSON body as shown.

## 4. Validation Steps

Run only the controller test class:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests com.github.jenkaby.bikerental.customer.web.query.CustomerQueryControllerTest
```
