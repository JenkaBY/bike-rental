package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

/**
 * Result interface for rental cost calculation.
 *
 * <p>Provides detailed breakdown of cost calculation including:
 * <ul>
 *   <li>Base cost for planned duration</li>
 *   <li>Overtime cost for excess time</li>
 *   <li>Total cost (base + overtime)</li>
 *   <li>Duration details (actual, billable, planned, overtime)</li>
 *   <li>Forgiveness application status</li>
 * </ul>
 */
public interface RentalCost {

    Money baseCost();

    Money overtimeCost();

    default Money totalCost() {
        return baseCost().add(overtimeCost());
    }

    int actualMinutes();

    /**
     * Billable duration in minutes (rounded to 5-minute increments).
     *
     * @return billable minutes after rounding
     */
    int billableMinutes();

    /**
     * Planned rental duration in minutes.
     *
     * @return planned duration in minutes
     */
    int plannedMinutes();

    /**
     * Overtime duration in minutes (actual - planned).
     *
     * @return overtime minutes, can be negative if returned early
     */
    int overtimeMinutes();

    /**
     * Whether forgiveness rule was applied (overtime <= threshold).
     *
     * @return true if forgiveness was applied, false otherwise
     */
    boolean forgivenessApplied();

    /**
     * Human-readable message describing the calculation.
     *
     * @return calculation message (e.g., "On time", "Forgiven", "Overtime charged")
     */
    String calculationMessage();
}
