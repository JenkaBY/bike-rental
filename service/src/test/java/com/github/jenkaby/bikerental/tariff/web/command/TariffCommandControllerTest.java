package com.github.jenkaby.bikerental.tariff.web.command;

import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.application.usecase.ActivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.DeactivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffRequest;
import com.github.jenkaby.bikerental.tariff.web.command.mapper.TariffCommandMapper;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TariffCommandController.class)
@DisplayName("Tariff Command Controller Tests")
class TariffCommandControllerTest {

    public static final String API_TARIFFS = "/api/tariffs";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTariffUseCase createUseCase;

    @MockitoBean
    private UpdateTariffUseCase updateUseCase;

    @MockitoBean
    private ActivateTariffUseCase activateUseCase;

    @MockitoBean
    private DeactivateTariffUseCase deactivateUseCase;

    @MockitoBean
    private TariffCommandMapper commandMapper;

    @MockitoBean
    private TariffQueryMapper queryMapper;

    @Nested
    @DisplayName("POST /api/tariffs")
    class Post {

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            @DisplayName("when name is blank or null")
            void whenNameIsBlank_orNull(String name) throws Exception {
                TariffRequest request = new TariffRequest(
                        name,
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("name"));
            }

            @Test
            @DisplayName("when name exceeds 200 characters")
            void whenNameExceedsMaxLength() throws Exception {
                String longName = "a".repeat(201);
                TariffRequest request = new TariffRequest(
                        longName,
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("name"));
            }

            @Test
            @DisplayName("when description exceeds 1000 characters")
            void whenDescriptionExceedsMaxLength() throws Exception {
                String longDescription = "a".repeat(1001);
                TariffRequest request = new TariffRequest(
                        "Name",
                        longDescription,
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("description"));
            }

            @ParameterizedTest
            @NullSource
            @ValueSource(strings = {"-1.00", "-0.01", "0"})
            @DisplayName("when base price is null, negative or zero")
            void whenBasePriceIsInvalid(String basePriceStr) throws Exception {
                BigDecimal basePrice = basePriceStr == null ? null : new BigDecimal(basePriceStr);

                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        basePrice,
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("basePrice"));
            }

            @ParameterizedTest
            @NullSource
            @ValueSource(strings = {"-1.00", "-0.01", "0"})
            @DisplayName("when half hour price is null, negative or zero")
            void whenHalfHourPriceIsInvalid(String priceStr) throws Exception {
                BigDecimal price = priceStr == null ? null : new BigDecimal(priceStr);

                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        price,
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("halfHourPrice"));
            }

            @ParameterizedTest
            @NullSource
            @ValueSource(strings = {"-1.00", "-0.01", "0"})
            @DisplayName("when hour price is null, negative or zero")
            void whenHourPriceIsInvalid(String priceStr) throws Exception {
                BigDecimal price = priceStr == null ? null : new BigDecimal(priceStr);

                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        price,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("hourPrice"));
            }

            @ParameterizedTest
            @NullSource
            @ValueSource(strings = {"-1.00", "-0.01", "0"})
            @DisplayName("when day price is null, negative or zero")
            void whenDayPriceIsInvalid(String priceStr) throws Exception {
                BigDecimal price = priceStr == null ? null : new BigDecimal(priceStr);

                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        price,
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("dayPrice"));
            }

            @ParameterizedTest
            @NullSource
            @ValueSource(strings = {"-1.00", "-0.01", "0"})
            @DisplayName("when hour discounted price is null, negative or zero")
            void whenHourDiscountedPriceIsInvalid(String priceStr) throws Exception {
                BigDecimal price = priceStr == null ? null : new BigDecimal(priceStr);

                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        price,
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("hourDiscountedPrice"));
            }

            @Test
            @DisplayName("when price has more than 2 decimal places")
            void whenPriceHasTooManyDecimals() throws Exception {
                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.123"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("basePrice"));
            }

            @Test
            @DisplayName("when validFrom is null")
            void whenValidFromIsNull() throws Exception {
                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        null,
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                        .andExpect(jsonPath("$.errors[0].field").value("validFrom"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_null"));
            }

            @Test
            @DisplayName("when status is null")
            void whenStatusIsNull() throws Exception {
                TariffRequest request = new TariffRequest(
                        "Name",
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        null
                );

                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("status"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_null"));
            }

            @Test
            @DisplayName("when request body is empty")
            void whenRequestBodyIsEmpty() throws Exception {
                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when request body is malformed JSON")
            void whenRequestBodyIsMalformed() throws Exception {
                mockMvc.perform(post(API_TARIFFS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/tariffs/{id}")
    class Put {

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            @DisplayName("when name is blank or null")
            void whenNameIsBlank_orNull(String name) throws Exception {
                long id = 1L;
                TariffRequest request = new TariffRequest(
                        name,
                        "desc",
                        "bicycle",
                        new BigDecimal("1.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("1.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.90"),
                        LocalDate.parse("2026-01-01"),
                        null,
                        com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus.INACTIVE
                );

                mockMvc.perform(put(API_TARIFFS + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("name"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_blank"));
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/tariffs/{id}/activate|deactivate")
    class Patch {

        @Test
        @DisplayName("should return 200 when activating tariff")
        void patchActivateShouldReturn200() throws Exception {
            long id = 1L;

            given(activateUseCase.execute(eq(id))).willReturn(mock(Tariff.class));


            mockMvc.perform(patch(API_TARIFFS + "/{id}/activate", id))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 200 when activating or deactivating tariff")
        void patchDeactivateShouldReturn200() throws Exception {
            long id = 1L;
            given(deactivateUseCase.execute(eq(id))).willReturn(mock(Tariff.class));

            mockMvc.perform(patch(API_TARIFFS + "/{id}/deactivate", id))
                    .andExpect(status().isOk());
        }
    }
}
