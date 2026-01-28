package com.github.jenkaby.bikerental.customer.application.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.customers")
public record CustomerSearchProperties(
        @Min(1)
        int searchLimitResult
) {
}
