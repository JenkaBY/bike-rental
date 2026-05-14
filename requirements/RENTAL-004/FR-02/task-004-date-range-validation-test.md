# Task 004: Add Date Range Validation Tests to RentalQueryControllerTest

> **Applied Skill:** `spring-mvc-controller-test` — @ApiTest, @MockitoBean, negative path tests;
> `java.instructions.md` — JUnit 5, MockMvc, asserting error response fields

## 1. Objective

Add a new `@Nested` class `GetRentalsDateRangeValidation` inside `RentalQueryControllerTest` that
covers the `from > to` invalid input scenario (HTTP 400 with `CONSTRAINT_VIOLATION` error code)
and confirms that the same-day range (`from == to`) returns HTTP 200.

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/rental/web/query/RentalQueryControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
```

**Code to Add/Replace:**

* **Location:** Add a new `@Nested` class after the closing `}` of the existing `GetRentals`
  nested class, before the final closing `}` of `RentalQueryControllerTest`.

```java
    @Nested
    class GetRentalsDateRangeValidation {

        @Test
        void getRentals_fromAfterTo_returnsBadRequest() throws Exception {
            mockMvc.perform(get(API_RENTALS)
                            .param("from", "2026-02-20")
                            .param("to", "2026-02-15")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCodes.CONSTRAINT_VIOLATION));
        }

        @Test
        void getRentals_fromEqualsTo_returnsOk() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var page = new Page<>(List.of(mock(Rental.class)), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(findRentalsUseCase.execute(any(FindRentalsUseCase.FindRentalsQuery.class))).willReturn(page);
            given(mapper.toRentalSummaryResponse(any(Rental.class))).willReturn(mock(RentalSummaryResponse.class));

            mockMvc.perform(get(API_RENTALS)
                            .param("from", "2026-02-15")
                            .param("to", "2026-02-15")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
```

## 4. Validation Steps

skip
