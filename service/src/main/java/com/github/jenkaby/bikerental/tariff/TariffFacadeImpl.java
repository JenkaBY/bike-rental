package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateRentalCostUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffForRentalUseCase;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Service
class TariffFacadeImpl implements TariffFacade {

    private final GetTariffByIdUseCase getTariffByIdUseCase;
    private final TariffToInfoMapper tariffToInfoMapper;
    private final SelectTariffForRentalUseCase selectTariffForRentalUseCase;
    private final CalculateRentalCostUseCase calculateRentalCostUseCase;

    TariffFacadeImpl(
            GetTariffByIdUseCase getTariffByIdUseCase,
            TariffToInfoMapper tariffToInfoMapper,
            SelectTariffForRentalUseCase selectTariffForRentalUseCase,
            CalculateRentalCostUseCase calculateRentalCostUseCase) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
        this.tariffToInfoMapper = tariffToInfoMapper;
        this.selectTariffForRentalUseCase = selectTariffForRentalUseCase;
        this.calculateRentalCostUseCase = calculateRentalCostUseCase;
    }

    @Override
    public Optional<TariffInfo> findById(Long tariffId) {
        return getTariffByIdUseCase.execute(tariffId)
                .map(tariffToInfoMapper::toTariffInfo);
    }

    @Override
    public TariffInfo selectTariff(String equipmentTypeSlug, Duration duration, LocalDate rentalDate) {
        int durationMinutes = (int) duration.toMinutes();
        var command = new SelectTariffForRentalUseCase.SelectTariffCommand(
                equipmentTypeSlug,
                durationMinutes,
                rentalDate
        );
        var tariff = selectTariffForRentalUseCase.execute(command);
        return tariffToInfoMapper.toTariffInfo(tariff);
    }

    @Override
    public RentalCost calculateRentalCost(Long tariffId, Duration plannedDuration) {
        return calculateRentalCost(tariffId, plannedDuration, (int) plannedDuration.toMinutes(), plannedDuration);
    }

    @Override
    public RentalCost calculateRentalCost(Long tariffId, Duration actualDuration, int billableMinutes, Duration plannedDuration) {
        var command = new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);
        return calculateRentalCostUseCase.execute(command);
    }
}
