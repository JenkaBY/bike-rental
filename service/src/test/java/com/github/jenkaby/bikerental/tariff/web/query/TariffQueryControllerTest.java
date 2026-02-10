package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffForRentalUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffSelectionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

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
    private SelectTariffForRentalUseCase selectTariffForRentalUseCase;

    @MockitoBean
    private TariffPeriodSelector tariffPeriodSelector;

    @MockitoBean
    private TariffToInfoMapper tariffToInfoMapper;

    @MockitoBean
    private TariffQueryMapper mapper;

    @MockitoBean
    private TariffSelectionMapper selectionMapper;

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

    @Nested
    @DisplayName("GET /api/tariffs/selection")
    class SelectTariff {

        @Nested
        @DisplayName("Should return 200 OK")
        class ShouldReturn200 {

            @Test
            @DisplayName("when valid parameters provided")
            void selectTariff_shouldReturnOk_whenValidParameters() throws Exception {
                String equipmentType = "bike";
                int durationMinutes = 60;
                LocalDate rentalDate = LocalDate.of(2026, 2, 10);
                Duration duration = Duration.ofMinutes(durationMinutes);

                Tariff tariff = Tariff.builder()
                        .id(1L)
                        .name("Standard Hourly")
                        .description("Standard hourly rate")
                        .equipmentTypeSlug("bike")
                        .basePrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("100.00"))
                        .halfHourPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("60.00"))
                        .hourPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("100.00"))
                        .dayPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("500.00"))
                        .hourDiscountedPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("90.00"))
                        .validFrom(LocalDate.of(2026, 1, 1))
                        .validTo(null)
                        .status(TariffStatus.ACTIVE)
                        .build();

                TariffInfo tariffInfo = new TariffInfo(
                        1L,
                        "Standard Hourly",
                        "bike",
                        BigDecimal.valueOf(100.00),
                        BigDecimal.valueOf(60.00),
                        BigDecimal.valueOf(100.00),
                        BigDecimal.valueOf(500.00),
                        LocalDate.of(2026, 1, 1),
                        null,
                        true
                );

                TariffSelectionResponse response = new TariffSelectionResponse(
                        1L,
                        "Standard Hourly",
                        "bike",
                        BigDecimal.valueOf(100.00),
                        TariffPeriod.HOUR
                );

                org.mockito.BDDMockito.given(selectTariffForRentalUseCase.execute(
                                org.mockito.ArgumentMatchers.argThat(cmd ->
                                        cmd.equipmentType().equals(equipmentType) &&
                                                cmd.durationMinutes() == durationMinutes &&
                                                cmd.rentalDate().equals(rentalDate)
                                )))
                        .willReturn(tariff);
                org.mockito.BDDMockito.given(tariffPeriodSelector.selectPeriod(duration))
                        .willReturn(TariffPeriod.HOUR);
                org.mockito.BDDMockito.given(tariffToInfoMapper.toTariffInfo(tariff))
                        .willReturn(tariffInfo);
                org.mockito.BDDMockito.given(selectionMapper.toSelectionResponse(tariffInfo, TariffPeriod.HOUR))
                        .willReturn(response);

                mockMvc.perform(get(API_TARIFFS + "/selection")
                                .param("equipmentType", equipmentType)
                                .param("durationMinutes", String.valueOf(durationMinutes))
                                .param("rentalDate", rentalDate.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1L))
                        .andExpect(jsonPath("$.name").value("Standard Hourly"))
                        .andExpect(jsonPath("$.equipmentType").value("bike"))
                        .andExpect(jsonPath("$.price").value(100.00))
                        .andExpect(jsonPath("$.period").value("HOUR"));
            }

            @Test
            @DisplayName("when rentalDate is not provided (uses today)")
            void selectTariff_shouldReturnOk_whenRentalDateNotProvided() throws Exception {
                String equipmentType = "bike";
                int durationMinutes = 60;
                Duration duration = Duration.ofMinutes(durationMinutes);

                Tariff tariff = Tariff.builder()
                        .id(1L)
                        .name("Standard Hourly")
                        .description("Standard hourly rate")
                        .equipmentTypeSlug("bike")
                        .basePrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("100.00"))
                        .halfHourPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("60.00"))
                        .hourPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("100.00"))
                        .dayPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("500.00"))
                        .hourDiscountedPrice(com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("90.00"))
                        .validFrom(LocalDate.of(2026, 1, 1))
                        .validTo(null)
                        .status(TariffStatus.ACTIVE)
                        .build();

                TariffInfo tariffInfo = new TariffInfo(
                        1L,
                        "Standard Hourly",
                        "bike",
                        BigDecimal.valueOf(100.00),
                        BigDecimal.valueOf(60.00),
                        BigDecimal.valueOf(100.00),
                        BigDecimal.valueOf(500.00),
                        LocalDate.of(2026, 1, 1),
                        null,
                        true
                );

                TariffSelectionResponse response = new TariffSelectionResponse(
                        1L,
                        "Standard Hourly",
                        "bike",
                        BigDecimal.valueOf(100.00),
                        TariffPeriod.HOUR
                );

                org.mockito.BDDMockito.given(selectTariffForRentalUseCase.execute(
                                org.mockito.ArgumentMatchers.argThat(cmd ->
                                        cmd.equipmentType().equals(equipmentType) &&
                                                cmd.durationMinutes() == durationMinutes &&
                                                cmd.rentalDate() == null
                                )))
                        .willReturn(tariff);
                org.mockito.BDDMockito.given(tariffPeriodSelector.selectPeriod(duration))
                        .willReturn(TariffPeriod.HOUR);
                org.mockito.BDDMockito.given(tariffToInfoMapper.toTariffInfo(tariff))
                        .willReturn(tariffInfo);
                org.mockito.BDDMockito.given(selectionMapper.toSelectionResponse(tariffInfo, TariffPeriod.HOUR))
                        .willReturn(response);

                mockMvc.perform(get(API_TARIFFS + "/selection")
                                .param("equipmentType", equipmentType)
                                .param("durationMinutes", String.valueOf(durationMinutes)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1L))
                        .andExpect(jsonPath("$.name").value("Standard Hourly"));
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @Test
            @DisplayName("when equipmentType is missing")
            void selectTariff_shouldReturnBadRequest_whenEquipmentTypeMissing() throws Exception {
                mockMvc.perform(get(API_TARIFFS + "/selection")
                                .param("durationMinutes", "60"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.detail").value(
                                containsString("Required request parameter 'equipmentType'")
                        ));
            }

            @Test
            @DisplayName("when durationMinutes is missing")
            void selectTariff_shouldReturnBadRequest_whenDurationMinutesMissing() throws Exception {
                mockMvc.perform(get(API_TARIFFS + "/selection")
                                .param("equipmentType", "bike"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.detail").value(
                                containsString("Required request parameter 'durationMinutes'")
                        ));
            }

            @ParameterizedTest
            @ValueSource(ints = {0, -1, -60})
            @DisplayName("when durationMinutes is not positive")
            void selectTariff_shouldReturnBadRequest_whenDurationMinutesInvalid(int durationMinutes) throws Exception {
                mockMvc.perform(get(API_TARIFFS + "/selection")
                                .param("equipmentType", "bike")
                                .param("durationMinutes", String.valueOf(durationMinutes)))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("Should return 404 Not Found")
        class ShouldReturn404 {

            @Test
            @DisplayName("when no suitable tariff found")
            void selectTariff_shouldReturnNotFound_whenNoSuitableTariff() throws Exception {
                String equipmentType = "bike";
                int durationMinutes = 60;
                LocalDate rentalDate = LocalDate.of(2026, 2, 10);

                org.mockito.BDDMockito.given(selectTariffForRentalUseCase.execute(
                                org.mockito.ArgumentMatchers.argThat(cmd ->
                                        cmd.equipmentType().equals(equipmentType) &&
                                                cmd.durationMinutes() == durationMinutes &&
                                                cmd.rentalDate().equals(rentalDate)
                                )))
                        .willThrow(new SuitableTariffNotFoundException(equipmentType, rentalDate, null));

                mockMvc.perform(get(API_TARIFFS + "/selection")
                                .param("equipmentType", equipmentType)
                                .param("durationMinutes", String.valueOf(durationMinutes))
                                .param("rentalDate", rentalDate.toString()))
                        .andExpect(status().isNotFound());
            }
        }
    }
}
