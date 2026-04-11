# Task 011: WebMvc Tests for `RentalCommandController` — Insufficient Funds Scenario

> **Applied Skill:** `spring-mvc-controller-test` — Every new error path on a `@RestController` must be covered by a
`@WebMvcTest` slice test using `@ApiTest`. Add a new `@Nested` class under the existing `PostRentals` nested class.

## 1. Objective

Add a test case to `RentalCommandControllerTest` to verify that when `CreateRentalUseCase.execute(...)` throws
`InsufficientBalanceException`, the controller returns `422 Unprocessable Entity` with
`errorCode = rental.insufficient_funds` in the response body.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Step A — Add imports:**

Add the following imports at the top of the test file, after the existing imports:

```java
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
```

**Step B — Add `ShouldReturn422` nested class:**

* **Location:** Inside the `PostRentals` nested class, directly **after** the existing `ShouldReturn400` nested class.

```java
        @Nested
        @DisplayName("Should return 422 Unprocessable Entity")
        class ShouldReturn422 {

            @Test
            @DisplayName("when finance module throws InsufficientBalanceException")
            void whenInsufficientBalance() throws Exception {
                var request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        null
                );

                given(commandMapper.toCreateCommand(any(CreateRentalRequest.class)))
                        .willReturn(mock(CreateRentalUseCase.CreateRentalCommand.class));
                given(createRentalUseCase.execute(any(CreateRentalUseCase.CreateRentalCommand.class)))
                        .willThrow(new InsufficientBalanceException(Money.of("30.00"), Money.of("60.00")));

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.errorCode").value(ErrorCodes.INSUFFICIENT_FUNDS));
            }
        }
```

> **Note on `@MockitoBean` for `FinanceFacade`:** `FinanceFacade` is called by `CreateRentalService`, not directly by
> the controller. The controller test stubs `createRentalUseCase` to throw the exception directly — no additional
`@MockitoBean` for `FinanceFacade` is needed here.

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests "com.github.jenkaby.bikerental.rental.web.command.RentalCommandControllerTest"
```
