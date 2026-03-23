package com.github.jenkaby.bikerental.tariff.domain.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown;

import java.time.Duration;

public record BaseEquipmentCostBreakdown(
        String equipmentType,
        Long tariffId,
        String tariffName,
        String pricingType,
        Money itemCost,
        Duration billedDuration,
        Duration overtime,
        Duration forgiven,
        BreakdownCostDetails calculationBreakdown
) implements EquipmentCostBreakdown {
}
