# Task 011: Update TariffV2CalculationControllerTest — fix constructors and add V2 tests

> **Applied Skill:** `spring-mvc-controller-test` — @ApiTest, @MockitoBean, @Nested, parameterized bad-request tests

## 1. Objective

Two changes in this task:

1. **Fix existing `BaseEquipmentCostBreakdown`-based response constructors** — after task-002 added `equipmentId` as
   the first field of `EquipmentCostBreakdownResponse`, all existing
   `new CostCalculationResponse.EquipmentCostBreakdownResponse(...)` calls in the test need `null` prepended as the
   first argument.

2. **Add a new `@Nested` class** for `PUT /api/tariffs/calculations` covering:
    - Happy path (200 OK, verifies facade wiring)
    - Validation failures (400 Bad Request) for missing `startAt`, empty `equipments`, missing
      `plannedDurationMinutes`, and special-tariff consistency

## 2. File to Modify

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/tariff/web/query/TariffV2CalculationControllerTest.java`
* **Action:** Modify Existing File

---

### Change 1 — Add missing imports

* **Location:** Import section at the top of the file.
* **Add the following imports** (after the existing `import java.math.BigDecimal;` line):

```java
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
```

---

### Change 2 — Fix all existing `EquipmentCostBreakdownResponse` constructor calls

The `CostCalculationResponse.EquipmentCostBreakdownResponse` record now has `@Nullable Long equipmentId` as its
**first** field. All constructor calls in the test currently pass 9 arguments; each needs `null` prepended to become
10 arguments.

There are **2 occurrences** inside the `calculate_normalMode_returns200` test — both follow this pattern:

* **Remove (9-arg form):**

```java
                        new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                new BigDecimal("16"), 120, 0, 0,
                                new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                        "2h degressive: 16.00",
                                        new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                )
                        ),
```

* **Replace with (10-arg form, `null` prepended):**

```java
                        new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                null, "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                new BigDecimal("16"), 120, 0, 0,
                                new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                        "2h degressive: 16.00",
                                        new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                )
                        ),
```

Apply the same fix to the **second occurrence** (the last item in the list), which ends with `)` instead of `),`:

* **Remove (9-arg form):**

```java
                        new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                new BigDecimal("16"), 120, 0, 0,
                                new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                        "2h degressive: 16.00",
                                        new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                )
                        )
```

* **Replace with:**

```java
                        new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                null, "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                new BigDecimal("16"), 120, 0, 0,
                                new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                        "2h degressive: 16.00",
                                        new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                )
                        )
```

---

### Change 3 — Add the constant for the V2 endpoint URL

* **Location:** After the existing `private static final String API_V2_CALCULATE = "/api/tariffs/calculate";` line.
* **Add:**

```java
    private static final String API_V2_CALCULATIONS = "/api/tariffs/calculations";
