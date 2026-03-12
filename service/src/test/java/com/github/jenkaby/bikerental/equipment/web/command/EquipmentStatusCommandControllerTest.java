package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusRequest;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusUpdateRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentStatusCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentStatusMapper;
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

import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = EquipmentStatusCommandController.class)
class EquipmentStatusCommandControllerTest {

    public static final String API_EQUIPMENT_STATUSES = "/api/equipment-statuses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEquipmentStatusUseCase createUseCase;

    @MockitoBean
    private UpdateEquipmentStatusUseCase updateUseCase;

    @MockitoBean
    private EquipmentStatusCommandMapper commandMapper;

    @MockitoBean
    private EquipmentStatusMapper queryMapper;

    @Nested
    class Post {

        @Nested
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenSlugIsBlank(String slug) throws Exception {
                EquipmentStatusRequest request = new EquipmentStatusRequest(
                        slug,
                        "Available",
                        "Equipment is available for rental"
                );

                mockMvc.perform(post(API_EQUIPMENT_STATUSES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Validation error")));
            }

            @Test
            void whenSlugExceedsMaxLength() throws Exception {
                String longSlug = "a".repeat(51);
                EquipmentStatusRequest request = new EquipmentStatusRequest(
                        longSlug,
                        "Available",
                        "Equipment is available for rental"
                );

                mockMvc.perform(post(API_EQUIPMENT_STATUSES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Validation error")));
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenAllowedTransitionsContainsNullOrBlankSlug(String allowedTransition) throws Exception {
                String longSlug = "a".repeat(5);
                HashSet<String> allowedTransitions = new HashSet<>();
                allowedTransitions.add(allowedTransition);
                EquipmentStatusRequest request = new EquipmentStatusRequest(
                        longSlug,
                        "Available",
                        "Equipment is available for rental",
                        allowedTransitions
                );

                mockMvc.perform(post(API_EQUIPMENT_STATUSES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Validation error")));
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                mockMvc.perform(post(API_EQUIPMENT_STATUSES)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                mockMvc.perform(post(API_EQUIPMENT_STATUSES)
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

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenAllowedTransitionsContainsNullOrBlankSlug(String allowedTransition) throws Exception {
                String slug = "a".repeat(5);
                HashSet<String> allowedTransitions = new HashSet<>();
                allowedTransitions.add(allowedTransition);
                EquipmentStatusRequest request = new EquipmentStatusRequest(
                        slug,
                        "Available",
                        "Equipment is available for rental",
                        allowedTransitions
                );

                mockMvc.perform(put(API_EQUIPMENT_STATUSES + "/{slug}", slug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Validation error")));
            }

            @Test
            void whenSlugExceedsMaxLength() throws Exception {
                String longSlug = "a".repeat(51);
                var request = new EquipmentStatusUpdateRequest(
                        "Available",
                        "Equipment is available for rental"
                );

                mockMvc.perform(put(API_EQUIPMENT_STATUSES + "/{slug}", longSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Bad Request"));
            }

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            void whenPathSlugIsBlank(String pathSlug) throws Exception {
                EquipmentStatusRequest request = createValidUpdateRequest();

                mockMvc.perform(put(API_EQUIPMENT_STATUSES + "/{slug}", pathSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsEmpty() throws Exception {
                String pathSlug = "available";

                mockMvc.perform(put(API_EQUIPMENT_STATUSES + "/{slug}", pathSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenRequestBodyIsMalformed() throws Exception {
                String pathSlug = "available";

                mockMvc.perform(put(API_EQUIPMENT_STATUSES + "/{slug}", pathSlug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());
            }

        }

        private static @NonNull EquipmentStatusRequest createValidUpdateRequest() {
            return new EquipmentStatusRequest(
                    "maintenance",
                    "Maintenance",
                    "Equipment is under maintenance"
            );
        }
    }
}
