package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.tariff.application.mapper.TariffV2ToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffV2ByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostQuoteUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class TariffV2FacadeImpl implements TariffV2Facade {

    private final GetTariffV2ByIdUseCase getTariffV2ByIdUseCase;
    private final TariffV2ToInfoMapper tariffV2ToInfoMapper;
    private final RentalCostCalculationUseCase rentalCostCalculationUseCase;
    private final RentalCostCalculationV2UseCase rentalCostCalculationV2UseCase;
    private final RentalCostQuoteUseCase rentalCostQuoteUseCase;

    TariffV2FacadeImpl(GetTariffV2ByIdUseCase getTariffV2ByIdUseCase,
                       TariffV2ToInfoMapper tariffV2ToInfoMapper,
                       RentalCostCalculationUseCase rentalCostCalculationUseCase,
                       RentalCostCalculationV2UseCase rentalCostCalculationV2UseCase,
                       RentalCostQuoteUseCase rentalCostQuoteUseCase) {
        this.getTariffV2ByIdUseCase = getTariffV2ByIdUseCase;
        this.tariffV2ToInfoMapper = tariffV2ToInfoMapper;
        this.rentalCostCalculationUseCase = rentalCostCalculationUseCase;
        this.rentalCostCalculationV2UseCase = rentalCostCalculationV2UseCase;
        this.rentalCostQuoteUseCase = rentalCostQuoteUseCase;
    }

    @Override
    public Optional<TariffV2Info> findById(Long tariffId) {
        return getTariffV2ByIdUseCase.execute(tariffId)
                .map(tariffV2ToInfoMapper::toTariffV2Info);
    }

    @Deprecated(forRemoval = true)
    @Override
    public RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command) {
        return rentalCostCalculationUseCase.execute(command);
    }

    @Override
    public RentalCostCalculationResult calculateRentalCostV2(RentalCostCalculationV2Command command) {
        return rentalCostCalculationV2UseCase.execute(command);
    }

    @Override
    public RentalCostQuote getQuote(UUID quoteId) {
        return rentalCostQuoteUseCase.getQuote(quoteId);
    }

    @Override
    public RentalCostQuote consumeQuote(UUID quoteId) {
        return rentalCostQuoteUseCase.consumeQuote(quoteId);
    }
}
