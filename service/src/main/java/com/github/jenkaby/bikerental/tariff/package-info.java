/**
 * Tariff Management Module.
 *
 * <p>This module handles tariff catalog, pricing configuration, and cost calculation
 * for equipment rentals. It provides tariff CRUD operations, activation/deactivation,
 * and querying capabilities for rental pricing.
 *
 * <p>Public API:
 * <ul>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.TariffFacade} - External module interface</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.TariffInfo} - Public tariff DTO</li>
 *   <li>{@link com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException} - Exception thrown by TariffFacade</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Tariff Module"
)
package com.github.jenkaby.bikerental.tariff;
