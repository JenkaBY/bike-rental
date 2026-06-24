package com.github.jenkaby.bikerental.users.application.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(

        @NotBlank
        @DefaultValue("http://localhost:8080")
        String issuer,

        @NotNull
        @DefaultValue("PT15M")
        Duration accessTokenTtl,

        @NotNull
        @DefaultValue("P14D")
        Duration refreshTokenTtl,

        @NotBlank
        @DefaultValue("bike-rental-identity")
        String keyId,

        @NotBlank
        @DefaultValue("uid")
        String userIdClaim,

        String privateKeyLocation,

        String publicKeyLocation
) {
}
