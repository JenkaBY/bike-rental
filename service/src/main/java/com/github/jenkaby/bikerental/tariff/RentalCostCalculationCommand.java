package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public record RentalCostCalculationCommand(
        List<EquipmentCostItem> equipments,
        Duration plannedDuration,
        Duration actualDuration,
        DiscountPercent discount,
        Long specialTariffId,
        Money specialPrice,
        LocalDate rentalDate
) {
    public Duration effectiveDuration() {
        return actualDuration != null ? actualDuration : plannedDuration;
    }
}
