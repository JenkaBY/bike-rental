package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffForRentalUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
class SelectTariffForRentalService implements SelectTariffForRentalUseCase {

    private final GetActiveTariffsByEquipmentTypeUseCase getActiveTariffsByEquipmentTypeUseCase;
    private final TariffPeriodSelector tariffPeriodSelector;
    private final Clock clock;

    SelectTariffForRentalService(GetActiveTariffsByEquipmentTypeUseCase getActiveTariffsByEquipmentTypeUseCase,
                                 TariffPeriodSelector tariffPeriodSelector, Clock clock) {
        this.getActiveTariffsByEquipmentTypeUseCase = getActiveTariffsByEquipmentTypeUseCase;
        this.tariffPeriodSelector = tariffPeriodSelector;
        this.clock = clock;
    }

    @Override
    public Tariff execute(SelectTariffForRentalUseCase.SelectTariffCommand command) {
        LocalDate rentalDate = command.rentalDate() != null ? command.rentalDate() : LocalDate.now(clock);
        Duration duration = Duration.ofMinutes(command.durationMinutes());

        return selectTariff(
                command.equipmentType(),
                duration,
                rentalDate
        );
    }

    private Tariff selectTariff(String equipmentTypeSlug, Duration duration, LocalDate rentalDate) {
        List<Tariff> activeTariffs = getActiveTariffsByEquipmentTypeUseCase.execute(equipmentTypeSlug);

        // Match by duration - determine the appropriate period
        TariffPeriod selectedPeriod = tariffPeriodSelector.selectPeriod(duration);

        // Get valid tariff for rental date with minimal price
        return activeTariffs.stream()
                .filter(tariff -> tariff.isValidOn(rentalDate))
                .min(Comparator.comparing(tariff -> tariff.getPriceForPeriod(selectedPeriod).amount()))
                .orElseThrow(() -> new SuitableTariffNotFoundException(equipmentTypeSlug, rentalDate, duration));
    }
}
