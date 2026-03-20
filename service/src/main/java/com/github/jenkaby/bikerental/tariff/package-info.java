/**
 * Tariff Management Module.
 *
 * <p>This module handles tariff catalog, pricing configuration, and cost calculation
 * for equipment rentals. It provides tariff CRUD operations, activation/deactivation,
 * and querying capabilities for rental pricing.
 *
 * <p>Public API:
 * <ul>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.TariffFacade} - External module interface (V1)</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.TariffInfo} - Public tariff DTO (V1)</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException} - Exception thrown by TariffFacade (V1)</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.TariffV2Facade} - External module interface (V2)</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.TariffV2Info} - Public tariff DTO (V2)</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand} - V2 batch calculation input</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult} - V2 batch calculation output</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Tariff Module"
)
package com.github.jenkaby.bikerental.tariff;
