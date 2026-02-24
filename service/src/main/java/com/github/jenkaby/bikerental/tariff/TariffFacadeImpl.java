package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateEstimatedCostUseCase;
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
    private final CalculateEstimatedCostUseCase calculateEstimatedCostUseCase;
    private final CalculateRentalCostUseCase calculateRentalCostUseCase;

    TariffFacadeImpl(
            GetTariffByIdUseCase getTariffByIdUseCase,
            TariffToInfoMapper tariffToInfoMapper,
            SelectTariffForRentalUseCase selectTariffForRentalUseCase,
            CalculateEstimatedCostUseCase calculateEstimatedCostUseCase,
            CalculateRentalCostUseCase calculateRentalCostUseCase) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
        this.tariffToInfoMapper = tariffToInfoMapper;
        this.selectTariffForRentalUseCase = selectTariffForRentalUseCase;
        this.calculateEstimatedCostUseCase = calculateEstimatedCostUseCase;
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
    public Money calculateEstimatedCost(Long tariffId, Duration duration) {
        var command = new CalculateEstimatedCostUseCase.CalculateEstimatedCostCommand(tariffId, duration);
        return calculateEstimatedCostUseCase.execute(command);
    }

    @Override
    public RentalCost calculateFinalCost(Long tariffId, Duration actualDuration, int billableMinutes, Duration plannedDuration) {
        var command = new CalculateRentalCostUseCase.CalculateRentalCostCommand(tariffId, actualDuration, billableMinutes, plannedDuration);
        return calculateRentalCostUseCase.execute(command);
    }
}
