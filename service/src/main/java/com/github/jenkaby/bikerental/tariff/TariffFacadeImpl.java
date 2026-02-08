package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
class TariffFacadeImpl implements TariffFacade {

    private final GetTariffByIdUseCase getTariffByIdUseCase;
    private final GetActiveTariffsByEquipmentTypeUseCase getActiveTariffsByEquipmentTypeUseCase;
    private final TariffToInfoMapper tariffToInfoMapper;
    private final TariffPeriodSelector tariffPeriodSelector;

    TariffFacadeImpl(
            GetTariffByIdUseCase getTariffByIdUseCase,
            GetActiveTariffsByEquipmentTypeUseCase getActiveTariffsByEquipmentTypeUseCase,
            TariffToInfoMapper tariffToInfoMapper,
            TariffPeriodSelector tariffPeriodSelector) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
        this.getActiveTariffsByEquipmentTypeUseCase = getActiveTariffsByEquipmentTypeUseCase;
        this.tariffToInfoMapper = tariffToInfoMapper;
        this.tariffPeriodSelector = tariffPeriodSelector;
    }

    @Override
    public Optional<TariffInfo> findById(Long tariffId) {
        return getTariffByIdUseCase.execute(tariffId)
                .map(tariffToInfoMapper::toTariffInfo);
    }

    @Override
    public TariffInfo selectTariff(String equipmentTypeSlug, Duration duration, LocalDate rentalDate) {
        List<Tariff> activeTariffs = getActiveTariffsByEquipmentTypeUseCase.execute(equipmentTypeSlug);

        // Filter tariffs valid for rental date
        List<Tariff> validTariffs = activeTariffs.stream()
                .filter(tariff -> tariff.isValidOn(rentalDate))
                .toList();

        if (validTariffs.isEmpty()) {
            throw new SuitableTariffNotFoundException(equipmentTypeSlug, rentalDate, duration);
        }

        // Match by duration - determine the appropriate period
        Duration selectedPeriodDuration = tariffPeriodSelector.selectPeriodDuration(duration);

        // Select tariff with lowest base price
        // Since period is determined by rental duration (same for all tariffs),
        // we simply select the tariff with the lowest price
        Tariff selectedTariff = validTariffs.stream()
                .min(Comparator.comparing(tariff -> tariff.getBasePrice().amount()))
                .orElseThrow(() -> new SuitableTariffNotFoundException(
                        equipmentTypeSlug,
                        rentalDate,
                        duration
                ));

        return tariffToInfoMapper.toTariffInfo(selectedTariff);
    }

    @Override
    public Money calculateEstimatedCost(Long tariffId, Duration duration, LocalDateTime startTime) {
        Tariff tariff = getTariffByIdUseCase.get(tariffId);

        // Simple calculation: use base price for now
        // Full cost calculation with overtime will be implemented in US-TR-002
        return tariff.getBasePrice();
    }
}
