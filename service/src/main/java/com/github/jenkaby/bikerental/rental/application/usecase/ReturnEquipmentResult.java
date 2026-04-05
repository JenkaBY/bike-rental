package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public record ReturnEquipmentResult(
        Rental rental,
        Map<Long, RentalCost> breakDownCosts,
        @Nullable SettlementInfo settlementInfo
) {
}
