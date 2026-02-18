package com.github.jenkaby.bikerental.rental.application.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;


@Validated
@ConfigurationProperties(prefix = "app.rental")
public record RentalProperties(
        @NotNull
        Duration timeIncrement,

        // Future property for US-TR-003 (forgiveness rule)
        // Can be null until US-TR-003 is implemented
        ForgivenessProperties forgiveness
) {
    public record ForgivenessProperties(
            @NotNull
            Duration overtimeDuration
    ) {
    }
}
