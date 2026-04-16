package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.application.usecase.*;
import com.github.jenkaby.bikerental.tariff.domain.model.DegressiveHourlyTariffV2;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2Status;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffV2QueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = {TariffV2QueryController.class})
@DisplayName("Tariff V2 Query Controller Tests")
class TariffV2QueryControllerTest {

    private static final String API_V2_TARIFFS = "/api/tariffs";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTariffV2ByIdUseCase getByIdUseCase;

    @MockitoBean
    private GetAllTariffsV2UseCase getAllUseCase;

    @MockitoBean
    private GetActiveTariffsV2ByEquipmentTypeUseCase getActiveByTypeUseCase;

    @MockitoBean
    private GetPricingTypesUseCase getPricingTypesUseCase;

    @MockitoBean
    private SelectTariffV2UseCase selectTariffUseCase;

    @MockitoBean
    private TariffV2QueryMapper mapper;

    private static final PricingParams EMPTY_PARAMS = new PricingParams(
            null, null, null, null, null, null, null, null, null, null);

    @Test
    @DisplayName("GET /api/tariffs/{id} returns 200 when found")
    void getById_returns200() throws Exception {
        TariffV2 tariff = new DegressiveHourlyTariffV2(
                1L, "T", null, "bicycle", "v2",
                LocalDate.now(), null, TariffV2Status.ACTIVE,
                Money.of("0"), Money.of("0"), Money.of("0"), 1, Money.of("0")
        );
        given(getByIdUseCase.get(1L)).willReturn(tariff);
        given(mapper.toResponse(tariff)).willReturn(new TariffV2Response(
                1L, "T", null, "bicycle", PricingType.DEGRESSIVE_HOURLY,
                EMPTY_PARAMS,
                LocalDate.now(), null, "v2", TariffV2Status.ACTIVE
        ));

        mockMvc.perform(get(API_V2_TARIFFS + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("T"));
    }

    @Test
    @DisplayName("GET /api/tariffs/{id} returns 404 when not found")
    void getById_returns404() throws Exception {
        given(getByIdUseCase.get(999L)).willThrow(new ResourceNotFoundException(TariffV2.class, "999"));
        mockMvc.perform(get(API_V2_TARIFFS + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tariffs returns 200 with page")
    void getAll_returns200() throws Exception {
        PageRequest pr = new PageRequest(20, 0, null);
        given(getAllUseCase.execute(any())).willReturn(Page.empty(pr));
        mockMvc.perform(get(API_V2_TARIFFS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(0));
    }


    @Nested
    @DisplayName("GET /api/tariffs/selection tests")
    class Selection {

        @Test
        @DisplayName("GET /api/tariffs/selection returns 200 when tariff found")
        void selection_returns200() throws Exception {
            TariffV2 tariff = new DegressiveHourlyTariffV2(
                    1L, "T", null, "bicycle", "v2",
                    LocalDate.now(), null, TariffV2Status.ACTIVE,
                    Money.of("9"), Money.of("0"), Money.of("0"), 1, Money.of("0")
            );
            given(selectTariffUseCase.execute(any(SelectTariffV2UseCase.SelectTariffCommand.class))).willReturn(tariff);
            given(mapper.toResponse(tariff)).willReturn(new TariffV2Response(
                    1L, "T", null, "bicycle", PricingType.DEGRESSIVE_HOURLY,
                    EMPTY_PARAMS,
                    LocalDate.now(), null, "v2", TariffV2Status.ACTIVE
            ));

            mockMvc.perform(get(API_V2_TARIFFS + "/selection")
                            .param("equipmentType", "bicycle").param("durationMinutes", "60"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCost").value(9))
                    .andExpect(jsonPath("$.calculationBreakdown.breakdownPatternCode").value("breakdown.cost.degressive_hourly.standard"))
                    .andExpect(jsonPath("$.calculationBreakdown.message").value("1h 0min degressive: 9 = 9"));
        }
    }
}
