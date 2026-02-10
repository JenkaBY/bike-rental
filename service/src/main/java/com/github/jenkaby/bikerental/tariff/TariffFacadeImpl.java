package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateEstimatedCostUseCase;
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

    TariffFacadeImpl(
            GetTariffByIdUseCase getTariffByIdUseCase,
            TariffToInfoMapper tariffToInfoMapper,
            SelectTariffForRentalUseCase selectTariffForRentalUseCase,
            CalculateEstimatedCostUseCase calculateEstimatedCostUseCase) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
        this.tariffToInfoMapper = tariffToInfoMapper;
        this.selectTariffForRentalUseCase = selectTariffForRentalUseCase;
        this.calculateEstimatedCostUseCase = calculateEstimatedCostUseCase;
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
}
