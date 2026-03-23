package com.github.jenkaby.bikerental.tariff;

import java.util.Optional;

public interface TariffV2Facade {

    Optional<TariffV2Info> findById(Long tariffId);

    RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command);
}
