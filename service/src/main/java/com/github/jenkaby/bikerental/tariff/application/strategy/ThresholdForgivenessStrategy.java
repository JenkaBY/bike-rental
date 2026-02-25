package com.github.jenkaby.bikerental.tariff.application.strategy;

import com.github.jenkaby.bikerental.shared.application.service.MessageService;
import com.github.jenkaby.bikerental.shared.config.RentalProperties.ForgivenessProperties;
import org.jspecify.annotations.NonNull;


public class ThresholdForgivenessStrategy implements ForgivenessStrategy {

    private final ForgivenessProperties forgivenessProperties;
    private final int thresholdMinutes;
    private final MessageService messageService;

    public ThresholdForgivenessStrategy(
            ForgivenessProperties forgivenessProperties,
            MessageService messageService) {
        if (forgivenessProperties == null) {
            throw new IllegalArgumentException("ForgivenessProperties cannot be null");
        }
        if (messageService == null) {
            throw new IllegalArgumentException("MessageService cannot be null");
        }
        this.forgivenessProperties = forgivenessProperties;
        this.messageService = messageService;
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
        return Math.min(overtimeMinutes, thresholdMinutes); // All overtime is forgiven within threshold
    }

    @Override
    public @NonNull String getForgivenessMessage(int overtimeMinutes) {
        if (overtimeMinutes <= 0) {
            return messageService.getMessage("forgiveness.message.on-time");
        }
        return messageService.getMessage("forgiveness.message.forgiven", overtimeMinutes);
    }
}
