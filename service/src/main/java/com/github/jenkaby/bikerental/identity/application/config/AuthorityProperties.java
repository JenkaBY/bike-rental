package com.github.jenkaby.bikerental.identity.application.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.authority")
public record AuthorityProperties(

        @NotBlank
        @DefaultValue("ROLE_")
        String rolePrefix
) {
}
