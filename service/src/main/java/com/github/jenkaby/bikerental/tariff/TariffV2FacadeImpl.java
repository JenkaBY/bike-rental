package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.tariff.application.mapper.TariffV2ToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffV2ByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class TariffV2FacadeImpl implements TariffV2Facade {

    private final GetTariffV2ByIdUseCase getTariffV2ByIdUseCase;
    private final TariffV2ToInfoMapper tariffV2ToInfoMapper;
    private final RentalCostCalculationUseCase rentalCostCalculationUseCase;

    TariffV2FacadeImpl(GetTariffV2ByIdUseCase getTariffV2ByIdUseCase,
                       TariffV2ToInfoMapper tariffV2ToInfoMapper,
                       RentalCostCalculationUseCase rentalCostCalculationUseCase) {
        this.getTariffV2ByIdUseCase = getTariffV2ByIdUseCase;
        this.tariffV2ToInfoMapper = tariffV2ToInfoMapper;
        this.rentalCostCalculationUseCase = rentalCostCalculationUseCase;
    }

    @Override
    public Optional<TariffV2Info> findById(Long tariffId) {
        return getTariffV2ByIdUseCase.execute(tariffId)
                .map(tariffV2ToInfoMapper::toTariffV2Info);
    }

    @Override
    public RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command) {
        return rentalCostCalculationUseCase.execute(command);
    }
}