```

---

### Change 4 — Add V2 nested test class

* **Location:** Add the following class just **before** the closing `}` of the top-level
  `TariffV2CalculationControllerTest` class, after the closing `}` of the existing `ValidationFails` nested class.
* **Snippet:**

```java
    @Nested
    @DisplayName("PUT /api/tariffs/calculations")
    class CostCalculationsV2 {

        @Test
        @DisplayName("returns 200 with V2 normal mode result")
        void costCalculations_normalMode_returns200() throws Exception {
            var request = new CostCalculationV2Request(
                    List.of(
                            new CostCalculationV2Request.EquipmentItemRequest(101L, "bicycle",
                                    LocalDateTime.of(2024, 6, 1, 12, 0)),
                            new CostCalculationV2Request.EquipmentItemRequest(102L, "bicycle", null)
                    ),
                    LocalDateTime.of(2024, 6, 1, 10, 0),
                    120,
                    null,
                    null,
                    null
            );
            var breakdowns = List.<com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown>of(
                    new com.github.jenkaby.bikerental.tariff.domain.service.EquipmentCostBreakdownV2(
                            101L, "bicycle", 1L, "Hourly Bicycle", PricingType.DEGRESSIVE_HOURLY.name(),
                            Money.of("16"), Duration.ofMinutes(120), Duration.ZERO, Duration.ZERO,
                            new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                    "2h degressive: 16.00",
                                    new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                            )
                    ),
                    new com.github.jenkaby.bikerental.tariff.domain.service.EquipmentCostBreakdownV2(
                            102L, "bicycle", 1L, "Hourly Bicycle", PricingType.DEGRESSIVE_HOURLY.name(),
                            Money.of("16"), Duration.ofMinutes(120), Duration.ZERO, Duration.ZERO,
                            new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                    "2h degressive: 16.00",
                                    new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                            )
                    )
            );
            var result = new BaseRentalCostCalculationResult(
                    breakdowns,
                    Money.of("32"),
                    new DiscountDetail(new DiscountPercent(new BigDecimal("10")), Money.of("3.20")),
                    Money.of("28.80"),
                    Duration.ofMinutes(120),
                    true,
                    false
            );
            var response = new CostCalculationResponse(
                    List.of(
                            new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                    101L, "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                    new BigDecimal("16"), 120, 0, 0,
                                    new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                            "2h degressive: 16.00",
                                            new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                    )
                            ),
                            new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                    102L, "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                    new BigDecimal("16"), 120, 0, 0,
                                    new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                            "2h degressive: 16.00",
                                            new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                    )
                            )
                    ),
                    new BigDecimal("32"),
                    new CostCalculationResponse.DiscountDetailResponse(new BigDecimal("10"), new BigDecimal("3.20")),
                    new BigDecimal("28.80"),
                    120,
                    true,
                    false
            );

            given(batchMapper.toV2Command(any())).willReturn(null);
            given(tariffV2Facade.calculateRentalCostV2(any())).willReturn(result);
            given(batchMapper.toResponse(result)).willReturn(response);

            mockMvc.perform(put(API_V2_CALCULATIONS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCost").value(28.80))
                    .andExpect(jsonPath("$.subtotal").value(32))
                    .andExpect(jsonPath("$.estimate").value(true))
                    .andExpect(jsonPath("$.equipmentBreakdowns.length()").value(2))
                    .andExpect(jsonPath("$.equipmentBreakdowns[0].equipmentId").value(101))
                    .andExpect(jsonPath("$.equipmentBreakdowns[1].equipmentId").value(102));
        }

        @Nested
        class ValidationFails {

            public static Stream<Arguments> invalidEquipmentRequests() {
                return Stream.of(
                        Arguments.of(
                                new CostCalculationV2Request(List.of(), LocalDateTime.of(2024, 6, 1, 10, 0), 60, null, null, null),
                                "equipments",
                                "validation.not_empty"
                        ),
                        Arguments.of(
                                new CostCalculationV2Request(
                                        List.of(new CostCalculationV2Request.EquipmentItemRequest(1L, " ", null)),
                                        LocalDateTime.of(2024, 6, 1, 10, 0), 60, null, null, null),
                                "equipments[0].equipmentType",
                                "validation.not_blank"
                        ),
                        Arguments.of(
                                new CostCalculationV2Request(
                                        List.of(new CostCalculationV2Request.EquipmentItemRequest(null, "bicycle", null)),
                                        LocalDateTime.of(2024, 6, 1, 10, 0), 60, null, null, null),
                                "equipments[0].equipmentId",
                                "validation.not_null"
                        )
                );
            }

            @MethodSource("invalidEquipmentRequests")
            @ParameterizedTest
            void whenEquipmentRequestInvalid_returns400(CostCalculationV2Request request, String expectedField, String expectedCode) throws Exception {
                mockMvc.perform(put(API_V2_CALCULATIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                        .andExpect(jsonPath("$.errors[0].code").value(expectedCode));
            }

            @Test
            @DisplayName("PUT /api/tariffs/calculations returns 400 when startAt is null")
            void whenStartAtNull_returns400() throws Exception {
                var request = new CostCalculationV2Request(
                        List.of(new CostCalculationV2Request.EquipmentItemRequest(1L, "bicycle", null)),
                        null,
                        60,
                        null, null, null
                );

                mockMvc.perform(put(API_V2_CALCULATIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("startAt"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_null"));
            }

            @Test
            @DisplayName("PUT /api/tariffs/calculations returns 400 when plannedDurationMinutes is null")
            void whenPlannedDurationNull_returns400() throws Exception {
                var request = new CostCalculationV2Request(
                        List.of(new CostCalculationV2Request.EquipmentItemRequest(1L, "bicycle", null)),
                        LocalDateTime.of(2024, 6, 1, 10, 0),
                        null,
                        null, null, null
                );

                mockMvc.perform(put(API_V2_CALCULATIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("plannedDurationMinutes"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_null"));
            }

            public static Stream<Arguments> invalidSpecialTariffConsistency() {
                return Stream.of(
                        Arguments.of(1L, null),
                        Arguments.of(null, new BigDecimal("9.99"))
                );
            }

            @MethodSource("invalidSpecialTariffConsistency")
            @ParameterizedTest
            void whenSpecialTariffConsistencyInvalid_returns400(Long tariffId, BigDecimal price) throws Exception {
                var request = new CostCalculationV2Request(
                        List.of(new CostCalculationV2Request.EquipmentItemRequest(1L, "bicycle", null)),
                        LocalDateTime.of(2024, 6, 1, 10, 0),
                        60,
                        null,
                        tariffId,
                        price
                );

                mockMvc.perform(put(API_V2_CALCULATIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.special_tariff_consistency"));
            }
        }
    }
```

## 4. Validation Steps

skip