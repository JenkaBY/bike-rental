package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TariffQueryController.class)
@DisplayName("Tariff Query Controller WebMvc Tests")
class TariffQueryControllerTest {

    private static final String API_TARIFFS = "/api/tariffs";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTariffByIdUseCase getByIdUseCase;

    @MockitoBean
    private GetAllTariffsUseCase getAllUseCase;

    @MockitoBean
    private GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase;

    @MockitoBean
    private TariffQueryMapper mapper;

    @Nested
    @DisplayName("GET /api/tariffs/{id}")
    class GetById {

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(longs = {0L, -1L})
            @DisplayName("when id is not positive")
            void getById_shouldReturnBadRequest_whenIdInvalid(long id) throws Exception {
                mockMvc.perform(get(API_TARIFFS + "/{id}", id))
                        .andExpect(status().isBadRequest()).andExpect(
                                jsonPath("$.detail").value(
                                        containsString("must be greater than 0")
                                ));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/tariffs/active")
    class GetActiveByType {

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @Test
            @DisplayName("when equipmentType is missing")
            void getActive_shouldReturnBadRequest_whenEquipmentTypeMissing() throws Exception {
                mockMvc.perform(get(API_TARIFFS + "/active"))
                        .andExpect(status().isBadRequest())
                        .andExpect(
                                jsonPath("$.detail").value(
                                        containsString("Required request parameter 'equipmentType' for method parameter type String is not present")
                                ));
            }
        }
    }
}
