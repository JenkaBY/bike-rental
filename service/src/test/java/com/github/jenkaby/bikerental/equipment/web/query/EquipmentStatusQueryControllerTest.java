package com.github.jenkaby.bikerental.equipment.web.query;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentStatusesUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.web.query.controller.EquipmentStatusQueryController;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentStatusMapper;
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

@ApiTest(controllers = EquipmentStatusQueryController.class)
class EquipmentStatusQueryControllerTest {

    public static final String API_EQUIPMENT_STATUSES = "/api/equipment-statuses";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetEquipmentStatusesUseCase useCase;

    @MockitoBean
    private EquipmentStatusMapper mapper;

    @Nested
    class Get {

        @Nested
        class ShouldReturn200 {

            @Test
            void whenSingleStatusExists() throws Exception {
                var status = mock(EquipmentStatus.class);
                var response = new EquipmentStatusResponse("available", "Available", "Equipment is available for rental");

                given(useCase.findAll()).willReturn(List.of(status));
                given(mapper.toResponse(status)).willReturn(response);

                mockMvc.perform(get(API_EQUIPMENT_STATUSES))
                        .andExpect(status().isOk());
            }

            @Test
            void whenNoStatusesExist() throws Exception {
                given(useCase.findAll()).willReturn(List.of());

                mockMvc.perform(get(API_EQUIPMENT_STATUSES))
                        .andExpect(status().isOk());
            }
        }
    }
}
