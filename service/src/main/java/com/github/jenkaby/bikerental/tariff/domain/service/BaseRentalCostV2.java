package com.github.jenkaby.bikerental.tariff.domain.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;

public record BaseRentalCostV2(
        Money totalCost,
        BreakdownCostDetails calculationBreakdown
) implements RentalCostV2 {
}
