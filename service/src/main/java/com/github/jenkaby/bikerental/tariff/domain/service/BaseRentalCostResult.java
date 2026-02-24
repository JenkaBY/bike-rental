package com.github.jenkaby.bikerental.tariff.domain.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;


public record BaseRentalCostResult(
        Money baseCost,
        Money overtimeCost,
        int actualMinutes,
        int billableMinutes,
        int plannedMinutes,
        int overtimeMinutes,
        boolean forgivenessApplied,
        String calculationMessage
) implements RentalCost {

    public static BaseRentalCostResult withForgiveness(
            Money baseCost,
            int actualMinutes,
            int billableMinutes,
            int plannedMinutes,
            int overtimeMinutes,
            String message) {
        return new BaseRentalCostResult(
                baseCost,
                Money.zero(),
                actualMinutes,
                billableMinutes,
                plannedMinutes,
                overtimeMinutes,
                true,
                message
        );
    }

    public static BaseRentalCostResult withOvertime(
            Money baseCost,
            Money overtimeCost,
            int actualMinutes,
            int billableMinutes,
            int plannedMinutes,
            int overtimeMinutes,
            String message) {
        return new BaseRentalCostResult(
                baseCost,
                overtimeCost,
                actualMinutes,
                billableMinutes,
                plannedMinutes,
                overtimeMinutes,
                false,
                message
        );
    }
}
