package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.application.strategy.ForgivenessStrategy;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateRentalCostUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostResult;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
class CalculateRentalCostService implements CalculateRentalCostUseCase {

    private final GetTariffByIdUseCase getTariffByIdUseCase;
    private final TariffPeriodSelector tariffPeriodSelector;
    private final ForgivenessStrategy forgivenessStrategy;

    CalculateRentalCostService(
            GetTariffByIdUseCase getTariffByIdUseCase,
            TariffPeriodSelector tariffPeriodSelector,
            ForgivenessStrategy forgivenessStrategy) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
        this.tariffPeriodSelector = tariffPeriodSelector;
        this.forgivenessStrategy = forgivenessStrategy;
    }

    @Override
    public @NonNull RentalCost execute(@NonNull CalculateRentalCostCommand command) {
        // 1. Get tariff
        Tariff tariff = getTariffByIdUseCase.get(command.tariffId());

        // 2. Determine base cost based on actual duration
        TariffPeriod period = tariffPeriodSelector.selectPeriod(command.actualDuration());
        Money basePrice = tariff.getPriceForPeriod(period);

        long periodMinutes = period.getMinutes();
        int actualMinutes = (int) command.actualDuration().toMinutes();

        long periods = (long) Math.ceil((double) actualMinutes / periodMinutes);
        Money baseCost = basePrice.multiply(BigDecimal.valueOf(periods));

        int billableMinutes = command.billableMinutes();
        int plannedMinutes = (int) command.plannedDuration().toMinutes();
        int overtimeMinutes = actualMinutes - plannedMinutes;

        // With period-multiplier pricing, baseCost already covers all actual time
        // in full period increments (ceil), so there is never an additional overtime charge.
        // overtimeMinutes is kept for informational purposes only.
        boolean forgivenessApplied = overtimeMinutes <= 0;
        String message = forgivenessApplied
                ? forgivenessStrategy.getForgivenessMessage(overtimeMinutes)
                : String.format("Overtime covered by period pricing (%d minute(s) over plan, %d period(s) charged)",
                overtimeMinutes, periods);

        return BaseRentalCostResult.withForgiveness(
                baseCost,
                actualMinutes,
                billableMinutes,
                plannedMinutes,
                overtimeMinutes,
                message
        );
    }
}
