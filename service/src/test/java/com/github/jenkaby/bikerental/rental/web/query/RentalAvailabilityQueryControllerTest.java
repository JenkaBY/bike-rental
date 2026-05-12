package com.github.jenkaby.bikerental.rental.web.query;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.rental.application.usecase.GetAvailableForRentEquipmentsUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalAvailabilityQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalAvailabilityQueryController.class)
class RentalAvailabilityQueryControllerTest {

    private static final String API_AVAILABLE_EQUIPMENTS = "/api/rentals/available-equipments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAvailableForRentEquipmentsUseCase getAvailableForRentEquipmentsUseCase;

    @MockitoBean
    private RentalAvailabilityQueryMapper mapper;

    @MockitoBean
    private PageMapper pageMapper;

    @Nested
    class GetAvailableEquipments {

        @Test
        void getAvailableEquipments_returnsOk_withResults() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var domain = new AvailableForRentalEquipment(1L, "SN-001", "BIKE-001", "mountain-bike", "Trek Marlin");
            var response = new AvailableEquipmentResponse(1L, "BIKE-001", "SN-001", "mountain-bike", "Trek Marlin");
            var domainPage = new Page<>(List.of(domain), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(getAvailableForRentEquipmentsUseCase.getAvailableEquipments(any(EquipmentSearchFilter.class), any(PageRequest.class)))
                    .willReturn(domainPage);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_AVAILABLE_EQUIPMENTS)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.totalItems").value(1));
        }

        @Test
        void getAvailableEquipments_returnsOk_withEmptyResult() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var emptyPage = Page.<AvailableForRentalEquipment>empty(pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(getAvailableForRentEquipmentsUseCase.getAvailableEquipments(any(EquipmentSearchFilter.class), any(PageRequest.class)))
                    .willReturn(emptyPage);

            mockMvc.perform(get(API_AVAILABLE_EQUIPMENTS)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(0))
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        void getAvailableEquipments_withTextFilter_returnsOk() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var domain = new AvailableForRentalEquipment(2L, "SN-002", "MTB-001", "mountain-bike", "Trek MTB");
            var response = new AvailableEquipmentResponse(2L, "MTB-001", "SN-002", "mountain-bike", "Trek MTB");
            var domainPage = new Page<>(List.of(domain), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(getAvailableForRentEquipmentsUseCase.getAvailableEquipments(any(EquipmentSearchFilter.class), any(PageRequest.class)))
                    .willReturn(domainPage);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_AVAILABLE_EQUIPMENTS)
                            .param("q", "MTB")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1));
        }
    }
}