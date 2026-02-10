package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffForRentalUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@DisplayName("SelectTariffForRentalService Tests")
class SelectTariffForRentalServiceTest {

    @Mock
    private GetActiveTariffsByEquipmentTypeUseCase getActiveTariffsByEquipmentTypeUseCase;

    @Mock
    private TariffPeriodSelector tariffPeriodSelector;
    @Spy
    private Clock realClock = spy(Clock.systemDefaultZone());
    @InjectMocks
    private SelectTariffForRentalService service;

    @Test
    @DisplayName("Should select tariff successfully")
    void shouldSelectTariffSuccessfully() {
        String equipmentType = "bike";
        int durationMinutes = 60;
        LocalDate rentalDate = LocalDate.of(2026, 2, 10);
        Duration duration = Duration.ofMinutes(durationMinutes);

        SelectTariffForRentalUseCase.SelectTariffCommand command =
                new SelectTariffForRentalUseCase.SelectTariffCommand(equipmentType, durationMinutes, rentalDate);

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

        given(getActiveTariffsByEquipmentTypeUseCase.execute(equipmentType))
                .willReturn(List.of(tariff));
        given(tariffPeriodSelector.selectPeriod(duration))
                .willReturn(com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod.HOUR);

        Tariff result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Standard Hourly");
        assertThat(result.getEquipmentTypeSlug()).isEqualTo("bike");

        then(getActiveTariffsByEquipmentTypeUseCase).should().execute(equipmentType);
        then(tariffPeriodSelector).should().selectPeriod(duration);
    }

    @Test
    @DisplayName("Should use today's date when rentalDate is null")
    void shouldUseTodaysDateWhenRentalDateIsNull() {
        String equipmentType = "bike";
        int durationMinutes = 60;
        Duration duration = Duration.ofMinutes(durationMinutes);

        SelectTariffForRentalUseCase.SelectTariffCommand command =
                new SelectTariffForRentalUseCase.SelectTariffCommand(equipmentType, durationMinutes, null);

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

        given(getActiveTariffsByEquipmentTypeUseCase.execute(equipmentType))
                .willReturn(List.of(tariff));
        given(tariffPeriodSelector.selectPeriod(duration))
                .willReturn(com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod.HOUR);

        Tariff result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        then(getActiveTariffsByEquipmentTypeUseCase).should().execute(equipmentType);
    }

    @Test
    @DisplayName("Should throw exception when no suitable tariff found")
    void shouldThrowExceptionWhenNoSuitableTariffFound() {
        String equipmentType = "bike";
        int durationMinutes = 60;
        LocalDate rentalDate = LocalDate.of(2026, 2, 10);
        Duration duration = Duration.ofMinutes(durationMinutes);

        SelectTariffForRentalUseCase.SelectTariffCommand command =
                new SelectTariffForRentalUseCase.SelectTariffCommand(equipmentType, durationMinutes, rentalDate);

        given(getActiveTariffsByEquipmentTypeUseCase.execute(equipmentType))
                .willReturn(List.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SuitableTariffNotFoundException.class);

        then(getActiveTariffsByEquipmentTypeUseCase).should().execute(equipmentType);
    }
}
