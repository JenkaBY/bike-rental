package com.github.jenkaby.bikerental.tariff.application.strategy;

import com.github.jenkaby.bikerental.shared.config.RentalProperties.ForgivenessProperties;
import org.jspecify.annotations.NonNull;


public class ThresholdForgivenessStrategy implements ForgivenessStrategy {

    private final ForgivenessProperties forgivenessProperties;
    private final int thresholdMinutes;

    public ThresholdForgivenessStrategy(ForgivenessProperties forgivenessProperties) {
        if (forgivenessProperties == null) {
            throw new IllegalArgumentException("ForgivenessProperties cannot be null");
        }
        this.forgivenessProperties = forgivenessProperties;
        this.thresholdMinutes = forgivenessProperties.overtimeDurationMinutes();
        if (thresholdMinutes < 0) {
            throw new IllegalArgumentException("Forgiveness threshold cannot be negative");
        }
    }

    @Override
    public boolean shouldForgive(int overtimeMinutes) {
        return overtimeMinutes <= thresholdMinutes;
    }

    @Override
    public int getForgivenMinutes(int overtimeMinutes) {
        if (overtimeMinutes <= 0) {
            return 0;
        }
        if (overtimeMinutes <= thresholdMinutes) {
            return overtimeMinutes; // All overtime is forgiven within threshold
        }
        return thresholdMinutes; // Only threshold amount is forgiven
    }

    @Override
    public @NonNull String getForgivenessMessage(int overtimeMinutes) {
        if (overtimeMinutes <= 0) {
            return "On time or early return";
        }
        return String.format("Forgiven (%d minutes overtime)", overtimeMinutes);
    }

    public int getThresholdMinutes() {
        return thresholdMinutes;
    }
}
