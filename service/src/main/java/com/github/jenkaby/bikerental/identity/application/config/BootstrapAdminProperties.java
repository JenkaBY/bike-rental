package com.github.jenkaby.bikerental.identity.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security.bootstrap.admin-user")
public record BootstrapAdminProperties(

        @DefaultValue("admin")
        String username,

        @DefaultValue("admin@bike-rental.local")
        String email,

        @DefaultValue("Administrator")
        String displayName,

        String password
) {
}
