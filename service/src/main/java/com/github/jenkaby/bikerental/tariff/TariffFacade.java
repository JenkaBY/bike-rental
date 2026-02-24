package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;


public interface TariffFacade {

    Optional<TariffInfo> findById(Long tariffId);

    TariffInfo selectTariff(String equipmentTypeSlug, Duration duration, LocalDate rentalDate);

    Money calculateEstimatedCost(Long tariffId, Duration duration);

    /**
     * Calculates final rental cost based on actual duration and planned duration.
     *
     * <p>This method considers:
     * <ul>
     *   <li>Base cost from tariff period price (selected based on actual duration)</li>
     *   <li>Forgiveness rule (up to 7 minutes overtime is forgiven)</li>
     *   <li>Proportional overtime charges</li>
     * </ul>
     *
     * <p>The tariff period (HALF_HOUR, HOUR, or DAY) is determined by the actual duration,
     * ensuring customers pay for the tariff tier that matches their actual usage.
     *
     * @param tariffId        tariff ID
     * @param actualDuration  actual rental duration
     * @param billableMinutes billable minutes (rounded to 5-minute increments)
     * @param plannedDuration planned rental duration (used for overtime calculation)
     * @return detailed cost breakdown
     */
    RentalCost calculateFinalCost(Long tariffId, Duration actualDuration, int billableMinutes, Duration plannedDuration);
}
