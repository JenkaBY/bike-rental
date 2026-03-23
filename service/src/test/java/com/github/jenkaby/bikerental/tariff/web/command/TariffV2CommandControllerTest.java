package com.github.jenkaby.bikerental.tariff.web.command;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.application.usecase.*;
import com.github.jenkaby.bikerental.tariff.domain.model.DegressiveHourlyTariffV2;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2Status;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffV2Request;
import com.github.jenkaby.bikerental.tariff.web.command.mapper.TariffV2CommandMapper;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffV2QueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = TariffV2CommandController.class)
@DisplayName("Tariff V2 Command Controller Tests")
class TariffV2CommandControllerTest {

    private static final String API_V2_TARIFFS = "/api/v2/tariffs";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTariffV2UseCase createUseCase;

    @MockitoBean
    private UpdateTariffV2UseCase updateUseCase;

    @MockitoBean
    private GetTariffV2ByIdUseCase getByIdUseCase;

    @MockitoBean
    private ActivateTariffV2UseCase activateUseCase;

    @MockitoBean
    private DeactivateTariffV2UseCase deactivateUseCase;

    @MockitoBean
    private TariffV2CommandMapper commandMapper;

    @MockitoBean
    private TariffV2QueryMapper queryMapper;

    private static final PricingParams DEGRESSIVE_PARAMS = new PricingParams(
            new BigDecimal("9.00"), new BigDecimal("2.00"), new BigDecimal("1.00"),
            null, null, null, null, 30, new BigDecimal("1.00"), null);

    private static Map<String, Object> degressiveParamsMap() {
        return Map.of(
                TariffV2FieldNames.FIRST_HOUR_PRICE, new BigDecimal("9.00"),
                TariffV2FieldNames.HOURLY_DISCOUNT, new BigDecimal("2.00"),
                TariffV2FieldNames.MINIMUM_HOURLY_PRICE, new BigDecimal("1.00"),
                TariffV2FieldNames.MINIMUM_DURATION_MINUTES, 30,
                TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, new BigDecimal("1.00")
        );
    }

