package com.github.jenkaby.bikerental.equipment.web.query;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentBySerialNumberUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByUidUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.web.query.controller.EquipmentQueryController;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = EquipmentQueryController.class)
class EquipmentQueryControllerTest {

    public static final String API_EQUIPMENTS = "/api/equipments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetEquipmentByIdUseCase getById;

    @MockitoBean
    private GetEquipmentByUidUseCase getByUid;

    @MockitoBean
    private GetEquipmentBySerialNumberUseCase getBySerial;

    @MockitoBean
    private SearchEquipmentsUseCase searchUseCase;

    @MockitoBean
    private EquipmentQueryMapper mapper;

    @Nested
    class GetRequests {

        @Test
        void getEquipmentById_found() throws Exception {
            var domain = mock(Equipment.class);

            var response = mock(EquipmentResponse.class);

            given(getById.execute(1L)).willReturn(Optional.of(domain));
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS + "/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void getEquipmentById_notFound() throws Exception {
            given(getById.execute(999L)).willReturn(Optional.empty());

            mockMvc.perform(get(API_EQUIPMENTS + "/999").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(getById).execute(999L);
        }

        @Test
        void getEquipmentByUid_found() throws Exception {
            var domain = mock(Equipment.class);

            var response = mock(EquipmentResponse.class);

            given(getByUid.execute(any())).willReturn(Optional.of(domain));
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS + "/by-uid/UID-2").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(getByUid).execute(any());
        }

        @Test
        void getEquipmentByUid_notFound() throws Exception {
            given(getByUid.execute(any())).willReturn(Optional.empty());

            mockMvc.perform(get(API_EQUIPMENTS + "/by-uid/UNKNOWN").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(getByUid).execute(any());
        }

        @Test
        void getEquipmentBySerial_found() throws Exception {
            var domain = mock(Equipment.class);

            var response = mock(EquipmentResponse.class);

            given(getBySerial.execute(any())).willReturn(Optional.of(domain));
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS + "/by-serial/SN-003").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(getBySerial).execute(any());
        }

        @Test
        void getEquipmentBySerial_notFound() throws Exception {
            given(getBySerial.execute(any())).willReturn(Optional.empty());

            mockMvc.perform(get(API_EQUIPMENTS + "/by-serial/UNKNOWN").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(getBySerial).execute(any());
        }

        @Test
        void searchEquipments_found() throws Exception {
            var domain = mock(Equipment.class);

            var response = mock(EquipmentResponse.class);
            given(mapper.toSearchQuery(any(), any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, null, new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            var page = new Page<>(List.of(domain), 1L, new PageRequest(20, 0, null));

            given(searchUseCase.execute(any())).willReturn(page);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }

        @Test
        void searchEquipments_empty() throws Exception {
            given(mapper.toSearchQuery(any(), any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, null, new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            given(searchUseCase.execute(any())).willReturn(com.github.jenkaby.bikerental.shared.domain.model.vo.Page.empty(new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            mockMvc.perform(get(API_EQUIPMENTS).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }

        @Test
        void searchEquipments_withQ_returnsOk() throws Exception {
            var domain = mock(Equipment.class);
            var response = mock(EquipmentResponse.class);
            given(mapper.toSearchQuery(any(), any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, "bike", new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            var page = new Page<>(List.of(domain), 1L, new PageRequest(20, 0, null));

            given(searchUseCase.execute(any())).willReturn(page);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS)
                            .param("q", "bike")
                            .accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }

        @Test
        void getEquipmentById_invalidId() throws Exception {
            // non-numeric id should result in 400 - type mismatch
            mockMvc.perform(get(API_EQUIPMENTS + "/abc").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
