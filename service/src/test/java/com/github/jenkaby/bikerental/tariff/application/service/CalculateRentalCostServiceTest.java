package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.application.strategy.ForgivenessStrategy;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateRentalCostUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculateRentalCostService Tests")
class CalculateRentalCostServiceTest {

    @Mock
    private GetTariffByIdUseCase getTariffByIdUseCase;

    @Mock
    private TariffPeriodSelector tariffPeriodSelector;

    @Mock
    private ForgivenessStrategy forgivenessStrategy;

    @InjectMocks
    private CalculateRentalCostService calculator;

    private Tariff createTestTariff() {
        return Tariff.builder()
                .id(1L)
                .name("Test Tariff")
                .description("Test")
                .equipmentTypeSlug("bike")
                .basePrice(Money.of("100.00"))
                .halfHourPrice(Money.of("60.00"))
                .hourPrice(Money.of("100.00"))
                .dayPrice(Money.of("500.00"))
                .hourDiscountedPrice(Money.of("90.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(null)
                .status(TariffStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should calculate cost for 1 hour rental (1 period)")
    void shouldCalculateCostFor1HourRental() {
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(60);
        int billableMinutes = 60;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.getForgivenessMessage(0)).willReturn("On time");

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.actualMinutes()).isEqualTo(60);
        assertThat(result.plannedMinutes()).isEqualTo(60);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
        assertThat(result.forgivenessApplied()).isTrue();
    }

    @Test
    @DisplayName("Should calculate cost for 2 hour rental (2 periods)")
    void shouldCalculateCostFor2HourRental() {
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(2);
        Duration actualDuration = Duration.ofMinutes(120);
        int billableMinutes = 120;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.getForgivenessMessage(0)).willReturn("On time");

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("200.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("200.00"));
        assertThat(result.overtimeMinutes()).isEqualTo(0);
        assertThat(result.forgivenessApplied()).isTrue();
    }

    @Test
    @DisplayName("Should round up to next period for partial hour")
    void shouldRoundUpToNextPeriodForPartialHour() {
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofMinutes(60);
        Duration actualDuration = Duration.ofMinutes(90);
        int billableMinutes = 90;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("200.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("200.00"));
        assertThat(result.overtimeMinutes()).isEqualTo(30);
        assertThat(result.forgivenessApplied()).isTrue();
    }

    @Test
    @DisplayName("Should return on time - actual less than planned (1 period)")
    void shouldReturnOnTimeActualLessThanPlanned() {
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(55);
        int billableMinutes = 55;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.getForgivenessMessage(-5)).willReturn("Early return");

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeMinutes()).isEqualTo(-5);
        assertThat(result.forgivenessApplied()).isTrue();
    }

    @Test
    @DisplayName("Should use half-hour price for 30-minute rental")
    void shouldUseHalfHourPriceFor30MinuteRental() {
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofMinutes(30);
        Duration actualDuration = Duration.ofMinutes(30);
        int billableMinutes = 30;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HALF_HOUR);
        given(forgivenessStrategy.getForgivenessMessage(0)).willReturn("On time");

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("60.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("60.00"));
    }

    @Test
    @DisplayName("Should use day price for 4+ hour rental (1 day period)")
    void shouldUseDayPriceFor4PlusHourRental() {
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(5);
        Duration actualDuration = Duration.ofMinutes(300);
        int billableMinutes = 300;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.DAY);
        given(forgivenessStrategy.getForgivenessMessage(0)).willReturn("On time");

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("500.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("500.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "60,  60, 0,   100.00",
            "90,  60, 30,  200.00",
            "120, 60, 60,  200.00",
            "180, 60, 120, 300.00",
            "30,  30, 0,    60.00"
    })
    @DisplayName("Should calculate baseCost as periodPrice × ceil(actualMinutes / periodMinutes)")
    void shouldCalculateBaseCostByPeriodMultiplier(int actualMinutes, int periodMinutes,
                                                   int expectedOvertime, String expectedBaseCost) {
        Long tariffId = 1L;
        Duration actualDuration = Duration.ofMinutes(actualMinutes);
        Duration plannedDuration = Duration.ofMinutes(actualMinutes - expectedOvertime);
        TariffPeriod period = periodMinutes == 30 ? TariffPeriod.HALF_HOUR : TariffPeriod.HOUR;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(period);

        RentalCost result = calculator.execute(
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, actualMinutes, plannedDuration));

        assertThat(result.baseCost()).isEqualByComparingTo(Money.of(expectedBaseCost));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.overtimeMinutes()).isEqualTo(expectedOvertime);
        assertThat(result.forgivenessApplied()).isTrue();
    }
}
