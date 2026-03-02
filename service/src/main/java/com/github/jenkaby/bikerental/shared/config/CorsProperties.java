package com.github.jenkaby.bikerental.shared.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        @NotEmpty
        List<String> allowedOrigins,

        @NotEmpty
        @DefaultValue({"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"})
        List<String> allowedMethods,

        @NotEmpty
        @DefaultValue("*")
        List<String> allowedHeaders,

        @DefaultValue("false")
        boolean allowCredentials,

        @DefaultValue("3600")
        long maxAge
) {
}


