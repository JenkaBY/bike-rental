package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.application.strategy.ForgivenessStrategy;
import com.github.jenkaby.bikerental.tariff.application.strategy.OvertimeCalculationStrategy;
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

    @Mock
    private OvertimeCalculationStrategy overtimeCalculationStrategy;

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
    @DisplayName("Should calculate cost for on-time return (no overtime)")
    void shouldCalculateCostForOnTimeReturn() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(55);
        int billableMinutes = 55;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(-5)).willReturn(true);
        given(forgivenessStrategy.getForgivenessMessage(-5)).willReturn("Возврат вовремя или досрочно");

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.actualMinutes()).isEqualTo(55);
        assertThat(result.billableMinutes()).isEqualTo(55);
        assertThat(result.plannedMinutes()).isEqualTo(60);
        assertThat(result.overtimeMinutes()).isEqualTo(-5);
        assertThat(result.forgivenessApplied()).isTrue();
        assertThat(result.calculationMessage()).contains("Возврат вовремя");
    }

    @Test
    @DisplayName("Should apply forgiveness for 5 minutes overtime")
    void shouldApplyForgivenessFor5MinutesOvertime() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(65);
        int billableMinutes = 65;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(5)).willReturn(true);
        given(forgivenessStrategy.getForgivenessMessage(5)).willReturn("Просрочка прощена (5 минут просрочки)");

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
        assertThat(result.totalCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeMinutes()).isEqualTo(5);
        assertThat(result.forgivenessApplied()).isTrue();
        assertThat(result.calculationMessage()).contains("Просрочка прощена");
    }

    @Test
    @DisplayName("Should apply forgiveness for exactly 7 minutes overtime")
    void shouldApplyForgivenessForExactly7MinutesOvertime() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(67);
        int billableMinutes = 67;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(7)).willReturn(true);
        given(forgivenessStrategy.getForgivenessMessage(7)).willReturn("Просрочка прощена (7 минут просрочки)");

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.overtimeMinutes()).isEqualTo(7);
        assertThat(result.forgivenessApplied()).isTrue();
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
    }

    @Test
    @DisplayName("Should charge overtime for 8 minutes overtime (beyond forgiveness)")
    void shouldChargeOvertimeFor8MinutesOvertime() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(68);
        int billableMinutes = 68;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(8)).willReturn(false);
        given(forgivenessStrategy.getForgivenMinutes(8)).willReturn(7);
        given(overtimeCalculationStrategy.calculateOvertimeCost(
                Money.of("100.00"), TariffPeriod.HOUR, 8, 7))
                .willReturn(Money.of("8.33"));

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("100.00"));
        assertThat(result.overtimeMinutes()).isEqualTo(8);
        assertThat(result.forgivenessApplied()).isFalse();
        assertThat(result.overtimeCost().isPositive()).isTrue();
        // Chargeable overtime = 8 - 7 = 1 minute, rounded to 5 minutes = 5 minutes
        // Price per 5 min = 100 / 60 * 5 = 8.33
        // Overtime cost = 8.33
        assertThat(result.calculationMessage()).contains("Overtime charged");
    }

    @Test
    @DisplayName("Should calculate overtime cost for 10 minutes overtime")
    void shouldCalculateOvertimeCostFor10MinutesOvertime() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(70);
        int billableMinutes = 70;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(10)).willReturn(false);
        given(forgivenessStrategy.getForgivenMinutes(10)).willReturn(7);
        given(overtimeCalculationStrategy.calculateOvertimeCost(
                Money.of("100.00"), TariffPeriod.HOUR, 10, 7))
                .willReturn(Money.of("8.33"));

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.overtimeMinutes()).isEqualTo(10);
        assertThat(result.forgivenessApplied()).isFalse();
        // Chargeable overtime = 10 - 7 = 3 minutes, rounded to 5 minutes = 5 minutes
        // Price per 5 min = 100 / 60 * 5 = 8.33
        // Overtime cost = 8.33
        assertThat(result.overtimeCost().amount()).isEqualByComparingTo(Money.of("8.33").amount());
    }

    @Test
    @DisplayName("Should calculate overtime cost for 15 minutes overtime")
    void shouldCalculateOvertimeCostFor15MinutesOvertime() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(75);
        int billableMinutes = 75;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(15)).willReturn(false);
        given(forgivenessStrategy.getForgivenMinutes(15)).willReturn(7);
        given(overtimeCalculationStrategy.calculateOvertimeCost(
                Money.of("100.00"), TariffPeriod.HOUR, 15, 7))
                .willReturn(Money.of("16.67"));

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.overtimeMinutes()).isEqualTo(15);
        assertThat(result.forgivenessApplied()).isFalse();
        // Chargeable overtime = 15 - 7 = 8 minutes, rounded to 10 minutes = 10 minutes
        // Price per 5 min = 100 / 60 * 5 = 8.33
        // Intervals = 10 / 5 = 2
        // Overtime cost = 8.33 * 2 = 16.67
        assertThat(result.overtimeCost().amount()).isEqualByComparingTo(Money.of("16.67").amount());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "5",
            "7"
    })
    @DisplayName("Should apply forgiveness for overtime within threshold")
    void shouldApplyForgivenessForOvertimeWithinThreshold(int overtimeMinutes) {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(60 + overtimeMinutes);
        int billableMinutes = 60 + overtimeMinutes;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(overtimeMinutes)).willReturn(true);
        given(forgivenessStrategy.getForgivenessMessage(overtimeMinutes)).willReturn(
                overtimeMinutes <= 0 ? "Возврат вовремя или досрочно" : String.format("Просрочка прощена (%d минут просрочки)", overtimeMinutes));

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result.overtimeMinutes()).isEqualTo(overtimeMinutes);
        assertThat(result.forgivenessApplied()).isTrue();
        assertThat(result.overtimeCost()).isEqualByComparingTo(Money.zero());
    }

    @ParameterizedTest
    @CsvSource({
            "8, 8.33",
            "10, 8.33",
            "12, 16.67",
            "15, 16.67",
            "20, 25.00"
    })
    @DisplayName("Should calculate overtime cost for overtime beyond forgiveness threshold")
    void shouldCalculateOvertimeCostForOvertimeBeyondForgivenessThreshold(int overtimeMinutes, String expectedOvertimeCost) {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(1);
        Duration actualDuration = Duration.ofMinutes(60 + overtimeMinutes);
        int billableMinutes = 60 + overtimeMinutes;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HOUR);
        given(forgivenessStrategy.shouldForgive(overtimeMinutes)).willReturn(false);
        given(forgivenessStrategy.getForgivenMinutes(overtimeMinutes)).willReturn(7);
        given(overtimeCalculationStrategy.calculateOvertimeCost(
                Money.of("100.00"), TariffPeriod.HOUR, overtimeMinutes, 7))
                .willReturn(Money.of(expectedOvertimeCost));

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result.overtimeMinutes()).isEqualTo(overtimeMinutes);
        assertThat(result.forgivenessApplied()).isFalse();
        assertThat(result.overtimeCost().amount()).isEqualByComparingTo(Money.of(expectedOvertimeCost).amount());
    }

    @Test
    @DisplayName("Should use half-hour price for 30-minute rental")
    void shouldUseHalfHourPriceFor30MinuteRental() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofMinutes(30);
        Duration actualDuration = Duration.ofMinutes(30);
        int billableMinutes = 30;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.HALF_HOUR);
        given(forgivenessStrategy.shouldForgive(0)).willReturn(true);
        given(forgivenessStrategy.getForgivenessMessage(0)).willReturn("Возврат вовремя или досрочно");

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("60.00"));
    }

    @Test
    @DisplayName("Should use day price for 4+ hour rental")
    void shouldUseDayPriceFor4PlusHourRental() {
        // Given
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(5);
        Duration actualDuration = Duration.ofMinutes(300);
        int billableMinutes = 300;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.DAY);
        given(forgivenessStrategy.shouldForgive(0)).willReturn(true);
        given(forgivenessStrategy.getForgivenessMessage(0)).willReturn("Возврат вовремя или досрочно");

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("500.00"));
    }

    @Test
    @DisplayName("Should calculate overtime cost proportionally based on period price")
    void shouldCalculateOvertimeCostProportionallyBasedOnPeriodPrice() {
        // Given - using day price (500.00 for 24 hours)
        Long tariffId = 1L;
        Duration plannedDuration = Duration.ofHours(5);
        Duration actualDuration = Duration.ofMinutes(310); // 10 minutes overtime
        int billableMinutes = 310;

        Tariff tariff = createTestTariff();
        given(getTariffByIdUseCase.get(tariffId)).willReturn(tariff);
        given(tariffPeriodSelector.selectPeriod(actualDuration)).willReturn(TariffPeriod.DAY);
        given(forgivenessStrategy.shouldForgive(10)).willReturn(false);
        given(forgivenessStrategy.getForgivenMinutes(10)).willReturn(7);
        given(overtimeCalculationStrategy.calculateOvertimeCost(
                Money.of("500.00"), TariffPeriod.DAY, 10, 7))
                .willReturn(Money.of("1.74"));

        CalculateRentalCostUseCase.CalculateRentalCostCommand command =
                new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);

        // When
        RentalCost result = calculator.execute(command);

        // Then
        assertThat(result.baseCost()).isEqualByComparingTo(Money.of("500.00"));
        assertThat(result.overtimeMinutes()).isEqualTo(10);
        assertThat(result.forgivenessApplied()).isFalse();
        // Chargeable overtime = 10 - 7 = 3 minutes, rounded to 5 minutes = 5 minutes
        // Price per 5 min = 500 / 1440 * 5 = 1.74
        // Overtime cost = 1.74
        assertThat(result.overtimeCost().amount()).isEqualByComparingTo(Money.of("1.74").amount());
    }
}
