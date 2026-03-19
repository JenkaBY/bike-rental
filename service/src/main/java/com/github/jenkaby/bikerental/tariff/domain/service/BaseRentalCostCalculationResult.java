package com.github.jenkaby.bikerental.tariff.domain.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.DiscountDetail;
import com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult;

import java.time.Duration;
import java.util.List;

public record BaseRentalCostCalculationResult(
        List<EquipmentCostBreakdown> equipmentBreakdowns,
        Money subtotal,
        DiscountDetail discount,
        Money totalCost,
        Duration effectiveDuration,
        boolean estimate,
        boolean specialPricingApplied
) implements RentalCostCalculationResult {
}
