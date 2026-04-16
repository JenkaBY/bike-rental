package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.DiscountDetail;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseEquipmentCostBreakdown;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.BatchCalculationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TariffV2CalculationController.class)
@DisplayName("Tariff V2 Calculation Controller Tests")
class TariffV2CalculationControllerTest {

    private static final String API_V2_CALCULATE = "/api/tariffs/calculate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TariffV2Facade tariffV2Facade;

    @MockitoBean
    private BatchCalculationMapper batchMapper;

    @Test
    @DisplayName("POST /api/tariffs/calculate returns 200 with normal mode result")
    void calculate_normalMode_returns200() throws Exception {
        var request = new CostCalculationRequest(
                List.of(
                        new CostCalculationRequest.EquipmentItemRequest("bicycle"),
                        new CostCalculationRequest.EquipmentItemRequest("bicycle")
                ),
                120,
                null,
                10,
                null,
                null,
                null
        );
        var breakdowns = List.<com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown>of(
                new BaseEquipmentCostBreakdown(
                        "bicycle", 1L, "Hourly Bicycle", PricingType.DEGRESSIVE_HOURLY.name(),
                        Money.of("16"), Duration.ofMinutes(120), Duration.ZERO, Duration.ZERO,
                        new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                "2h degressive: 16.00",
                                new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                        )
                ),
                new BaseEquipmentCostBreakdown(
                        "bicycle", 1L, "Hourly Bicycle", PricingType.DEGRESSIVE_HOURLY.name(),
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
                                "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
                                new BigDecimal("16"), 120, 0, 0,
                                new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard(
                                        "2h degressive: 16.00",
                                        new com.github.jenkaby.bikerental.tariff.BreakdownCostDetails.FlatHourlyStandard.Details(2, 0, "16.00", "16.00")
                                )
                        ),
                        new CostCalculationResponse.EquipmentCostBreakdownResponse(
                                "bicycle", 1L, "Hourly Bicycle", "DEGRESSIVE_HOURLY",
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

        given(batchMapper.toCommand(any())).willReturn(null);
        given(tariffV2Facade.calculateRentalCost(any())).willReturn(result);
        given(batchMapper.toResponse(result)).willReturn(response);

        mockMvc.perform(post(API_V2_CALCULATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(28.80))
                .andExpect(jsonPath("$.subtotal").value(32))
                .andExpect(jsonPath("$.estimate").value(true))
                .andExpect(jsonPath("$.equipmentBreakdowns.length()").value(2));
    }

    @Nested
    class ValidationFails {

        // --- equipments related tests (empty list, blank equipmentType) ---
        public static Stream<Arguments> invalidEquipmentRequests() {
            return Stream.of(
                    Arguments.of(
                            new CostCalculationRequest(List.of(), 60, null, null, null, null, null),
                            "equipments",
                            "validation.not_empty"
                    ),
                    Arguments.of(
                            new CostCalculationRequest(List.of(new CostCalculationRequest.EquipmentItemRequest(" ")), 60, null, null, null, null, null),
                            "equipments[0].equipmentType",
                            "validation.not_blank"
                    )
            );
        }

        @MethodSource("invalidEquipmentRequests")
        @ParameterizedTest
        void whenEquipmentRequestInvalid_returns400(CostCalculationRequest request, String expectedField, String expectedCode) throws Exception {
            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].code").value(expectedCode));
        }

        // --- plannedDuration null (unique) ---
        @Test
        @DisplayName("POST /api/tariffs/calculate returns 400 when plannedDurationMinutes is null")
        void whenPlannedDurationNull_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value("plannedDurationMinutes"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.not_null"));
        }

        // --- discountPercent boundary tests ---
        public static Stream<Arguments> invalidDiscounts() {
            return Stream.of(
                    Arguments.of(-1, "validation.min"),
                    Arguments.of(101, "validation.max")
            );
        }

        @MethodSource("invalidDiscounts")
        @ParameterizedTest
        void whenDiscountPercentInvalid_returns400(Integer discountValue, String expectedCode) throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    discountValue,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value("discountPercent"))
                    .andExpect(jsonPath("$.errors[0].code").value(expectedCode));
        }

        // --- special tariff consistency (object-level) ---
        public static Stream<Arguments> invalidSpecialTariffConsistency() {
            return Stream.of(
                    Arguments.of(1L, null),
                    Arguments.of(null, new BigDecimal("9.99"))
            );
        }

        @MethodSource("invalidSpecialTariffConsistency")
        @ParameterizedTest
        void whenSpecialTariffConsistencyInvalid_returns400(Long tariffId, BigDecimal price) throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    null,
                    tariffId,
                    price,
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.special_tariff_consistency"));
        }

        @Test
        @DisplayName("POST /api/tariffs/calculate returns 400 when specialPrice is negative")
        void whenSpecialPriceNegative_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    null,
                    null,
                    new BigDecimal("-1"),
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value("specialPrice"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.decimal_min"));
        }

        @Test
        @DisplayName("POST /api/tariffs/calculate returns 400 when specialTariffId is zero")
        void whenSpecialTariffIdZero_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    null,
                    0L,
                    null,
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value("specialTariffId"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.positive"));
        }
    }
}
