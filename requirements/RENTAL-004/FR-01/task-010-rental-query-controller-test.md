# Task 010: Update RentalQueryControllerTest — Date Param Scenarios

> **Applied Skill:** `spring-mvc-controller-test` — @ApiTest, @MockitoBean, @Nested, parameterized tests;
> `java.instructions.md` — JUnit 5, MockMvc

## 1. Objective

Extend the existing `GetRentals` nested class in `RentalQueryControllerTest` to cover:

1. Happy-path combinations that include `from` and/or `to` date params (extend the existing
   `searchParameterCombinations` stream).
2. A bad-request test for an invalid date format (e.g. `"15-02-2026"`).

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/rental/web/query/RentalQueryControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports needed — `LocalDate` is not used directly in the test (dates are passed as query
param strings via `.param()`).

**Step 1 — Extend `searchParameterCombinations` to include date params.**

* **Location:** Inside `GetRentals`, replace the `searchParameterCombinations` static method.

Replace:

```java
        static Stream<Arguments> searchParameterCombinations() {
            var customerId = UUID.randomUUID().toString();
            return Stream.of(
                    Arguments.of("status only", "ACTIVE", null, null),
                    Arguments.of("customerId only", null, customerId, null),
                    Arguments.of("equipmentUid only", null, null, "BIKE-001"),
                    Arguments.of("status and customerId", "ACTIVE", customerId, null),
                    Arguments.of("status and equipmentUid", "ACTIVE", null, "BIKE-001"),
                    Arguments.of("all params", "ACTIVE", customerId, "BIKE-001"),
                    Arguments.of("no params", null, null, null)
            );
        }
```

With:

```java
        static Stream<Arguments> searchParameterCombinations() {
            var customerId = UUID.randomUUID().toString();
            return Stream.of(
                    Arguments.of("status only", "ACTIVE", null, null, null, null),
                    Arguments.of("customerId only", null, customerId, null, null, null),
                    Arguments.of("equipmentUid only", null, null, "BIKE-001", null, null),
                    Arguments.of("status and customerId", "ACTIVE", customerId, null, null, null),
                    Arguments.of("status and equipmentUid", "ACTIVE", null, "BIKE-001", null, null),
                    Arguments.of("all params", "ACTIVE", customerId, "BIKE-001", null, null),
                    Arguments.of("no params", null, null, null, null, null),
                    Arguments.of("from only", null, null, null, "2026-02-15", null),
                    Arguments.of("to only", null, null, null, null, "2026-02-20"),
                    Arguments.of("from and to", null, null, null, "2026-02-15", "2026-02-20"),
                    Arguments.of("status with date range", "ACTIVE", null, null, "2026-02-15", "2026-02-20"),
                    Arguments.of("same day range", null, null, null, "2026-02-15", "2026-02-15")
            );
        }
```

**Step 2 — Update the parameterized test method to accept and pass `from` and `to`.**

* **Location:** Inside `GetRentals`, replace the `getRentals_returnsOk` parameterized test.

Replace:

```java
        @ParameterizedTest(name = "{0}")
        @MethodSource("searchParameterCombinations")
        void getRentals_returnsOk(String description, String status, String customerId, String equipmentUid) throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var page = new Page<>(List.of(mock(Rental.class)), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(findRentalsUseCase.execute(any(FindRentalsUseCase.FindRentalsQuery.class))).willReturn(page);
            given(mapper.toRentalSummaryResponse(any(Rental.class))).willReturn(mock(RentalSummaryResponse.class));

            var request = get(API_RENTALS).accept(MediaType.APPLICATION_JSON);
            if (status != null) request = request.param("status", status);
            if (customerId != null) request = request.param("customerId", customerId);
            if (equipmentUid != null) request = request.param("equipmentUid", equipmentUid);

            mockMvc.perform(request)
                    .andExpect(status().isOk());
        }
```

With:

```java
        @ParameterizedTest(name = "{0}")
        @MethodSource("searchParameterCombinations")
        void getRentals_returnsOk(String description, String status, String customerId, String equipmentUid,
                                  String from, String to) throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var page = new Page<>(List.of(mock(Rental.class)), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(findRentalsUseCase.execute(any(FindRentalsUseCase.FindRentalsQuery.class))).willReturn(page);
            given(mapper.toRentalSummaryResponse(any(Rental.class))).willReturn(mock(RentalSummaryResponse.class));

            var request = get(API_RENTALS).accept(MediaType.APPLICATION_JSON);
            if (status != null) request = request.param("status", status);
            if (customerId != null) request = request.param("customerId", customerId);
            if (equipmentUid != null) request = request.param("equipmentUid", equipmentUid);
            if (from != null) request = request.param("from", from);
            if (to != null) request = request.param("to", to);

            mockMvc.perform(request)
                    .andExpect(status().isOk());
        }
```

**Step 3 — Add a bad-request test for invalid date format.**

* **Location:** Inside `GetRentals`, add after the existing `getRentals_invalidStatus` test.

```java
        @Test
        void getRentals_invalidFromDateFormat_returnsBadRequest() throws Exception {
            mockMvc.perform(get(API_RENTALS)
                            .param("from", "15-02-2026")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getRentals_invalidToDateFormat_returnsBadRequest() throws Exception {
            mockMvc.perform(get(API_RENTALS)
                            .param("to", "20-02-2026")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
```

## 4. Validation Steps

skip