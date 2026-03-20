package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;
import java.util.List;

public interface RentalCostCalculationResult {

    List<EquipmentCostBreakdown> equipmentBreakdowns();

    Money subtotal();

    DiscountDetail discount();

    Money totalCost();

    Duration effectiveDuration();

    boolean estimate();

    boolean specialPricingApplied();
}
