package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult;

public interface RentalCostCalculationUseCase {

    RentalCostCalculationResult execute(RentalCostCalculationCommand command);
}
