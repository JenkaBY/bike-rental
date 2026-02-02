package com.github.jenkaby.bikerental.equipment.web.query;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentTypesUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.web.query.controller.EquipmentTypeQueryController;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentTypeMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = EquipmentTypeQueryController.class)
class EquipmentTypeQueryControllerTest {

    public static final String API_EQUIPMENT_TYPES = "/api/equipment-types";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetEquipmentTypesUseCase useCase;

    @MockitoBean
    private EquipmentTypeMapper mapper;

    @Nested
    class Get {

        @Nested
        class ShouldReturn200 {

            @Test
            void whenSingleTypeExists() throws Exception {
                var type = mock(EquipmentType.class);
                var response = new EquipmentTypeResponse("bicycle", "Bicycle", "Standard bicycles for rental");

                given(useCase.findAll()).willReturn(List.of(type));
                given(mapper.toResponse(type)).willReturn(response);

                mockMvc.perform(get(API_EQUIPMENT_TYPES))
                        .andExpect(status().isOk());
            }

            @Test
            void whenNoTypesExist() throws Exception {
                given(useCase.findAll()).willReturn(List.of());

                mockMvc.perform(get(API_EQUIPMENT_TYPES))
                        .andExpect(status().isOk());
            }
        }
    }
}
