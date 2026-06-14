package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;

public interface RentalCostCalculationV2UseCase {

    RentalCostCalculationResult execute(RentalCostCalculationV2Command command);
}
