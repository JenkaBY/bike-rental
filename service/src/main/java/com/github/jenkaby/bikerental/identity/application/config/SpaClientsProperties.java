package com.github.jenkaby.bikerental.identity.application.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SpaClientsProperties(

        @NotEmpty
        List<SpaClient> spaClients
) {

    public record SpaClient(

            @NotBlank
            String clientId,

            @NotEmpty
            List<String> redirectUris,

            List<String> postLogoutRedirectUris,

            @NotEmpty
            List<String> scopes
    ) {
        public SpaClient {
            postLogoutRedirectUris = postLogoutRedirectUris == null ? List.of() : postLogoutRedirectUris;
        }
    }
}
