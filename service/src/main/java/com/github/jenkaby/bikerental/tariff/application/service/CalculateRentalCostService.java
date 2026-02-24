package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.application.strategy.ForgivenessStrategy;
import com.github.jenkaby.bikerental.tariff.application.strategy.OvertimeCalculationStrategy;
import com.github.jenkaby.bikerental.tariff.application.usecase.CalculateRentalCostUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostResult;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;


@Service
class CalculateRentalCostService implements CalculateRentalCostUseCase {

    private final GetTariffByIdUseCase getTariffByIdUseCase;
    private final TariffPeriodSelector tariffPeriodSelector;
    private final ForgivenessStrategy forgivenessStrategy;
    private final OvertimeCalculationStrategy overtimeCalculationStrategy;

    CalculateRentalCostService(
            GetTariffByIdUseCase getTariffByIdUseCase,
            TariffPeriodSelector tariffPeriodSelector,
            ForgivenessStrategy forgivenessStrategy,
            OvertimeCalculationStrategy overtimeCalculationStrategy) {
        this.getTariffByIdUseCase = getTariffByIdUseCase;
        this.tariffPeriodSelector = tariffPeriodSelector;
        this.forgivenessStrategy = forgivenessStrategy;
        this.overtimeCalculationStrategy = overtimeCalculationStrategy;
    }

    @Override
    public @NonNull RentalCost execute(@NonNull CalculateRentalCostCommand command) {
        // 1. Get tariff
        Tariff tariff = getTariffByIdUseCase.get(command.tariffId());

        // 2. Determine base cost based on actual duration
        TariffPeriod period = tariffPeriodSelector.selectPeriod(command.actualDuration());
        Money basePrice = tariff.getPriceForPeriod(period);
        Money baseCost = basePrice; // Base cost is the period price

        // 3. Get duration details
        int actualMinutes = (int) command.actualDuration().toMinutes();
        int billableMinutes = command.billableMinutes();
        int plannedMinutes = (int) command.plannedDuration().toMinutes();
        int overtimeMinutes = actualMinutes - plannedMinutes;

        // 4. Apply forgiveness strategy
        if (forgivenessStrategy.shouldForgive(overtimeMinutes)) {
            String message = forgivenessStrategy.getForgivenessMessage(overtimeMinutes);

            return BaseRentalCostResult.withForgiveness(
                    baseCost,
                    actualMinutes,
                    billableMinutes,
                    plannedMinutes,
                    overtimeMinutes,
                    message
            );
        }

        // 5. Calculate overtime cost using strategy
        int forgivenMinutes = forgivenessStrategy.getForgivenMinutes(overtimeMinutes);
        Money overtimeCost = overtimeCalculationStrategy.calculateOvertimeCost(
                basePrice,
                period,
                overtimeMinutes,
                forgivenMinutes
        );

        String message = String.format("Overtime charged (%d minutes beyond forgiveness)",
                overtimeMinutes - forgivenMinutes);

        return BaseRentalCostResult.withOvertime(
                baseCost,
                overtimeCost,
                actualMinutes,
                billableMinutes,
                plannedMinutes,
                overtimeMinutes,
                message
        );
    }
}