    @Nested
    @DisplayName("POST /api/v2/tariffs")
    class PostTests {
        @Test
        @DisplayName("returns 201 when valid DEGRESSIVE_HOURLY request")
        void post_create_returns201() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Hourly Bicycle",
                    "Desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"),
                    null
            );
            TariffV2 domain = new DegressiveHourlyTariffV2(
                    1L, "Hourly Bicycle", "Desc", "bicycle", "v2",
                    LocalDate.parse("2026-01-01"), null, TariffV2Status.INACTIVE,
                    Money.of("9.00"), Money.of("2.00"), Money.of("1.00"), 30, Money.of("1.00")
            );
            TariffV2Response response = new TariffV2Response(
                    1L, "Hourly Bicycle", "Desc", "bicycle", PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"), null, "v2", TariffV2Status.INACTIVE
            );

            given(commandMapper.toCreateCommand(any())).willReturn(
                    new CreateTariffV2UseCase.CreateTariffV2Command(
                            "Hourly Bicycle", "Desc", "bicycle", PricingType.DEGRESSIVE_HOURLY,
                            degressiveParamsMap(),
                            LocalDate.parse("2026-01-01"), null
                    ));
            given(createUseCase.execute(any())).willReturn(domain);
            given(queryMapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Hourly Bicycle"))
                    .andExpect(jsonPath("$.pricingType").value("DEGRESSIVE_HOURLY"));
        }

        @Test
        @DisplayName("returns 400 when required fields are missing")
        void post_create_returns400_whenMissingRequiredFields() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    null, // name is missing
                    "Desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"})
        @DisplayName("returns 400 when name is blank or too long")
        void post_create_returns400_whenInvalidName(String invalidName) throws Exception {
            TariffV2Request request = new TariffV2Request(
                    invalidName,
                    "Desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("name"));
        }

        @Test
        @DisplayName("returns 400 when pricingType is invalid")
        void post_create_returns400_whenInvalidPricingType() throws Exception {
            String invalidJson = "{" +
                    "\"name\":\"Test\"," +
                    "\"description\":\"Desc\"," +
                    "\"category\":\"bicycle\"," +
                    "\"pricingType\":\"INVALID_TYPE\"," +
                    "\"pricingParams\":{}" +
                    "}";
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.not_readable"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }

        @Test
        @DisplayName("returns 400 when date format is invalid")
        void post_create_returns400_whenInvalidDateFormat() throws Exception {
            String invalidJson = "{" +
                    "\"name\":\"Test\"," +
                    "\"description\":\"Desc\"," +
                    "\"category\":\"bicycle\"," +
                    "\"pricingType\":\"DEGRESSIVE_HOURLY\"," +
                    "\"pricingParams\":{}," +
                    "\"validFrom\":\"2026/01/01\"" +
                    "}";
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.not_readable"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/v2/tariffs/{id}")
    class PutTests {
        @Test
        @DisplayName("returns 200 when valid")
        void put_update_returns200() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Updated",
                    "Desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"),
                    null
            );
            TariffV2 existing = new DegressiveHourlyTariffV2(
                    1L, "Old", null, "bicycle", "v2",
                    LocalDate.parse("2026-01-01"), null, TariffV2Status.INACTIVE,
                    Money.of("0"), Money.of("0"), Money.of("0"), 0, Money.of("0")
            );
            TariffV2 updated = new DegressiveHourlyTariffV2(
                    1L, "Updated", "Desc", "bicycle", "v2",
                    LocalDate.parse("2026-01-01"), null, TariffV2Status.INACTIVE,
                    Money.of("9.00"), Money.of("2.00"), Money.of("1.00"), 30, Money.of("1.00")
            );
            given(getByIdUseCase.get(1L)).willReturn(existing);
            given(commandMapper.toUpdateCommand(eq(1L), any())).willReturn(
                    new UpdateTariffV2UseCase.UpdateTariffV2Command(
                            1L, "Updated", "Desc", "bicycle", PricingType.DEGRESSIVE_HOURLY,
                            degressiveParamsMap(),
                            LocalDate.parse("2026-01-01"), null
                    ));
            given(updateUseCase.execute(any())).willReturn(updated);
            given(queryMapper.toResponse(updated)).willReturn(new TariffV2Response(
                    1L, "Updated", "Desc", "bicycle", PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"), null, "v2", TariffV2Status.INACTIVE
            ));

            mockMvc.perform(put(API_V2_TARIFFS + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"})
        @DisplayName("returns 400 when name is blank or too long")
        void put_update_returns400_whenInvalidName(String invalidName) throws Exception {
            TariffV2Request request = new TariffV2Request(
                    invalidName,
                    "Desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    DEGRESSIVE_PARAMS,
                    LocalDate.parse("2026-01-01"),
                    null
            );
            mockMvc.perform(put(API_V2_TARIFFS + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("name"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/tariffs/{id}")
    class PatchTests {
        @Test
        @DisplayName("/activate returns 200")
        void patch_activate_returns200() throws Exception {
            TariffV2 activated = new DegressiveHourlyTariffV2(
                    1L, "T", null, "bicycle", "v2",
                    LocalDate.now(), null, TariffV2Status.ACTIVE,
                    Money.of("0"), Money.of("0"), Money.of("0"), 30, Money.of("0")
            );
            given(activateUseCase.execute(1L)).willReturn(activated);
            given(queryMapper.toResponse(activated)).willReturn(new TariffV2Response(
                    1L, "T", null, "bicycle", PricingType.DEGRESSIVE_HOURLY,
                    new PricingParams(null, null, null, null, null, null, null, 30, null, null),
                    LocalDate.now(), null, "v2", TariffV2Status.ACTIVE
            ));

            mockMvc.perform(patch(API_V2_TARIFFS + "/1/activate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("/deactivate returns 200")
        void patch_deactivate_returns200() throws Exception {
            TariffV2 deactivated = new DegressiveHourlyTariffV2(
                    1L, "T", null, "bicycle", "v2",
                    LocalDate.now(), null, TariffV2Status.INACTIVE,
                    Money.of("0"), Money.of("0"), Money.of("0"), 30, Money.of("0")
            );
            given(deactivateUseCase.execute(1L)).willReturn(deactivated);
            given(queryMapper.toResponse(deactivated)).willReturn(new TariffV2Response(
                    1L, "T", null, "bicycle", PricingType.DEGRESSIVE_HOURLY,
                    new PricingParams(null, null, null, null, null, null, null, 30, null, null),
                    LocalDate.now(), null, "v2", TariffV2Status.INACTIVE
            ));

            mockMvc.perform(patch(API_V2_TARIFFS + "/1/deactivate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("INACTIVE"));
        }
    }

    @Nested
    @DisplayName("@ValidTariffV2Pricing validation (POST /api/v2/tariffs)")
    class ValidTariffV2PricingPostTests {
        @Test
        @DisplayName("returns 201 when DEGRESSIVE_HOURLY params are valid")
        void post_create_returns201_whenDegressiveHourlyValid() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Valid Degressive",
                    "desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    new PricingParams(
                            new BigDecimal("10.00"), // firstHourPrice
                            new BigDecimal("2.00"),  // hourlyDiscount
                            new BigDecimal("5.00"),  // minimumHourlyPrice
                            null, // hourlyPrice
                            null, // dailyPrice
                            null, // overtimeHourlyPrice
                            null, // issuanceFee
                            60,   // minimumDurationMinutes
                            new BigDecimal("1.00"), // minimumDurationSurcharge
                            null // price
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 when firstHourPrice is missing (DEGRESSIVE_HOURLY)")
        void post_create_returns400_whenFirstHourPriceMissing() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "No firstHourPrice",
                    "desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    new PricingParams(
                            null, // firstHourPrice missing
                            new BigDecimal("2.00"),
                            new BigDecimal("5.00"),
                            null, null, null, null,
                            60,
                            new BigDecimal("1.00"),
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }

        @Test
        @DisplayName("returns 400 when minimumHourlyPrice > firstHourPrice (DEGRESSIVE_HOURLY)")
        void post_create_returns400_whenMinHourlyPriceGtFirstHourPrice() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Min > First",
                    "desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    new PricingParams(
                            new BigDecimal("10.00"),
                            new BigDecimal("2.00"),
                            new BigDecimal("20.00"), // minimumHourlyPrice > firstHourPrice
                            null, null, null, null,
                            60,
                            new BigDecimal("1.00"),
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }

        @Test
        @DisplayName("returns 400 when minimumDurationMinutes is negative (DEGRESSIVE_HOURLY)")
        void post_create_returns400_whenMinDurationNegative() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Negative min duration",
                    "desc",
                    "bicycle",
                    PricingType.DEGRESSIVE_HOURLY,
                    new PricingParams(
                            new BigDecimal("10.00"),
                            new BigDecimal("2.00"),
                            new BigDecimal("5.00"),
                            null, null, null, null,
                            -1, // negative
                            new BigDecimal("1.00"),
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }
    }

    @Nested
    @DisplayName("@ValidTariffV2Pricing validation (POST /api/v2/tariffs) — FLAT_HOURLY")
    class ValidTariffV2PricingPostFlatHourlyTests {
        @Test
        @DisplayName("returns 200 when FLAT_HOURLY params are valid")
        void post_create_returns200_whenFlatHourlyValid() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Valid Flat Hourly",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_HOURLY,
                    new PricingParams(
                            null, // firstHourPrice
                            null, // hourlyDiscount
                            null, // minimumHourlyPrice
                            new BigDecimal("15.00"), // hourlyPrice
                            null, // dailyPrice
                            null, // overtimeHourlyPrice
                            null, // issuanceFee
                            30,   // minimumDurationMinutes
                            new BigDecimal("1.00"), // minimumDurationSurcharge
                            null // price
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 when hourlyPrice is missing (FLAT_HOURLY)")
        void post_create_returns400_whenHourlyPriceMissing() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "No hourlyPrice",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_HOURLY,
                    new PricingParams(
                            null, null, null,
                            null, // hourlyPrice missing
                            null, null, null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }

        @Test
        @DisplayName("returns 400 when hourlyPrice is negative (FLAT_HOURLY)")
        void post_create_returns400_whenHourlyPriceNegative() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Negative hourlyPrice",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_HOURLY,
                    new PricingParams(
                            null, null, null,
                            new BigDecimal("-1.00"), // negative
                            null, null, null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }

        @Test
        @DisplayName("returns 400 when firstHourPrice is present (FLAT_HOURLY, not applicable field)")
        void post_create_returns400_whenFirstHourPricePresent() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Has firstHourPrice",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_HOURLY,
                    new PricingParams(
                            new BigDecimal("10.00"), // not applicable
                            null, null,
                            new BigDecimal("15.00"),
                            null, null, null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }
    }

    @Nested
    @DisplayName("@ValidTariffV2Pricing validation (POST /api/v2/tariffs) — DAILY")
    class ValidTariffV2PricingPostDailyTests {
        @Test
        @DisplayName("returns 200 when DAILY params are valid")
        void post_create_returns200_whenDailyValid() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Valid Daily",
                    "desc",
                    "bicycle",
                    PricingType.DAILY,
                    new PricingParams(
                            null, // firstHourPrice
                            null, // hourlyDiscount
                            null, // minimumHourlyPrice
                            null, // hourlyPrice
                            new BigDecimal("100.00"), // dailyPrice
                            new BigDecimal("10.00"), // overtimeHourlyPrice
                            null, // issuanceFee
                            null, // minimumDurationMinutes
                            null, // minimumDurationSurcharge
                            null // price
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 when dailyPrice is missing (DAILY)")
        void post_create_returns400_whenDailyPriceMissing() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "No dailyPrice",
                    "desc",
                    "bicycle",
                    PricingType.DAILY,
                    new PricingParams(
                            null, null, null,
                            null,
                            null, // dailyPrice missing
                            null, null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }

        @Test
        @DisplayName("returns 400 when dailyPrice is negative (DAILY)")
        void post_create_returns400_whenDailyPriceNegative() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Negative dailyPrice",
                    "desc",
                    "bicycle",
                    PricingType.DAILY,
                    new PricingParams(
                            null, null, null,
                            null,
                            new BigDecimal("-100.00"), // negative
                            null, null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }

        @Test
        @DisplayName("returns 400 when firstHourPrice is present (DAILY, not applicable field)")
        void post_create_returns400_whenFirstHourPricePresent() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Has firstHourPrice",
                    "desc",
                    "bicycle",
                    PricingType.DAILY,
                    new PricingParams(
                            new BigDecimal("10.00"), // not applicable
                            null, null,
                            null,
                            new BigDecimal("100.00"),
                            null, null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }
    }

    @Nested
    @DisplayName("@ValidTariffV2Pricing validation (POST /api/v2/tariffs) — FLAT_FEE")
    class ValidTariffV2PricingPostFlatFeeTests {
        @Test
        @DisplayName("returns 200 when FLAT_FEE params are valid")
        void post_create_returns200_whenFlatFeeValid() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Valid Flat Fee",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_FEE,
                    new PricingParams(
                            null, // firstHourPrice
                            null, // hourlyDiscount
                            null, // minimumHourlyPrice
                            null, // hourlyPrice
                            null, // dailyPrice
                            null, // overtimeHourlyPrice
                            new BigDecimal("250.00"), // issuanceFee
                            null, // minimumDurationMinutes
                            null, // minimumDurationSurcharge
                            null // price
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 when issuanceFee is missing (FLAT_FEE)")
        void post_create_returns400_whenIssuanceFeeMissing() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "No issuanceFee",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_FEE,
                    new PricingParams(
                            null, null, null,
                            null, null, null,
                            null, // issuanceFee missing
                            null, null, null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }

        @Test
        @DisplayName("returns 400 when issuanceFee is negative (FLAT_FEE)")
        void post_create_returns400_whenIssuanceFeeNegative() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Negative issuanceFee",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_FEE,
                    new PricingParams(
                            null, null, null,
                            null, null, null,
                            new BigDecimal("-250.00"), // negative
                            null, null, null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }

        @Test
        @DisplayName("returns 400 when firstHourPrice is present (FLAT_FEE, not applicable field)")
        void post_create_returns400_whenFirstHourPricePresent() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Has firstHourPrice",
                    "desc",
                    "bicycle",
                    PricingType.FLAT_FEE,
                    new PricingParams(
                            new BigDecimal("10.00"), // not applicable
                            null, null,
                            null, null,
                            new BigDecimal("250.00"),
                            null,
                            null,
                            null,
                            null
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.request.constraint_violation"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("createTariff.arg0"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.valid_tariff_v2pricing"));
        }
    }

    @Nested
    @DisplayName("@ValidTariffV2Pricing validation (POST /api/v2/tariffs) — SPECIAL")
    class ValidTariffV2PricingPostSpecialTests {
        @Test
        @DisplayName("returns 200 when SPECIAL params are valid")
        void post_create_returns200_whenSpecialValid() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "Valid Special",
                    "desc",
                    "bicycle",
                    PricingType.SPECIAL,
                    new PricingParams(
                            null, null, null,
                            null, null, null,
                            null, // issuanceFee
                            null, null,
                            new BigDecimal("777.00") // price
                    ),
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 when price is missing (SPECIAL)")
        void post_create_returns400_whenPriceMissing() throws Exception {
            TariffV2Request request = new TariffV2Request(
                    "No price",
                    "desc",
                    "bicycle",
                    PricingType.SPECIAL,
                    null,
                    LocalDate.now(),
                    null
            );
            mockMvc.perform(post(API_V2_TARIFFS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                    .andExpect(jsonPath("$.correlationId").exists())
                    .andExpect(jsonPath("$.errors[0].field").value("params"))
                    .andExpect(jsonPath("$.errors[0].code").value("validation.not_null"));
        }
    }
}
