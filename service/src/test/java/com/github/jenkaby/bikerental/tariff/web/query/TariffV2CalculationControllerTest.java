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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TariffV2CalculationController.class)
@DisplayName("Tariff V2 Calculation Controller Tests")
class TariffV2CalculationControllerTest {

    private static final String API_V2_CALCULATE = "/api/v2/tariffs/calculate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TariffV2Facade tariffV2Facade;

    @MockitoBean
    private BatchCalculationMapper batchMapper;

    @Test
    @DisplayName("POST /api/v2/tariffs/calculate returns 200 with normal mode result")
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
        @Test
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when equipments list is empty")
        void whenEquipmentsEmpty_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(),
                    60,
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
                    .andExpect(jsonPath("$.errors[0].field").value("equipments"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.not_empty"));
        }

        @Test
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when equipmentType is blank")
        void whenEquipmentTypeBlank_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest(" ")),
                    60,
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
                    .andExpect(jsonPath("$.errors[0].field").value("equipments[0].equipmentType"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.not_blank"));
        }

        @Test
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when plannedDurationMinutes is null")
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

        @Test
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when discountPercent out of range")
        void whenDiscountPercentOutOfRange_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    101,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value("discountPercent"));
        }

        @Test
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when discountPercent is negative")
        void whenDiscountPercentNegative_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    -1,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post(API_V2_CALCULATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].field").value("discountPercent"));
        }

        @Test
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when specialTariffId provided without specialPrice")
        void whenSpecialTariffIdWithoutPrice_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    null,
                    1L,
                    null,
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
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when specialPrice provided without specialTariffId")
        void whenSpecialPriceWithoutTariffId_returns400() throws Exception {
            var request = new CostCalculationRequest(
                    List.of(new CostCalculationRequest.EquipmentItemRequest("bicycle")),
                    60,
                    null,
                    null,
                    null,
                    new java.math.BigDecimal("9.99"),
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
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when specialPrice is negative")
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
        @DisplayName("POST /api/v2/tariffs/calculate returns 400 when specialTariffId is zero")
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
