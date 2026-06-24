package com.github.jenkaby.bikerental.identity.infrastructure.bootstrap;

import com.github.jenkaby.bikerental.identity.application.config.SpaClientsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.security.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
class SpaClientBootstrapRunner implements ApplicationRunner {

    private final RegisteredClientRepository registeredClientRepository;
    private final SpaClientsProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        properties.spaClients().forEach(this::registerIfAbsent);
    }

    private void registerIfAbsent(SpaClientsProperties.SpaClient client) {
        if (registeredClientRepository.findByClientId(client.clientId()) != null) {
            return;
        }
        var builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(client.clientId())
                .clientName(client.clientId())
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .reuseRefreshTokens(true)
                        .build());
        client.redirectUris().forEach(builder::redirectUri);
        client.postLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
        client.scopes().forEach(builder::scope);
        registeredClientRepository.save(builder.build());
        log.info("Registered SPA OAuth2 client '{}'", client.clientId());
    }
}
