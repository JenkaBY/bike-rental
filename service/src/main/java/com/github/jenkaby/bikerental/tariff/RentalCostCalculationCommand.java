package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public record RentalCostCalculationCommand(
        @NonNull List<EquipmentCostItem> equipments,
        @NonNull Duration plannedDuration,
        @Nullable Duration actualDuration,
        @Nullable DiscountPercent discount,
        @Nullable Long specialTariffId,
        @Nullable Money specialPrice,
        LocalDate rentalDate
) {
    public Duration effectiveDuration() {
        return actualDuration != null ? actualDuration : plannedDuration;
    }
}
