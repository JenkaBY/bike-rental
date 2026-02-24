package com.github.jenkaby.bikerental.shared.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;


@Validated
@ConfigurationProperties(prefix = "app.rental")
public record RentalProperties(
        @NotNull
        Duration timeIncrement,

        @NotNull
        ForgivenessProperties forgiveness
) {
    public record ForgivenessProperties(
            @NotNull
            Duration overtimeDuration
    ) {
        public int overtimeDurationMinutes() {
            return (int) overtimeDuration.toMinutes();
        }
    }

    public int getForgivenessThresholdMinutes() {
        return forgiveness.overtimeDurationMinutes();
    }

    public int getTimeIncrementMinutes() {
        return (int) timeIncrement.toMinutes();
    }
}
