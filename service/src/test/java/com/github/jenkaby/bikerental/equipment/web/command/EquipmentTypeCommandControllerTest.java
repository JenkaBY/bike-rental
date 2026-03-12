package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeRequest;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeUpdateRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentTypeCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentTypeMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = EquipmentTypeCommandController.class)
class EquipmentTypeCommandControllerTest {

    public static final String API_EQUIPMENT_TYPES = "/api/equipment-types";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEquipmentTypeUseCase createUseCase;

    @MockitoBean
    private UpdateEquipmentTypeUseCase updateUseCase;

    @MockitoBean
    private EquipmentTypeCommandMapper commandMapper;

    @MockitoBean
    private EquipmentTypeMapper queryMapper;

    @Nested
    class Post {

        @Nested
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenSlugIsBlank(String slug) throws Exception {
                EquipmentTypeRequest request = new EquipmentTypeRequest(
                        slug,
                        "Bike",
                        "Two wheeled vehicle"
                );

                mockMvc.perform(post(API_EQUIPMENT_TYPES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").exists())
                        .andExpect(jsonPath("$.detail").exists());
            }

            @Test
            void whenSlugExceedsMaxLength() throws Exception {
                String longSlug = "a".repeat(51);
                EquipmentTypeRequest request = new EquipmentTypeRequest(
                        longSlug,
                        "Bike",
                        "Two wheeled vehicle"
                );

                mockMvc.perform(post(API_EQUIPMENT_TYPES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").exists())
                        .andExpect(jsonPath("$.detail").exists());
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                mockMvc.perform(post(API_EQUIPMENT_TYPES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                mockMvc.perform(post(API_EQUIPMENT_TYPES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    class Put {

        @Nested
        class ShouldReturn400 {

            @Test
            void whenSlugExceedsMaxLength() throws Exception {
                String longSlug = "a".repeat(51);
                var request = new EquipmentTypeUpdateRequest(
                        "Bike",
                        "Two wheeled vehicle"
                );

                mockMvc.perform(put(API_EQUIPMENT_TYPES + "/{slug}", longSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").exists())
                        .andExpect(jsonPath("$.detail").exists());
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            void whenPathSlugIsBlank(String pathSlug) throws Exception {
                var request = createValidUpdateRequest();

                mockMvc.perform(put(API_EQUIPMENT_TYPES + "/{slug}", pathSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                String pathSlug = "bike";

                mockMvc.perform(put(API_EQUIPMENT_TYPES + "/{slug}", pathSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                String pathSlug = "bike";

                mockMvc.perform(put(API_EQUIPMENT_TYPES + "/{slug}", pathSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());
            }

        }

        private static @NonNull EquipmentTypeUpdateRequest createValidUpdateRequest() {
            return new EquipmentTypeUpdateRequest(
                    "scooter",
                    "Scooter"
            );
        }
    }
}

