package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetPricingTypesUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.vo.PricingTypeInfo;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingTypeResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffV2QueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = {PricingTypeQueryController.class})
@DisplayName("Pricing Type Query Controller Tests")
class PricingTypeQueryControllerTest {

    private static final String API_V2_TARIFFS = "/api/tariffs";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPricingTypesUseCase getPricingTypesUseCase;

    @MockitoBean
    private TariffV2QueryMapper mapper;

    @Test
    @DisplayName("GET /api/tariffs/pricing-types returns 200 with list")
    void getPricingTypes_returns200() throws Exception {
        given(getPricingTypesUseCase.execute()).willReturn(List.of(
                new PricingTypeInfo("DEGRESSIVE_HOURLY", "Degressive Hourly", "Desc")
        ));
        given(mapper.toResponse(any(PricingTypeInfo.class))).willReturn(new PricingTypeResponse(
                "DEGRESSIVE_HOURLY", "Degressive Hourly", "Desc"
        ));

        mockMvc.perform(get(API_V2_TARIFFS + "/pricing-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].slug").value("DEGRESSIVE_HOURLY"));
    }
}


