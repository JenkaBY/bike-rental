package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;

public interface EquipmentCostBreakdown {

    String equipmentType();

    Long tariffId();

    String tariffName();

    String pricingType();

    Money itemCost();

    Duration billedDuration();

    default boolean forgivenessApplied() {
        return forgiven() != null && !forgiven().isPositive();
    }

    Duration overtime();

    Duration forgiven();

    BreakdownCostDetails calculationBreakdown();
}
