package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = EquipmentCommandController.class)
class EquipmentCommandControllerTest {

    public static final String API_EQUIPMENTS = "/api/equipments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEquipmentUseCase createUseCase;

    @MockitoBean
    private UpdateEquipmentUseCase updateUseCase;

    @MockitoBean
    private EquipmentCommandMapper commandMapper;

    @MockitoBean
    private EquipmentQueryMapper queryMapper;

    @Nested
    class Post {

        @Nested
        class ShouldReturn201 {

            @Test
            void whenRequestContainsAllValidFields() throws Exception {
                EquipmentRequest request = createValidRequest();

                configureMapperDefaults();
                given(createUseCase.execute(any(CreateEquipmentUseCase.CreateEquipmentCommand.class)))
                        .willReturn(mock(Equipment.class));

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }

            @Test
            void whenRequestContainsOnlyRequiredFields() throws Exception {
                EquipmentRequest request = new EquipmentRequest(
                        "SN-101",
                        "uid",
                        "BICYCLE",
                        "AVAILABLE",
                        null,
                        null,
                        null
                );

                configureMapperDefaults();
                given(createUseCase.execute(any(CreateEquipmentUseCase.CreateEquipmentCommand.class)))
                        .willReturn(mock(Equipment.class));

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }
        }

        @Nested
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenSerialNumberIsBlank(String serialNumber) throws Exception {
                EquipmentRequest request = new EquipmentRequest(
                        serialNumber,
                        "UID-100",
                        "bicycle",
                        "available",
                        "ModelA",
                        LocalDate.of(2024, 1, 1),
                        "good"
                );

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='serialNumber')].code").value(hasItem("validation.not_blank")));
            }

            @Test
            void whenSerialNumberExceedsMaxLength() throws Exception {
                String longSerialNumber = "A".repeat(51);
                EquipmentRequest request = new EquipmentRequest(
                        longSerialNumber,
                        "UID-100",
                        "bicycle",
                        "available",
                        "ModelA",
                        LocalDate.of(2024, 1, 1),
                        "good"
                );

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='serialNumber')].code").value(hasItem("validation.size")));
            }

            @Test
            void whenUidExceedsMaxLength() throws Exception {
                String longUid = "U".repeat(101);
                EquipmentRequest request = new EquipmentRequest(
                        "SN-100",
                        longUid,
                        "bicycle",
                        "available",
                        "ModelA",
                        LocalDate.of(2024, 1, 1),
                        "good"
                );

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='uid')].code").value(hasItem("validation.size")));
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenTypeSlugIsBlank(String typeSlug) throws Exception {
                EquipmentRequest request = new EquipmentRequest(
                        "SN-100",
                        "UID-100",
                        typeSlug,
                        "available",
                        "ModelA",
                        LocalDate.of(2024, 1, 1),
                        "good"
                );

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='typeSlug')].code").value(hasItem("validation.not_blank")));
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenStatusSlugIsBlank(String statusSlug) throws Exception {
                EquipmentRequest request = new EquipmentRequest(
                        "SN-100",
                        "UID-100",
                        "bicycle",
                        statusSlug,
                        "ModelA",
                        LocalDate.of(2024, 1, 1),
                        "good"
                );

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='statusSlug')].code").value(hasItem("validation.not_blank")));
            }

            @Test
            void whenModelExceedsMaxLength() throws Exception {
                String longModel = "M".repeat(201);
                EquipmentRequest request = new EquipmentRequest(
                        "SN-100",
                        "UID-100",
                        "bicycle",
                        "available",
                        longModel,
                        LocalDate.of(2024, 1, 1),
                        "good"
                );

                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='model')].code").value(hasItem("validation.size")));
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                mockMvc.perform(post(API_EQUIPMENTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());
            }
        }

        private static @NonNull EquipmentRequest createValidRequest() {
            return new EquipmentRequest(
                    "SN-100",
                    "UID-100",
                    "BICYCLE",
                    "AVAILABLE",
                    "ModelA",
                    LocalDate.of(2024, 1, 1),
                    "good"
            );
        }

        private void configureMapperDefaults() {
            given(commandMapper.toCreateCommand(any(EquipmentRequest.class)))
                    .willReturn(mock(CreateEquipmentUseCase.CreateEquipmentCommand.class));
            given(queryMapper.toResponse(any(Equipment.class)))
                    .willReturn(mock(EquipmentResponse.class));
        }
    }

    @Nested
    class Put {

        @Nested
        class ShouldReturn200 {

            @Test
            void whenUpdateEquipmentWithAllValidFields() throws Exception {
                Long equipmentId = 1L;
                EquipmentRequest request = createValidUpdateRequest();

                configureMapperDefaults(equipmentId);
                given(updateUseCase.execute(any(UpdateEquipmentUseCase.UpdateEquipmentCommand.class)))
                        .willReturn(mock(Equipment.class));

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenSerialNumberIsBlank(String serialNumber) throws Exception {
                Long equipmentId = 1L;
                EquipmentRequest request = new EquipmentRequest(
                        serialNumber,
                        "UID-200",
                        "bicycle",
                        "available",
                        "ModelB",
                        LocalDate.of(2024, 2, 1),
                        "good"
                );

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='serialNumber')].code").value(hasItem("validation.not_blank")));
            }

            @Test
            void whenSerialNumberExceedsMaxLength() throws Exception {
                Long equipmentId = 1L;
                String longSerialNumber = "S".repeat(51);
                EquipmentRequest request = new EquipmentRequest(
                        longSerialNumber,
                        "UID-200",
                        "bicycle",
                        "available",
                        "ModelB",
                        LocalDate.of(2024, 2, 1),
                        "good"
                );

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='serialNumber')].code").value(hasItem("validation.size")));
            }

            @Test
            void whenUidExceedsMaxLength() throws Exception {
                Long equipmentId = 1L;
                String longUid = "U".repeat(101);
                EquipmentRequest request = new EquipmentRequest(
                        "SN-200",
                        longUid,
                        "bicycle",
                        "available",
                        "ModelB",
                        LocalDate.of(2024, 2, 1),
                        "good"
                );

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='uid')].code").value(hasItem("validation.size")));
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenTypeSlugIsBlank(String typeSlug) throws Exception {
                Long equipmentId = 1L;
                EquipmentRequest request = new EquipmentRequest(
                        "SN-200",
                        "UID-200",
                        typeSlug,
                        "available",
                        "ModelB",
                        LocalDate.of(2024, 2, 1),
                        "good"
                );

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='typeSlug')].code").value(hasItem("validation.not_blank")));
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenStatusSlugIsBlank(String statusSlug) throws Exception {
                Long equipmentId = 1L;
                EquipmentRequest request = new EquipmentRequest(
                        "SN-200",
                        "UID-200",
                        "bicycle",
                        statusSlug,
                        "ModelB",
                        LocalDate.of(2024, 2, 1),
                        "good"
                );

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='statusSlug')].code").value(hasItem("validation.not_blank")));
            }

            @Test
            void whenModelExceedsMaxLength() throws Exception {
                Long equipmentId = 1L;
                String longModel = "M".repeat(201);
                EquipmentRequest request = new EquipmentRequest(
                        "SN-200",
                        "UID-200",
                        "bicycle",
                        "available",
                        longModel,
                        LocalDate.of(2024, 2, 1),
                        "good"
                );

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[?(@.field=='model')].code").value(hasItem("validation.size")));
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                Long equipmentId = 1L;

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                Long equipmentId = 1L;

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", equipmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenEquipmentIdIsInvalid() throws Exception {
                EquipmentRequest request = createValidUpdateRequest();

                mockMvc.perform(put(API_EQUIPMENTS + "/{id}", "invalid-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }
        }

        private static @NonNull EquipmentRequest createValidUpdateRequest() {
            return new EquipmentRequest(
                    "SN-200",
                    "UID-200",
                    "SCOOTER",
                    "MAINTENANCE",
                    "ModelB",
                    LocalDate.of(2024, 2, 1),
                    "fair"
            );
        }


        private void configureMapperDefaults(Long equipmentId) {
            given(commandMapper.toUpdateCommand(eq(equipmentId), any(EquipmentRequest.class)))
                    .willReturn(mock(UpdateEquipmentUseCase.UpdateEquipmentCommand.class));
            given(queryMapper.toResponse(any(Equipment.class)))
                    .willReturn(mock(EquipmentResponse.class));
        }
    }
}
