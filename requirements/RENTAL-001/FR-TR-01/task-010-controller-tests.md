# Task 010: Write Controller Tests for New `CreateRentalRequest` Validation Rules

> **Applied Skill:** `.github/skills/spring-mvc-controller-test/SKILL.md` — @ApiTest, @Nested, @ParameterizedTest,
> mutual-exclusivity cross-field, range validation

## 1. Objective

Extend `RentalCommandControllerTest` with new `@Nested` test cases (inside the existing `PostRentals` →
`ShouldReturn400` class) that verify:

1. `discountPercent` below 0 → `400 Bad Request`
2. `discountPercent` above 100 → `400 Bad Request`
3. Both `specialTariffId` and `discountPercent` set simultaneously → `400 Bad Request`

No changes to existing passing test methods.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
// All other imports already present (Stream, Arguments, ParameterizedTest, etc.)
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
```

**Code to Add/Replace:**

* **Location:** Inside the existing `ShouldReturn400` nested class within `PostRentals`, **after the last
  existing test method** (the `whenDurationIsNull` method). Add the following three test methods and one
  factory method:

```java
            public static Stream<Arguments> invalidDiscountPercentRequests() {
                return Stream.of(
                        Arguments.of(-1, "discountPercent must be between 0 and 100"),
                        Arguments.of(101, "discountPercent must be between 0 and 100")
                );
            }

            @ParameterizedTest(name = "discountPercent={0}")
            @MethodSource("invalidDiscountPercentRequests")
            @DisplayName("when discountPercent is out of range")
            void whenDiscountPercentIsOutOfRange(Integer discountPercent, String expectedMessage) throws Exception {
                var body = """
                        {
                          "customerId": "%s",
                          "equipmentIds": [1],
                          "duration": "PT2H",
                          "operatorId": "operator-1",
                          "discountPercent": %d
                        }
                        """.formatted(VALID_CUSTOMER_ID, discountPercent);

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field == 'discountPercent')].message")
                                .value(expectedMessage));

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @Test
            @DisplayName("when specialTariffId and discountPercent are both set")
            void whenSpecialTariffIdAndDiscountPercentAreBothSet() throws Exception {
                var body = """
                        {
                          "customerId": "%s",
                          "equipmentIds": [1],
                          "duration": "PT2H",
                          "operatorId": "operator-1",
                          "specialTariffId": 99,
                          "discountPercent": 10
                        }
                        """.formatted(VALID_CUSTOMER_ID);

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field == 'isSpecialTariffAndDiscountMutuallyExclusive')].message")
                                .value("specialTariffId and discountPercent are mutually exclusive"));

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }
```

Also add a happy-path test inside the existing `ShouldReturn201` nested class to verify optional pricing
fields are accepted:

* **Location:** Inside `ShouldReturn201`, after the last existing test method. Add:

```java
            @Test
            @DisplayName("when request contains discountPercent")
            void whenRequestContainsDiscountPercent() throws Exception {
                var body = """
                        {
                          "customerId": "%s",
                          "equipmentIds": [1],
                          "duration": "PT2H",
                          "operatorId": "operator-1",
                          "discountPercent": 10
                        }
                        """.formatted(VALID_CUSTOMER_ID);

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toCreateCommand(any(CreateRentalRequest.class)))
                        .willReturn(mock(CreateRentalUseCase.CreateRentalCommand.class));
                given(createRentalUseCase.execute(any(CreateRentalUseCase.CreateRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated());

                verify(createRentalUseCase).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @Test
            @DisplayName("when request contains specialTariffId and specialPrice")
            void whenRequestContainsSpecialTariffIdAndSpecialPrice() throws Exception {
                var body = """
                        {
                          "customerId": "%s",
                          "equipmentIds": [1],
                          "duration": "PT2H",
                          "operatorId": "operator-1",
                          "specialTariffId": 99,
                          "specialPrice": 15.00
                        }
                        """.formatted(VALID_CUSTOMER_ID);

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toCreateCommand(any(CreateRentalRequest.class)))
                        .willReturn(mock(CreateRentalUseCase.CreateRentalCommand.class));
                given(createRentalUseCase.execute(any(CreateRentalUseCase.CreateRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated());

                verify(createRentalUseCase).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
```
