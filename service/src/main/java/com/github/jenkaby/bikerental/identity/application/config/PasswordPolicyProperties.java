package com.github.jenkaby.bikerental.identity.application.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.password")
public record PasswordPolicyProperties(

        @Min(1)
        @DefaultValue("8")
        int minLength,

        @Min(1)
        @DefaultValue("20")
        int maxLength
) {
}
