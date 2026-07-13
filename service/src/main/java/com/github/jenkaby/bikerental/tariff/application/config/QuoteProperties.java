package com.github.jenkaby.bikerental.tariff.application.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.tariff")
public record QuoteProperties(
        @NotNull
        @DefaultValue("5m")
        Duration quoteTtl
) {
}
