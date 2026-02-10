package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateEstimatedCostUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import org.springframework.stereotype.Service;

@Service
class CalculateEstimatedCostService implements CalculateEstimatedCostUseCase {

    private final GetTariffByIdUseCase getTariffByIdUseCase;

    CalculateEstimatedCostService(GetTariffByIdUseCase getTariffByIdUseCase) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
    }

    @Override
    public Money execute(CalculateEstimatedCostUseCase.CalculateEstimatedCostCommand command) {
        var tariff = getTariffByIdUseCase.get(command.tariffId());

        // Simple calculation: use base price for now
        // Full cost calculation with overtime will be implemented in US-TR-002
        return tariff.getBasePrice();
    }
}
